package com.badminton.order.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.cart.entity.*;
import com.badminton.cart.repository.*;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.order.dto.request.*;
import com.badminton.order.dto.response.*;
import com.badminton.order.entity.*;
import com.badminton.order.mapper.*;
import com.badminton.order.repository.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.ProductMapper;
import com.badminton.product.repository.ProductRepository;
import com.badminton.user.repository.AddressRepository;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
  private final OrderRepository orders;
  private final OrderItemRepository lines;
  private final CartRepository carts;
  private final CartItemRepository cartItems;
  private final ProductRepository products;
  private final com.badminton.product.repository.ProductVariantRepository variants;
  private final AddressRepository addresses;
  private final Access access;
  private final com.badminton.payment.PaymentSettings paymentSettings;
  private final com.badminton.payment.PaymentRepository payments;
  private final com.badminton.review.repository.ReviewRepository reviews;
  private final com.badminton.audit.service.AuditService audit;
  private final OrderInventoryService inventory;

  public OrderService(
      OrderRepository orders,
      OrderItemRepository lines,
      CartRepository carts,
      CartItemRepository cartItems,
      ProductRepository products,
      com.badminton.product.repository.ProductVariantRepository variants,
      AddressRepository addresses,
      Access access,
      com.badminton.payment.PaymentSettings paymentSettings,
      com.badminton.payment.PaymentRepository payments,
      com.badminton.review.repository.ReviewRepository reviews,
      com.badminton.audit.service.AuditService audit,
      OrderInventoryService inventory) {
    this.orders = orders;
    this.lines = lines;
    this.carts = carts;
    this.cartItems = cartItems;
    this.products = products;
    this.variants = variants;
    this.addresses = addresses;
    this.access = access;
    this.paymentSettings = paymentSettings;
    this.payments = payments;
    this.reviews = reviews;
    this.audit = audit;
    this.inventory = inventory;
  }

  @org.springframework.beans.factory.annotation.Value("${app.shop.shipping-fee:0}")
  private BigDecimal shippingFee;

  @Transactional(readOnly = true)
  public OrderDetail detail(Long id) {
    Order o = found(orders.findById(id), "ORDER_NOT_FOUND");
    access.ownerOrAdmin(o.getUser().getUserId());
    return view(o);
  }

  public OrderDetail view(Order o) {
    return new OrderDetail(
        OrderMapper.toResponse(o),
        lines.findByOrderOrderIdOrderByProductProductIdAsc(o.getOrderId()).stream()
            .map(OrderItemMapper::toResponse)
            .toList());
  }

  @Transactional(readOnly = true)
  public PageResponse<OrderResponse> list(
      boolean admin, OrderStatus status, LocalDate date, int page, int size) {
    org.springframework.data.jpa.domain.Specification<Order> filter =
        admin ? all() : eq("user.userId", access.current().id());
    if (status != null) filter = filter.and(eq("orderStatus", status));
    if (date != null) {
      var vietnam = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
      var from = date.atStartOfDay(vietnam).withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime();
      var until = date.plusDays(1).atStartOfDay(vietnam).withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime();
      filter = filter.and((root, query, builder) -> builder.and(
          builder.greaterThanOrEqualTo(root.get("createdAt"), from),
          builder.lessThan(root.get("createdAt"), until)));
    }
    return PageResponse.from(
        orders
            .findAll(filter, page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(OrderMapper::toResponse));
  }

  public OrderDetail checkout(CheckoutRequest r) {
    var u = access.lockUser();
    Cart c = found(carts.findByUserUserId(u.getUserId()), "CART_EMPTY");
    var rows = cartItems.findByCartCartIdOrderByProductProductIdAsc(c.getCartId());
    require(!rows.isEmpty(), "CART_EMPTY", "Giỏ hàng trống");
    var address =
        found(
            addresses.findByAddressIdAndUserUserId(r.addressId(), u.getUserId()),
            "ADDRESS_NOT_FOUND");
    paymentSettings.requireEnabled(r.paymentMethod());
    BigDecimal subtotal = BigDecimal.ZERO;
    var locked = new ArrayList<Product>();
    var demandByProduct = new HashMap<Long,Integer>();
    for(var row:rows)demandByProduct.merge(row.getProduct().getProductId(),row.getQuantity(),Integer::sum);
    for (var row : rows) {
      Product p = found(products.lock(row.getProduct().getProductId()), "PRODUCT_NOT_FOUND");
      require(
          p.getStatus() == ProductStatus.ACTIVE && p.getStock() >= demandByProduct.get(p.getProductId()),
          "PRODUCT_OUT_OF_STOCK",
          "Sản phẩm không đủ số lượng");
      locked.add(p);
      if(row.getVariant()!=null){var v=found(variants.lock(row.getVariant().getVariantId()),"VARIANT_NOT_FOUND");require("ACTIVE".equals(v.getStatus())&&v.getStock()>=row.getQuantity(),"VARIANT_OUT_OF_STOCK","Biến thể không đủ tồn kho");}
      subtotal =
          subtotal.add(
              ProductMapper.effectivePrice(p).multiply(BigDecimal.valueOf(row.getQuantity())));
    }
    Order o = new Order();
    o.setUser(u);
    o.setOrderCode(code("ORD"));
    o.setReceiverName(address.getReceiverName());
    o.setReceiverPhone(address.getReceiverPhone());
    o.setShippingAddress(
        address.getDetailAddress()
            + ", "
            + address.getWard()
            + ", "
            + address.getProvince());
    o.setSubtotalAmount(money(subtotal));
    o.setShippingFee(money(shippingFee));
    o.setDiscountAmount(BigDecimal.ZERO);
    o.setTotalAmount(money(subtotal.add(shippingFee)));
    paymentSettings.validateAmount(r.paymentMethod(), o.getTotalAmount());
    o.setPaymentMethod(r.paymentMethod());
    o.setPaymentStatus(PaymentStatus.UNPAID);
    o.setOrderStatus(OrderStatus.PENDING);
    o.setNote(r.note());
    orders.saveAndFlush(o);
    for (int n = 0; n < rows.size(); n++) {
      var row = rows.get(n);
      var p = locked.get(n);
      OrderItem i = new OrderItem();
      i.setOrder(o);
      i.setProduct(p);
      i.setProductName(p.getProductName());
      i.setProductImage(p.getThumbnailUrl());
      if(row.getVariant()!=null){var v=found(variants.lock(row.getVariant().getVariantId()),"VARIANT_NOT_FOUND");i.setVariant(v);i.setVariantSku(v.getSku());i.setVariantColor(v.getColor());i.setVariantSize(v.getSize());}
      i.setQuantity(row.getQuantity());
      i.setUnitPrice(ProductMapper.effectivePrice(p));
      i.setTotalPrice(money(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))));
      lines.save(i);
    }
    cartItems.deleteAll(rows);
    lines.flush();
    return view(o);
  }

  public record ResetOrdersResult(int ordersDeleted, int orderItemsDeleted) {}

  /** Deletes only shop-order history; revenue is derived from these rows and resets with them. */
  public ResetOrdersResult resetAll() {
    var allOrders = orders.findAll();
    if (allOrders.isEmpty()) return new ResetOrdersResult(0, 0);
    var orderIds = allOrders.stream().map(Order::getOrderId).toList();
    int itemCount = 0;
    for (var order : allOrders) {
      itemCount += lines.findByOrderOrderIdOrderByProductProductIdAsc(order.getOrderId()).size();
      inventory.release(order);
    }
    reviews.deleteByOrderItemOrderOrderIdIn(orderIds);
    payments.deleteByOrderIdIn(orderIds);
    lines.deleteByOrderOrderIdIn(orderIds);
    // Derived deletes are queued by JPA; flush child rows before the bulk parent delete.
    lines.flush();
    orders.deleteAllByIdInBatch(orderIds);
    for (var orderId : orderIds) {
      audit.record("RESET_SHOP_ORDERS", "Order", orderId,
          Map.of("orders", orderIds.size(), "items", itemCount),
          Map.of("orders", 0, "shopRevenue", 0));
    }
    return new ResetOrdersResult(orderIds.size(), itemCount);
  }

  public OrderDetail cancel(Long id) {
    Order o = found(orders.lock(id), "ORDER_NOT_FOUND");
    access.owner(o.getUser().getUserId());
    require(
        o.getOrderStatus() == OrderStatus.PENDING || o.getOrderStatus() == OrderStatus.CONFIRMED,
        "ORDER_INVALID_STATUS",
        "Chỉ hủy trước giai đoạn chuẩn bị hàng");
    if (o.getPaymentStatus() == PaymentStatus.PAID) o.setPaymentStatus(PaymentStatus.REFUND_PENDING);
    o.setOrderStatus(OrderStatus.CANCELLED);
    return view(o);
  }

  public OrderDetail transition(Long id, OrderStatus next) {
    Order o = found(orders.lock(id), "ORDER_NOT_FOUND");
    OrderStatus expected =
        switch (o.getOrderStatus()) {
          case PENDING -> OrderStatus.CONFIRMED;
          case CONFIRMED -> OrderStatus.PREPARING;
          case PREPARING -> OrderStatus.SHIPPING;
          case SHIPPING -> OrderStatus.COMPLETED;
          default -> null;
        };
    require(
        expected == next, "ORDER_INVALID_STATUS", "Không thể bỏ qua hoặc lùi trạng thái đơn hàng");
    require(o.getPaymentMethod() == PaymentMethod.COD || next == OrderStatus.CONFIRMED || o.getPaymentStatus() == PaymentStatus.PAID,
        "PAYMENT_REQUIRED", "Cần xác nhận thanh toán trước khi chuẩn bị hoặc giao hàng.");
    o.setOrderStatus(next);
    if (next == OrderStatus.COMPLETED && o.getPaymentMethod() == PaymentMethod.COD && o.getPaymentStatus() == PaymentStatus.UNPAID) {
      o.setPaymentStatus(PaymentStatus.PAID);
      inventory.commit(o);
    }
    return view(o);
  }
}
