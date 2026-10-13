package com.badminton.payment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.badminton.order.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;

class PaymentGatewayTest {
  MockEnvironment env;
  PaymentSettings settings;
  GatewayHttpClient http;
  PaymentGateway gateway;
  ObjectMapper json = new ObjectMapper();
  @BeforeEach void setup() {
    env = new MockEnvironment().withProperty("app.payments.sandbox", "true")
        .withProperty("app.payments.web-url", "http://localhost:5173")
        .withProperty("app.payments.public-api-url", "https://shop.example.com")
        .withProperty("app.payments.vnpay.enabled", "true").withProperty("app.payments.vnpay.tmn-code", "TESTCODE")
        .withProperty("app.payments.vnpay.hash-secret", "test-vnp-secret")
        .withProperty("app.payments.momo.enabled", "true").withProperty("app.payments.momo.partner-code", "MOMO_TEST")
        .withProperty("app.payments.momo.access-key", "test-access").withProperty("app.payments.momo.secret-key", "test-momo-secret")
        .withProperty("app.payments.zalopay.enabled", "true").withProperty("app.payments.zalopay.app-id", "2553")
        .withProperty("app.payments.zalopay.key1", "test-key1").withProperty("app.payments.zalopay.key2", "test-key2");
    settings = new PaymentSettings(env); http = mock(GatewayHttpClient.class); gateway = new PaymentGateway(settings, http, json);
  }
  ShopPayment payment(PaymentMethod method) {
    var p = new ShopPayment(); p.setOrderId(12L); p.setMethod(method); p.setAmount(new BigDecimal("499000"));
    p.setReference("261013_abcdef123456789");
    org.springframework.test.util.ReflectionTestUtils.setField(p, "createdAt", LocalDateTime.of(2026, 10, 12, 20, 0));
    p.setExpiresAt(p.getCreatedAt().plusMinutes(15)); return p;
  }
  @Test void usesKnownHmacVectorAndRejectsMalformedSignature() {
    assertThat(PaymentCrypto.hmac("HmacSHA256", "key", "The quick brown fox jumps over the lazy dog"))
        .isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
    assertThat(PaymentCrypto.matches("abcd", "abcd")).isTrue();
    assertThat(PaymentCrypto.matches("abcd", "ABCD")).isTrue();
    assertThat(PaymentCrypto.matches("abcd", "not-hex")).isFalse();
    assertThat(PaymentCrypto.matches("abcd", null)).isFalse();
  }
  @Test void vnpaySignsExactAmountVietnamTimeAndFixedReturnAddress() {
    String url = gateway.create(payment(PaymentMethod.VNPAY), "127.0.0.1");
    assertThat(url).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
    var params = new TreeMap<String, String>();
    for (var field : URI.create(url).getRawQuery().split("&")) {
      var pair = field.split("=", 2); params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
    }
    String signature = params.remove("vnp_SecureHash");
    assertThat(params).containsEntry("vnp_Amount", "49900000").containsEntry("vnp_CreateDate", "20261013030000")
        .containsEntry("vnp_ExpireDate", "20261013031500").containsEntry("vnp_ReturnUrl", "http://localhost:5173/orders/12");
    assertThat(signature).isEqualTo(PaymentCrypto.hmac("HmacSHA512", "test-vnp-secret", PaymentCrypto.query(params)));
    verifyNoInteractions(http);
  }
  @Test void momoCreatesStableIdempotentRequestAndRejectsForeignRedirect() throws Exception {
    var p = payment(PaymentMethod.MOMO);
    when(http.post(anyString(), anyMap(), eq(false))).thenAnswer(invocation -> json.valueToTree(Map.of(
        "resultCode", 0, "orderId", p.getReference(), "requestId", p.getReference(), "amount", 499000,
        "partnerCode", "MOMO_TEST", "payUrl", "https://test-payment.momo.vn/pay?id=123")));
    assertThat(gateway.create(p, "127.0.0.1")).startsWith("https://test-payment.momo.vn/");
    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(http).post(eq("https://test-payment.momo.vn/v2/gateway/api/create"), captor.capture(), eq(false));
    var body = captor.getValue();
    assertThat(body).containsEntry("requestId", p.getReference()).containsEntry("orderId", p.getReference())
        .containsEntry("autoCapture", true).containsEntry("amount", 499000L);
    String raw = "accessKey=test-access&amount=499000&extraData=&ipnUrl=https://shop.example.com/api/payments/callbacks/momo"
        + "&orderId=" + p.getReference() + "&orderInfo=Thanh toan DH12&partnerCode=MOMO_TEST&redirectUrl=http://localhost:5173/orders/12"
        + "&requestId=" + p.getReference() + "&requestType=captureWallet";
    assertThat(body.get("signature")).isEqualTo(PaymentCrypto.hmac("HmacSHA256", "test-momo-secret", raw));
    when(http.post(anyString(), anyMap(), eq(false))).thenReturn(json.readTree("{\"resultCode\":0,\"orderId\":\"" + p.getReference()
        + "\",\"requestId\":\"" + p.getReference() + "\",\"amount\":499000,\"partnerCode\":\"MOMO_TEST\",\"payUrl\":\"https://momo.vn.attacker.invalid/pay\"}"));
    assertThatThrownBy(() -> gateway.create(p, "127.0.0.1")).isInstanceOf(IllegalArgumentException.class);
  }
  @Test void zalopaySignsCreateWithKeyOneAndRawCallbackWithKeyTwo() throws Exception {
    when(http.post(anyString(), anyMap(), eq(true))).thenReturn(json.readTree("{\"return_code\":1,\"order_url\":\"https://sbgateway.zalopay.vn/pay\"}"));
    var p = payment(PaymentMethod.ZALOPAY);
    gateway.create(p, "127.0.0.1");
    ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
    verify(http).post(eq("https://sb-openapi.zalopay.vn/v2/create"), captor.capture(), eq(true));
    var body = captor.getValue();
    String signed = "2553|" + p.getReference() + "|DH12|499000|" + p.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        + "|{\"redirecturl\":\"http://localhost:5173/orders/12\"}|[]";
    assertThat(body.get("mac")).isEqualTo(PaymentCrypto.hmac("HmacSHA256", "test-key1", signed));
    String data = "{\"app_id\":2553,\"app_trans_id\":\"ref\",\"amount\":499000,\"zp_trans_id\":123}";
    var callback = json.createObjectNode().put("type", 1).put("data", data).put("mac", PaymentCrypto.hmac("HmacSHA256", "test-key2", data));
    assertThat(gateway.verifyZalopay(callback).paid()).isTrue();
    callback.put("data", data.replace("499000", "1"));
    assertThatThrownBy(() -> gateway.verifyZalopay(callback)).isInstanceOf(IllegalArgumentException.class);
  }
  @Test void missingConfigurationDisabledAndAmountValidationEnforced() {
    var empty = new PaymentSettings(new MockEnvironment());
    assertThat(empty.methods()).filteredOn(PaymentSettings.MethodOption::available).extracting(PaymentSettings.MethodOption::code).containsExactly(PaymentMethod.COD);
    assertThatThrownBy(() -> settings.validateAmount(PaymentMethod.MOMO, new BigDecimal("999"))).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> settings.validateAmount(PaymentMethod.VIETQR, new BigDecimal("1000.5"))).isInstanceOf(RuntimeException.class);
    settings.validateAmount(PaymentMethod.MOMO, new BigDecimal("1000.00"));
    env.setProperty("app.payments.sandbox", "false");
    assertThat(settings.enabled(PaymentMethod.VNPAY)).isFalse();
    env.setProperty("app.payments.web-url", "https://shop.example.com");
    assertThat(settings.enabled(PaymentMethod.VNPAY)).isFalse();
    assertThat(settings.enabled(PaymentMethod.MOMO)).isFalse();
    assertThat(settings.enabled(PaymentMethod.ZALOPAY)).isFalse();
    assertThat(settings.methods()).extracting(PaymentSettings.MethodOption::code)
        .containsExactly(PaymentMethod.COD, PaymentMethod.BANK_TRANSFER, PaymentMethod.VIETQR);
    env.setProperty("app.payments.vnpay.enabled", "false");
    assertThat(settings.enabled(PaymentMethod.VNPAY)).isFalse();
    assertThat(settings.configured(PaymentMethod.VNPAY)).isTrue();
  }
}
