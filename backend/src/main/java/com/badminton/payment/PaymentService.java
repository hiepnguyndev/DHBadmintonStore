package com.badminton.payment;

import static com.badminton.common.support.Checks.*;
import com.badminton.audit.service.AuditService;
import com.badminton.common.support.Access;
import com.badminton.order.entity.*;
import com.badminton.order.repository.OrderRepository;
import com.badminton.order.service.OrderInventoryService;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PaymentService {
  private final OrderRepository orders;
  private final PaymentRepository payments;
  private final PaymentSettings settings;
  private final PaymentGateway gateway;
  private final Access access;
  private final AuditService audit;
  private final TransactionTemplate transactions;
  private final OrderInventoryService inventory;
  public PaymentService(OrderRepository orders, PaymentRepository payments, PaymentSettings settings,
      PaymentGateway gateway, Access access, AuditService audit, PlatformTransactionManager manager,
      OrderInventoryService inventory) {
    this.orders = orders; this.payments = payments; this.settings = settings; this.gateway = gateway;
    this.access = access; this.audit = audit; this.transactions = new TransactionTemplate(manager); this.inventory = inventory;
  }
  public record View(PaymentMethod method, PaymentStatus status, BigDecimal amount, String reference,
      String checkoutUrl, String qrUrl, String bankName, String accountNumber, String accountName,
      String transferContent, LocalDateTime expiresAt, boolean expired, boolean available, boolean sandbox, String message) {}

  @Transactional(readOnly = true)
  public View get(Long orderId) {
    Order order = found(orders.findById(orderId), "ORDER_NOT_FOUND");
    access.ownerOrAdmin(order.getUser().getUserId());
    return view(order, payments.findByOrderId(orderId).orElse(null), null);
  }
  private boolean expired(ShopPayment payment) {
    return payment != null && payment.getExpiresAt() != null && !payment.getExpiresAt().isAfter(LocalDateTime.now(ZoneOffset.UTC));
  }
  private View view(Order order, ShopPayment payment, String message) {
    boolean expired = expired(payment);
    boolean payable = order.getPaymentStatus() == PaymentStatus.UNPAID && order.getOrderStatus() != OrderStatus.CANCELLED && !expired;
    String content = "DH" + order.getOrderId();
    String qr = null;
    if (payable && payment != null && PaymentSettings.bank(payment.getMethod()) && payment.getBankId() != null && !payment.getBankId().isBlank()) {
      qr = "https://img.vietqr.io/image/" + payment.getBankId() + "-" + payment.getAccountNumber() + "-compact2.png?amount="
          + order.getTotalAmount().toBigIntegerExact() + "&addInfo=" + content + "&accountName=" + PaymentCrypto.encode(payment.getAccountName());
    }
    if (message == null && expired && order.getPaymentStatus() == PaymentStatus.UNPAID)
      message = "Phiên thanh toán đã hết hạn. Kiểm tra giao dịch trước khi hủy đơn và đặt lại.";
    return new View(order.getPaymentMethod(), order.getPaymentStatus(), order.getTotalAmount(), payment == null ? null : payment.getReference(),
        payable && payment != null ? payment.getCheckoutUrl() : null, qr,
        payment == null ? null : payment.getBankName(), payment == null ? null : payment.getAccountNumber(),
        payment == null ? null : payment.getAccountName(), content, payment == null ? null : payment.getExpiresAt(), expired,
        settings.enabled(order.getPaymentMethod()), !PaymentSettings.bank(order.getPaymentMethod()) && order.getPaymentMethod() != PaymentMethod.COD && settings.sandbox(), message);
  }
  private ShopPayment prepare(Order order) {
    return payments.findByOrderId(order.getOrderId()).orElseGet(() -> {
      var payment = new ShopPayment();
      payment.setOrderId(order.getOrderId()); payment.setAmount(order.getTotalAmount()); payment.setMethod(order.getPaymentMethod());
      payment.setStatus(PaymentStatus.UNPAID);
      String token = UUID.randomUUID().toString().replace("-", "");
      payment.setReference(order.getPaymentMethod() == PaymentMethod.ZALOPAY
          ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyMMdd")) + "_" + token : "DH" + token);
      if (PaymentSettings.bank(order.getPaymentMethod())) {
        payment.setBankId(settings.get("bank.id")); payment.setBankName(settings.get("bank.name"));
        payment.setAccountNumber(settings.get("bank.account-number")); payment.setAccountName(settings.get("bank.account-name"));
      } else payment.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15));
      return payments.saveAndFlush(payment);
    });
  }
  public View start(Long orderId, String ipAddress) {
    // Commit the stable provider reference before network I/O, so early callbacks can find it.
    ShopPayment payment = transactions.execute(tx -> {
      Order order = found(orders.lock(orderId), "ORDER_NOT_FOUND");
      access.owner(order.getUser().getUserId());
      settings.requireEnabled(order.getPaymentMethod());
      require(order.getPaymentMethod() != PaymentMethod.COD && order.getPaymentStatus() == PaymentStatus.UNPAID
          && order.getOrderStatus() != OrderStatus.CANCELLED, "PAYMENT_INVALID_STATUS", "Đơn hàng không còn chờ thanh toán.");
      settings.validateAmount(order.getPaymentMethod(), order.getTotalAmount());
      return prepare(order);
    });
    if (PaymentSettings.bank(payment.getMethod()) || payment.getCheckoutUrl() != null || expired(payment)) return get(orderId);
    String url;
    try { url = gateway.create(payment, ipAddress); }
    catch (RuntimeException e) {
      return transactions.execute(tx -> view(found(orders.findById(orderId), "ORDER_NOT_FOUND"), payments.findByOrderId(orderId).orElse(null),
          "Chưa lấy được liên kết thanh toán. Kiểm tra trạng thái trước khi thử lại; không chuyển tiền nhiều lần."));
    }
    return transactions.execute(tx -> {
      Order order = found(orders.lock(orderId), "ORDER_NOT_FOUND");
      ShopPayment current = found(payments.findByOrderId(orderId), "PAYMENT_NOT_FOUND");
      if (current.getCheckoutUrl() == null) current.setCheckoutUrl(url);
      return view(order, current, null);
    });
  }
  public enum Result { OK, ALREADY, NOT_FOUND, AMOUNT_MISMATCH, INVALID }
  @Transactional
  public Result confirmSePay(long transactionId, String accountNumber, String code, BigDecimal amount) {
    if (!settings.sepayEnabled() || !settings.get("bank.account-number").equals(accountNumber)
        || code == null || !code.matches("DH[0-9]{1,18}")) return Result.INVALID;
    Long orderId;
    try { orderId = Long.parseLong(code.substring(2)); }
    catch (NumberFormatException e) { return Result.INVALID; }
    Order order = orders.lock(orderId).orElse(null);
    if (order == null) return Result.NOT_FOUND;
    if (!PaymentSettings.bank(order.getPaymentMethod()) || order.getTotalAmount().compareTo(amount) != 0) return Result.AMOUNT_MISMATCH;
    ShopPayment payment = prepare(order);
    String key = "SEPAY:" + transactionId;
    if (payment.getExternalKey() != null)
      return key.equals(payment.getExternalKey()) ? Result.ALREADY : Result.INVALID;
    if (order.getOrderStatus() == OrderStatus.CANCELLED || !Set.of(PaymentStatus.UNPAID, PaymentStatus.FAILED).contains(order.getPaymentStatus())) return Result.INVALID;
    payment.setExternalKey(key); payment.setStatus(PaymentStatus.PAID); order.setPaymentStatus(PaymentStatus.PAID);
    inventory.commit(order);
    audit.record("SEPAY_PAYMENT_CONFIRMED", "ORDER", orderId, null,
        Map.of("transactionId", transactionId, "amount", amount, "code", code));
    return Result.OK;
  }
  @Transactional
  public Result settle(PaymentGateway.Event event) {
    Long orderId = payments.findOrderIdByReference(event.reference()).orElse(null);
    if (orderId == null) return Result.NOT_FOUND;
    Order order = found(orders.lock(orderId), "ORDER_NOT_FOUND");
    ShopPayment payment = found(payments.lockByOrderId(order.getOrderId()), "PAYMENT_NOT_FOUND");
    if (payment.getMethod() != event.method() || order.getPaymentMethod() != event.method()) return Result.INVALID;
    if (payment.getAmount().compareTo(event.amount()) != 0 || order.getTotalAmount().compareTo(event.amount()) != 0) return Result.AMOUNT_MISMATCH;
    if (event.pending()) return Result.OK;
    String external = event.method().name() + ":" + event.transactionId();
    if (event.paid() && (event.transactionId() == null || !event.transactionId().matches("[A-Za-z0-9_-]{1,150}") || "0".equals(event.transactionId()))) return Result.INVALID;
    if (Set.of(PaymentStatus.PAID, PaymentStatus.REFUND_PENDING, PaymentStatus.REFUNDED).contains(payment.getStatus()))
      return !event.paid() || external.equals(payment.getExternalKey()) ? Result.ALREADY : Result.INVALID;
    if (event.paid()) {
      payment.setExternalKey(external);
      PaymentStatus status = order.getOrderStatus() == OrderStatus.CANCELLED ? PaymentStatus.REFUND_PENDING : PaymentStatus.PAID;
      payment.setStatus(status); order.setPaymentStatus(status);
      if (status == PaymentStatus.PAID) inventory.commit(order);
      audit.record("PAYMENT_VERIFIED", "ORDER", order.getOrderId(), null, Map.of("method", event.method(), "amount", event.amount(), "reference", payment.getReference(), "status", status));
    } else {
      payment.setStatus(PaymentStatus.FAILED); order.setPaymentStatus(PaymentStatus.FAILED);
    }
    payments.flush();
    return Result.OK;
  }
  @Transactional
  public View confirmTransfer(Long orderId, BigDecimal amount, String reference) {
    Order order = found(orders.lock(orderId), "ORDER_NOT_FOUND");
    require(PaymentSettings.bank(order.getPaymentMethod()), "PAYMENT_INVALID_METHOD", "Chỉ đối soát thủ công chuyển khoản ngân hàng/VietQR.");
    bad(order.getTotalAmount().compareTo(amount) == 0, "Số tiền đối soát phải bằng tổng đơn hàng.");
    String key = "BANK:" + reference.trim().toUpperCase(Locale.ROOT);
    ShopPayment payment = prepare(order);
    if (payment.getExternalKey() != null) {
      require(key.equals(payment.getExternalKey()), "PAYMENT_ALREADY_CONFIRMED", "Đơn đã có giao dịch được xác nhận.");
      return view(order, payment, null);
    }
    require(order.getPaymentStatus() == PaymentStatus.UNPAID || order.getPaymentStatus() == PaymentStatus.FAILED,
        "PAYMENT_INVALID_STATUS", "Đơn đã được thanh toán hoặc hoàn tiền.");
    payment.setExternalKey(key);
    var status = order.getOrderStatus() == OrderStatus.CANCELLED ? PaymentStatus.REFUND_PENDING : PaymentStatus.PAID;
    payment.setStatus(status); order.setPaymentStatus(status);
      if (status == PaymentStatus.PAID) inventory.commit(order);
    payments.flush();
    audit.record("BANK_PAYMENT_CONFIRMED", "ORDER", orderId, null, Map.of("amount", amount, "reference", reference, "status", status));
    return view(order, payment, null);
  }
  @Transactional
  public View confirmRefund(Long orderId, BigDecimal amount, String reference) {
    Order order = found(orders.lock(orderId), "ORDER_NOT_FOUND");
    bad(order.getTotalAmount().compareTo(amount) == 0, "Số tiền hoàn phải bằng tổng đơn hàng.");
    ShopPayment payment = prepare(order);
    String key = order.getPaymentMethod().name() + ":" + reference.trim().toUpperCase(Locale.ROOT);
    if (order.getPaymentStatus() == PaymentStatus.REFUNDED && key.equals(payment.getRefundKey())) return view(order, payment, null);
    require(order.getPaymentStatus() == PaymentStatus.REFUND_PENDING, "PAYMENT_INVALID_STATUS", "Đơn không chờ hoàn tiền.");
    payment.setRefundKey(key); payment.setStatus(PaymentStatus.REFUNDED); order.setPaymentStatus(PaymentStatus.REFUNDED);
    inventory.release(order);
    payments.flush();
    audit.record("PAYMENT_REFUND_CONFIRMED", "ORDER", orderId, null, Map.of("amount", amount, "reference", reference));
    return view(order, payment, null);
  }
}
