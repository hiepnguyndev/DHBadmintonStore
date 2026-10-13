package com.badminton.payment;

import static com.badminton.payment.PaymentCrypto.*;
import com.badminton.order.entity.PaymentMethod;
import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {
  private final PaymentSettings settings;
  private final GatewayHttpClient http;
  private final ObjectMapper json;
  public PaymentGateway(PaymentSettings settings, GatewayHttpClient http, ObjectMapper json) {
    this.settings = settings; this.http = http; this.json = json;
  }
  private String value(String key) { return settings.get(key); }
  private String info(ShopPayment payment) { return "Thanh toan DH" + payment.getOrderId(); }
  private String vnpDate(LocalDateTime utc) {
    return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }
  public String create(ShopPayment payment, String ipAddress) {
    return switch (payment.getMethod()) {
      case VNPAY -> vnpay(payment, ipAddress);
      case MOMO -> momo(payment);
      case ZALOPAY -> zalopay(payment);
      default -> throw new IllegalArgumentException("Not an online gateway");
    };
  }
  private String vnpay(ShopPayment payment, String ipAddress) {
    var params = new TreeMap<String, String>();
    params.put("vnp_Version", "2.1.0"); params.put("vnp_Command", "pay");
    params.put("vnp_TmnCode", value("vnpay.tmn-code"));
    params.put("vnp_Amount", payment.getAmount().movePointRight(2).toBigIntegerExact().toString());
    params.put("vnp_CurrCode", "VND"); params.put("vnp_TxnRef", payment.getReference());
    params.put("vnp_OrderInfo", info(payment)); params.put("vnp_OrderType", "other");
    params.put("vnp_Locale", "vn"); params.put("vnp_ReturnUrl", settings.returnUrl(payment.getOrderId()));
    params.put("vnp_IpAddr", ipAddress); params.put("vnp_CreateDate", vnpDate(payment.getCreatedAt()));
    params.put("vnp_ExpireDate", vnpDate(payment.getExpiresAt()));
    String query = query(params);
    return (settings.sandbox() ? "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" : "https://pay.vnpay.vn/vpcpay.html")
        + "?" + query + "&vnp_SecureHash=" + hmac("HmacSHA512", value("vnpay.hash-secret"), query);
  }
  private String momo(ShopPayment payment) {
    var body = new LinkedHashMap<String, Object>();
    body.put("partnerCode", value("momo.partner-code")); body.put("requestId", payment.getReference());
    body.put("orderId", payment.getReference()); body.put("amount", payment.getAmount().longValueExact());
    body.put("orderInfo", info(payment)); body.put("redirectUrl", settings.returnUrl(payment.getOrderId()));
    body.put("ipnUrl", settings.callback("momo")); body.put("requestType", "captureWallet");
    body.put("extraData", ""); body.put("autoCapture", true); body.put("lang", "vi");
    String raw = "accessKey=" + value("momo.access-key") + "&" + raw(body,
        "amount", "extraData", "ipnUrl", "orderId", "orderInfo", "partnerCode", "redirectUrl", "requestId", "requestType");
    body.put("signature", hmac("HmacSHA256", value("momo.secret-key"), raw));
    JsonNode result = http.post((settings.sandbox() ? "https://test-payment.momo.vn" : "https://payment.momo.vn") + "/v2/gateway/api/create", body, false);
    if (result.path("resultCode").asInt(-1) != 0 || !payment.getReference().equals(result.path("orderId").asText())
        || !payment.getReference().equals(result.path("requestId").asText())
        || !value("momo.partner-code").equals(result.path("partnerCode").asText())
        || payment.getAmount().compareTo(result.path("amount").decimalValue()) != 0)
      throw new IllegalStateException("Cannot create MoMo payment");
    return safeUrl(result.path("payUrl").asText(), "momo.vn");
  }
  private String zalopay(ShopPayment payment) {
    var body = new LinkedHashMap<String, Object>();
    body.put("app_id", Integer.parseInt(value("zalopay.app-id")));
    body.put("app_trans_id", payment.getReference()); body.put("app_user", "DH" + payment.getOrderId());
    body.put("amount", payment.getAmount().longValueExact());
    body.put("app_time", payment.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    body.put("description", info(payment)); body.put("item", "[]"); body.put("bank_code", "");
    body.put("expire_duration_seconds", 900); body.put("callback_url", settings.callback("zalopay"));
    try { body.put("embed_data", json.writeValueAsString(Map.of("redirecturl", settings.returnUrl(payment.getOrderId())))); }
    catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
    String raw = List.of("app_id", "app_trans_id", "app_user", "amount", "app_time", "embed_data", "item")
        .stream().map(k -> String.valueOf(body.get(k))).collect(Collectors.joining("|"));
    body.put("mac", hmac("HmacSHA256", value("zalopay.key1"), raw));
    JsonNode result = http.post((settings.sandbox() ? "https://sb-openapi.zalopay.vn" : "https://openapi.zalopay.vn") + "/v2/create", body, true);
    if (result.path("return_code").asInt(-1) != 1) throw new IllegalStateException("Cannot create ZaloPay payment");
    return safeUrl(result.path("order_url").asText(), "zalopay.vn");
  }
  private static String safeUrl(String value, String domain) {
    URI uri = URI.create(value);
    if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
        || !(uri.getHost().equals(domain) || uri.getHost().endsWith("." + domain)))
      throw new IllegalArgumentException("Invalid payment redirect");
    return value;
  }
  private static String raw(Map<String, ?> values, String... fields) {
    return Arrays.stream(fields).map(k -> k + "=" + Objects.toString(values.get(k), "")).collect(Collectors.joining("&"));
  }
  private static void valid(boolean value) { if (!value) throw new IllegalArgumentException("Invalid payment notification"); }
  public record Event(PaymentMethod method, String reference, BigDecimal amount, String transactionId, boolean paid, boolean pending) {}
  public Event verifyVnpay(Map<String, String> params) {
    valid(settings.configured(PaymentMethod.VNPAY));
    var signed = new TreeMap<String, String>();
    params.forEach((k, v) -> { if (k.startsWith("vnp_") && !Set.of("vnp_SecureHash", "vnp_SecureHashType").contains(k)) signed.put(k, v); });
    valid(matches(hmac("HmacSHA512", value("vnpay.hash-secret"), query(signed)), params.get("vnp_SecureHash")));
    valid(value("vnpay.tmn-code").equals(params.get("vnp_TmnCode")));
    valid(!params.containsKey("vnp_CurrCode") || "VND".equals(params.get("vnp_CurrCode")));
    valid(params.getOrDefault("vnp_Amount", "").matches("[0-9]{1,15}"));
    boolean paid = "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus"));
    return new Event(PaymentMethod.VNPAY, params.get("vnp_TxnRef"), new BigDecimal(params.get("vnp_Amount")).movePointLeft(2),
        params.get("vnp_TransactionNo"), paid, false);
  }
  public Event verifyMomo(JsonNode node) {
    valid(settings.configured(PaymentMethod.MOMO) && node.isObject());
    var values = new LinkedHashMap<String, String>(); node.fields().forEachRemaining(e -> values.put(e.getKey(), e.getValue().asText()));
    String raw = "accessKey=" + value("momo.access-key") + "&" + raw(values, "amount", "extraData", "message", "orderId", "orderInfo",
        "orderType", "partnerCode", "payType", "requestId", "responseTime", "resultCode", "transId");
    valid(matches(hmac("HmacSHA256", value("momo.secret-key"), raw), values.get("signature")));
    valid(value("momo.partner-code").equals(values.get("partnerCode")) && Objects.equals(values.get("orderId"), values.get("requestId")));
    valid(node.path("amount").isIntegralNumber() && node.path("resultCode").isIntegralNumber());
    return new Event(PaymentMethod.MOMO, values.get("orderId"), node.get("amount").decimalValue(), values.get("transId"),
        node.get("resultCode").asInt() == 0, node.get("resultCode").asInt() == 9000);
  }
  public Event verifyZalopay(JsonNode node) {
    valid(settings.configured(PaymentMethod.ZALOPAY) && node.path("type").asInt() == 1 && node.path("data").isTextual());
    String data = node.get("data").asText();
    valid(matches(hmac("HmacSHA256", value("zalopay.key2"), data), node.path("mac").asText()));
    try {
      JsonNode parsed = json.readTree(data);
      valid(value("zalopay.app-id").equals(parsed.path("app_id").asText()) && parsed.path("amount").isIntegralNumber());
      return new Event(PaymentMethod.ZALOPAY, parsed.path("app_trans_id").asText(), parsed.path("amount").decimalValue(),
          parsed.path("zp_trans_id").asText(), true, false);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalArgumentException("Invalid payment notification"); }
  }
}
