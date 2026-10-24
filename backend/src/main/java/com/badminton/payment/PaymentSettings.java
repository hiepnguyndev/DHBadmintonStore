package com.badminton.payment;

import static com.badminton.common.support.Checks.*;
import com.badminton.order.entity.PaymentMethod;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PaymentSettings {
  private final Environment environment;
  public PaymentSettings(Environment environment) { this.environment = environment; }
  public String get(String key) { return environment.getProperty("app.payments." + key, "").trim(); }
  public boolean sandbox() { return environment.getProperty("app.payments.sandbox", Boolean.class, true); }
  public boolean flag(String key) { return Boolean.parseBoolean(get(key)); }
  public boolean sepayEnabled() { return flag("sepay.enabled") && !get("sepay.webhook-secret").isBlank(); }
  public String sepaySecret() { return get("sepay.webhook-secret"); }
  public static boolean bank(PaymentMethod method) { return method == PaymentMethod.BANK_TRANSFER || method == PaymentMethod.VIETQR; }
  private boolean filled(String... keys) { return Arrays.stream(keys).allMatch(k -> !get(k).isBlank()); }
  private boolean https(String key) {
    try { var uri = URI.create(get(key)); return "https".equals(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null; }
    catch (IllegalArgumentException e) { return false; }
  }
  public boolean configured(PaymentMethod method) {
    if (method == PaymentMethod.COD) return true;
    if (bank(method)) return filled("bank.name", "bank.account-name")
        && get("bank.id").matches("[A-Za-z0-9]{2,30}") && get("bank.account-number").matches("[A-Za-z0-9]{1,19}");
    return switch (method) {
      case VNPAY -> filled("vnpay.tmn-code", "vnpay.hash-secret");
      case MOMO -> filled("momo.partner-code", "momo.access-key", "momo.secret-key");
      case ZALOPAY -> get("zalopay.app-id").matches("[1-9][0-9]{0,8}") && filled("zalopay.key1", "zalopay.key2");
      default -> false;
    };
  }
  public boolean enabled(PaymentMethod method) {
    if (method != PaymentMethod.COD && !bank(method)) return false;
    if (method == PaymentMethod.COD) return true;
    if (!configured(method)) return false;
    if (bank(method)) return flag("bank.enabled");
    return false;
  }
  public record BankDetails(
      String bankName, String accountNumber, String accountName, boolean available) {}
  public BankDetails bankDetails() {
    return new BankDetails(
        get("bank.name"),
        get("bank.account-number"),
        get("bank.account-name"),
        enabled(PaymentMethod.BANK_TRANSFER));
  }
  public void requireEnabled(PaymentMethod method) {
    require(enabled(method), "PAYMENT_UNAVAILABLE", "Phương thức thanh toán tạm chưa khả dụng.");
  }
  public void validateAmount(PaymentMethod method, BigDecimal amount) {
    if (method == PaymentMethod.COD) return;
    bad(amount.signum() > 0 && amount.stripTrailingZeros().scale() <= 0 && amount.compareTo(new BigDecimal("9999999999")) <= 0,
        "Thanh toán trả trước cần số tiền nguyên VND, lớn hơn 0 và không quá 9.999.999.999 VND.");
    if (method == PaymentMethod.MOMO) bad(amount.compareTo(new BigDecimal("1000")) >= 0 && amount.compareTo(new BigDecimal("50000000")) <= 0,
        "MoMo hỗ trợ từ 1.000 đến 50.000.000 VND.");
  }
  public String returnUrl(Long orderId) { return get("web-url").replaceAll("/+$", "") + "/orders/" + orderId; }
  public String callback(String provider) { return get("public-api-url").replaceAll("/+$", "") + "/api/payments/callbacks/" + provider; }
  public record MethodOption(PaymentMethod code, String name, boolean available, boolean sandbox) {}
  public List<MethodOption> methods() {
    return Arrays.stream(PaymentMethod.values()).filter(m -> m == PaymentMethod.COD || bank(m)).map(m -> new MethodOption(m, switch (m) {
      case COD -> "Thanh toán khi nhận hàng";
      case BANK_TRANSFER -> "Chuyển khoản ngân hàng";
      case VIETQR -> "VietQR";
      case VNPAY -> "VNPAY";
      case MOMO -> "MoMo";
      case ZALOPAY -> "ZaloPay";
    }, enabled(m), !bank(m) && m != PaymentMethod.COD && sandbox())).toList();
  }
}
