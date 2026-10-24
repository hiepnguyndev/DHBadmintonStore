package com.badminton.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.badminton.payment.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {
    "app.payments.sandbox=true", "app.payments.web-url=http://localhost:5173", "app.payments.public-api-url=https://shop.example.com",
    "app.payments.bank.enabled=true", "app.payments.bank.id=970436", "app.payments.bank.name=Test Bank",
    "app.payments.bank.account-number=0000000000", "app.payments.bank.account-name=TEST ONLY",
    "app.payments.vnpay.enabled=true", "app.payments.vnpay.tmn-code=TESTCODE", "app.payments.vnpay.hash-secret=test-vnp-secret",
    "app.payments.momo.enabled=true", "app.payments.momo.partner-code=MOMO_TEST", "app.payments.momo.access-key=test-access",
    "app.payments.momo.secret-key=test-momo-secret", "app.payments.zalopay.enabled=true", "app.payments.zalopay.app-id=2553",
    "app.payments.zalopay.key1=test-key1", "app.payments.zalopay.key2=test-key2"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentsMySqlIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @MockitoBean GatewayHttpClient http;
  String buyer, other, admin;
  long address, category, brand;
  record Purchase(long orderId, long productId) {}

  JsonNode request(String token, String method, String path, Object body, int status) throws Exception {
    var request = MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path);
    if (token != null) request.header("Authorization", "Bearer " + token);
    if (body != null) request.contentType("application/json").content(json.writeValueAsString(body));
    var response = mvc.perform(request).andReturn().getResponse();
    assertThat(response.getStatus()).as(method + " " + path + " " + response.getContentAsString()).isEqualTo(status);
    return response.getContentAsString().isBlank() ? json.nullNode() : json.readTree(response.getContentAsString());
  }
  JsonNode call(String token, String method, String path, Object body, int status) throws Exception {
    return request(token, method, path, body, status).path("data");
  }
  String account(String role) throws Exception {
    String email = "payments-" + UUID.randomUUID() + "@example.com";
    call(null, "POST", "/api/auth/register", Map.of("fullName", "Payments Test", "email", email,
        "password", "Password123!", "confirmPassword", "Password123!"), 201);
    jdbc.update("UPDATE users SET role=? WHERE email=?", role, email);
    return call(null, "POST", "/api/auth/login", Map.of("email", email, "password", "Password123!"), 200).path("accessToken").asText();
  }
  @BeforeAll void setup() throws Exception {
    admin = account("ADMIN"); buyer = account("USER"); other = account("USER");
    address = call(buyer, "POST", "/api/addresses", Map.of("receiverName", "Payments Test", "receiverPhone", "0901234567",
        "province", "HN", "ward", "ML", "detailAddress", "123 Test", "isDefault", true), 201).path("addressId").asLong();
    category = call(admin, "POST", "/api/categories", Map.of("categoryName", "Payment " + UUID.randomUUID()), 201).path("categoryId").asLong();
    brand = call(admin, "POST", "/api/brands", Map.of("brandName", "Payment " + UUID.randomUUID()), 201).path("brandId").asLong();
  }
  Purchase purchase(String method) throws Exception {
    long product = call(admin, "POST", "/api/products", Map.of("categoryId", category, "brandId", brand,
        "productName", "Payment " + UUID.randomUUID(), "price", 499000, "stock", 20, "status", "ACTIVE"), 201).path("id").asLong();
    call(buyer, "POST", "/api/cart/items", Map.of("productId", product, "quantity", 1), 200);
    var order = call(buyer, "POST", "/api/orders", Map.of("addressId", address, "paymentMethod", method), 201).path("order");
    assertThat(order.path("paymentStatus").asText()).isEqualTo("UNPAID");
    assertThat(jdbc.queryForObject("SELECT stock FROM products WHERE product_id=?", Integer.class, product)).isEqualTo(19);
    return new Purchase(order.path("orderId").asLong(), product);
  }
  JsonNode start(long order) throws Exception { return call(buyer, "POST", "/api/orders/" + order + "/payment", null, 200); }
  String paymentStatus(long order) { return jdbc.queryForObject("SELECT payment_status FROM orders WHERE order_id=?", String.class, order); }
  Map<String, String> vnpay(String reference, String amount, String transaction, boolean paid) {
    var params = new TreeMap<String, String>();
    params.put("vnp_TmnCode", "TESTCODE"); params.put("vnp_TxnRef", reference); params.put("vnp_Amount", amount);
    params.put("vnp_TransactionNo", transaction); params.put("vnp_ResponseCode", paid ? "00" : "24");
    params.put("vnp_TransactionStatus", paid ? "00" : "02");
    params.put("vnp_SecureHash", PaymentCrypto.hmac("HmacSHA512", "test-vnp-secret", PaymentCrypto.query(params)));
    return params;
  }
  JsonNode vnpayCallback(Map<String, String> params) throws Exception {
    var request = MockMvcRequestBuilders.get("/api/payments/callbacks/vnpay"); params.forEach(request::param);
    var response = mvc.perform(request).andReturn().getResponse();
    assertThat(response.getStatus()).isEqualTo(200);
    return json.readTree(response.getContentAsString());
  }
  @Test void bankTransferIsUnpaidUntilAdminReconcilesAndCannotReuseBankTransaction() throws Exception {
    var p = purchase("BANK_TRANSFER"); long id = p.orderId();
    call(null, "GET", "/api/orders/" + id + "/payment", null, 401);
    call(other, "GET", "/api/orders/" + id + "/payment", null, 403);
    call(other, "POST", "/api/orders/" + id + "/payment", null, 403);
    var payment = start(id);
    assertThat(payment.path("qrUrl").asText()).contains("970436-0000000000", "amount=499000", "addInfo=DH" + id);
    assertThat(payment.path("status").asText()).isEqualTo("UNPAID");
    assertThat(start(id).path("reference")).isEqualTo(payment.path("reference"));
    var confirmation = Map.of("amount", 499000, "reference", "BANK-" + UUID.randomUUID());
    String endpoint = "/api/admin/orders/" + id + "/payment/confirm";
    call(buyer, "POST", endpoint, confirmation, 403);
    call(admin, "POST", endpoint, Map.of("amount", 1, "reference", "WRONG"), 400);
    call(admin, "PATCH", "/api/admin/orders/" + id + "/status", Map.of("status", "CONFIRMED"), 200);
    call(admin, "PATCH", "/api/admin/orders/" + id + "/status", Map.of("status", "PREPARING"), 409);
    call(admin, "POST", endpoint, confirmation, 200);
    call(admin, "POST", endpoint, confirmation, 200);
    assertThat(paymentStatus(id)).isEqualTo("PAID");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_logs WHERE entity_id=? AND action='BANK_PAYMENT_CONFIRMED'", Integer.class, id)).isEqualTo(1);
    var second = purchase("VIETQR"); start(second.orderId());
    call(admin, "POST", "/api/admin/orders/" + second.orderId() + "/payment/confirm", confirmation, 409);
    assertThat(paymentStatus(second.orderId())).isEqualTo("UNPAID");
    call(buyer, "POST", "/api/orders/" + id + "/cancel", null, 200);
    assertThat(paymentStatus(id)).isEqualTo("REFUND_PENDING");
    assertThat(jdbc.queryForObject("SELECT stock FROM products WHERE product_id=?", Integer.class, p.productId())).isEqualTo(20);
    call(admin, "POST", "/api/admin/orders/" + id + "/payment/refund-confirm", Map.of("amount", 499000, "reference", "REFUND-" + UUID.randomUUID()), 200);
    assertThat(paymentStatus(id)).isEqualTo("REFUNDED");
  }
  @Test void signedCallbackChecksAmountRejectsTamperingAndIsIdempotentUnderConcurrency() throws Exception {
    var p = purchase("VNPAY"); var payment = start(p.orderId()); String reference = payment.path("reference").asText();
    var valid = vnpay(reference, "49900000", "VN" + p.orderId(), true);
    var forged = new TreeMap<>(valid); forged.put("vnp_Amount", "100");
    assertThat(vnpayCallback(forged).path("RspCode").asText()).isEqualTo("97");
    assertThat(vnpayCallback(vnpay(reference, "100", "VN" + p.orderId(), true)).path("RspCode").asText()).isEqualTo("04");
    assertThat(paymentStatus(p.orderId())).isEqualTo("UNPAID");
    var pool = Executors.newFixedThreadPool(2);
    try {
      var first = pool.submit(() -> vnpayCallback(valid).path("RspCode").asText());
      var second = pool.submit(() -> vnpayCallback(valid).path("RspCode").asText());
      assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder("00", "02");
    } finally { pool.shutdownNow(); }
    assertThat(paymentStatus(p.orderId())).isEqualTo("PAID");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_logs WHERE entity_id=? AND action='PAYMENT_VERIFIED'", Integer.class, p.orderId())).isEqualTo(1);
    assertThat(vnpayCallback(vnpay(reference, "49900000", "VN" + p.orderId(), false)).path("RspCode").asText()).isEqualTo("02");
    assertThat(paymentStatus(p.orderId())).isEqualTo("PAID");
  }
  @Test void latePaymentAfterCancellationRequiresActualRefundAndDoesNotReserveStockAgain() throws Exception {
    var p = purchase("VNPAY"); String reference = start(p.orderId()).path("reference").asText();
    call(buyer, "POST", "/api/orders/" + p.orderId() + "/cancel", null, 200);
    assertThat(vnpayCallback(vnpay(reference, "49900000", "LATE" + p.orderId(), true)).path("RspCode").asText()).isEqualTo("00");
    assertThat(paymentStatus(p.orderId())).isEqualTo("REFUND_PENDING");
    assertThat(jdbc.queryForObject("SELECT stock FROM products WHERE product_id=?", Integer.class, p.productId())).isEqualTo(20);
    assertThat(call(buyer, "GET", "/api/orders/" + p.orderId() + "/payment", null, 200).path("checkoutUrl").isNull()).isTrue();
    call(buyer, "POST", "/api/orders/" + p.orderId() + "/payment", null, 409);
  }
  @Test void momoRequiresSignedNotificationAndAuthorizationOnlyIsNotPaid() throws Exception {
    when(http.post(contains("momo.vn"), anyMap(), eq(false))).thenAnswer(invocation -> {
      Map<String, Object> body = invocation.getArgument(1);
      return json.valueToTree(Map.of("resultCode", 0, "orderId", body.get("orderId"), "requestId", body.get("requestId"),
          "amount", body.get("amount"), "partnerCode", "MOMO_TEST", "payUrl", "https://test-payment.momo.vn/pay?id=123"));
    });
    var p = purchase("MOMO"); String reference = start(p.orderId()).path("reference").asText();
    var body = new TreeMap<String, Object>();
    body.put("partnerCode", "MOMO_TEST"); body.put("orderId", reference); body.put("requestId", reference); body.put("amount", 499000);
    body.put("extraData", ""); body.put("message", "Success"); body.put("orderInfo", "Thanh toan DH" + p.orderId());
    body.put("orderType", "momo_wallet"); body.put("payType", "qr"); body.put("responseTime", 1791853200000L);
    body.put("resultCode", 9000); body.put("transId", 123456789L + p.orderId());
    signMomo(body); body.put("amount", 1);
    request(null, "POST", "/api/payments/callbacks/momo", body, 400);
    body.put("amount", 499000); signMomo(body);
    request(null, "POST", "/api/payments/callbacks/momo", body, 204);
    assertThat(paymentStatus(p.orderId())).isEqualTo("UNPAID");
    body.put("resultCode", 0); signMomo(body);
    request(null, "POST", "/api/payments/callbacks/momo", body, 204);
    request(null, "POST", "/api/payments/callbacks/momo", body, 204);
    assertThat(paymentStatus(p.orderId())).isEqualTo("PAID");
  }
  void signMomo(Map<String, Object> body) {
    String raw = "accessKey=test-access";
    for (String key : List.of("amount", "extraData", "message", "orderId", "orderInfo", "orderType", "partnerCode", "payType", "requestId", "responseTime", "resultCode", "transId")) raw += "&" + key + "=" + body.get(key);
    body.put("signature", PaymentCrypto.hmac("HmacSHA256", "test-momo-secret", raw));
  }
  @Test void zalopayVerifiesRawDataTypeAndMerchant() throws Exception {
    when(http.post(contains("zalopay.vn"), anyMap(), eq(true))).thenReturn(json.readTree("{\"return_code\":1,\"order_url\":\"https://sbgateway.zalopay.vn/pay\"}"));
    var p = purchase("ZALOPAY"); String reference = start(p.orderId()).path("reference").asText();
    assertThat(reference).matches("[0-9]{6}_[a-f0-9]{32}");
    String data = json.writeValueAsString(Map.of("app_id", 2553, "app_trans_id", reference, "amount", 499000, "zp_trans_id", 987654321L + p.orderId()));
    var body = new HashMap<String, Object>(Map.of("type", 1, "data", data, "mac", "fake"));
    assertThat(request(null, "POST", "/api/payments/callbacks/zalopay", body, 200).path("return_code").asInt()).isEqualTo(-1);
    body.put("mac", PaymentCrypto.hmac("HmacSHA256", "test-key2", data));
    assertThat(request(null, "POST", "/api/payments/callbacks/zalopay", body, 200).path("return_code").asInt()).isEqualTo(1);
    assertThat(request(null, "POST", "/api/payments/callbacks/zalopay", body, 200).path("return_code").asInt()).isEqualTo(1);
    assertThat(paymentStatus(p.orderId())).isEqualTo("PAID");
  }
  @Test void providerTimeoutKeepsOneReferenceAndNeverMarksPaid() throws Exception {
    when(http.post(anyString(), anyMap(), anyBoolean())).thenThrow(new IllegalStateException("Timeout"));
    var p = purchase("MOMO"); var first = start(p.orderId()); var second = start(p.orderId());
    assertThat(first.path("message").asText()).isNotBlank();
    assertThat(first.path("reference")).isEqualTo(second.path("reference"));
    assertThat(paymentStatus(p.orderId())).isEqualTo("UNPAID");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM shop_payments WHERE order_id=?", Integer.class, p.orderId())).isEqualTo(1);
  }
  @Test void expiredSessionDoesNotCreateAnotherChargeAndAdminCannotFakeGatewaySuccess() throws Exception {
    var p = purchase("VNPAY"); String reference = start(p.orderId()).path("reference").asText();
    jdbc.update("UPDATE shop_payments SET expires_at=UTC_TIMESTAMP()-INTERVAL 1 MINUTE WHERE order_id=?", p.orderId());
    var expired = start(p.orderId());
    assertThat(expired.path("expired").asBoolean()).isTrue();
    assertThat(expired.path("checkoutUrl").isNull()).isTrue();
    assertThat(expired.path("reference").asText()).isEqualTo(reference);
    call(admin, "POST", "/api/admin/orders/" + p.orderId() + "/payment/confirm", Map.of("amount", 499000, "reference", "FAKE-GATEWAY"), 409);
    assertThat(paymentStatus(p.orderId())).isEqualTo("UNPAID");
  }
  @Test void methodsArePublicWithoutKeysAndCodStillCompletesNormally() throws Exception {
    var methods = call(null, "GET", "/api/payments/methods", null, 200);
    assertThat(methods.size()).isEqualTo(6);
    assertThat(methods.toString()).doesNotContain("secret", "test-access", "account-number");
    var p = purchase("COD");
    for (var status : List.of("CONFIRMED", "PREPARING", "SHIPPING", "COMPLETED"))
      call(admin, "PATCH", "/api/admin/orders/" + p.orderId() + "/status", Map.of("status", status), 200);
    assertThat(paymentStatus(p.orderId())).isEqualTo("PAID");
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM shop_payments WHERE order_id=?", Integer.class, p.orderId())).isZero();
  }
}
