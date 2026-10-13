package com.badminton.payment;

import com.badminton.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {
  private final PaymentService service;
  private final PaymentSettings settings;
  private final PaymentGateway gateway;
  private final ObjectMapper json;
  public PaymentController(PaymentService service, PaymentSettings settings, PaymentGateway gateway, ObjectMapper json) {
    this.service = service; this.settings = settings; this.gateway = gateway; this.json = json;
  }
  @GetMapping("/api/payments/methods")
  public Object methods() { return ApiResponse.ok("Thành công", settings.methods()); }
  @GetMapping("/api/payments/bank-details")
  public Object bankDetails() { return ApiResponse.ok("Thông tin chuyển khoản", settings.bankDetails()); }
  @GetMapping("/api/orders/{id}/payment")
  public Object get(@PathVariable Long id) { return ApiResponse.ok("Thành công", service.get(id)); }
  @PostMapping("/api/orders/{id}/payment")
  @PreAuthorize("hasRole('USER')")
  public Object start(@PathVariable Long id, HttpServletRequest request) {
    return ApiResponse.ok("Thông tin thanh toán", service.start(id, request.getRemoteAddr()));
  }
  public record Confirmation(@NotNull @DecimalMin("0.01") BigDecimal amount,
      @NotBlank @Pattern(regexp = "[A-Za-z0-9_.:/-]{3,100}") String reference) {}
  @PostMapping("/api/admin/orders/{id}/payment/confirm")
  @PreAuthorize("hasRole('ADMIN')")
  public Object confirm(@PathVariable Long id, @Valid @RequestBody Confirmation confirmation) {
    return ApiResponse.ok("Đã xác nhận tiền vào", service.confirmTransfer(id, confirmation.amount(), confirmation.reference()));
  }
  @PostMapping("/api/admin/orders/{id}/payment/refund-confirm")
  @PreAuthorize("hasRole('ADMIN')")
  public Object refund(@PathVariable Long id, @Valid @RequestBody Confirmation confirmation) {
    return ApiResponse.ok("Đã ghi nhận hoàn tiền", service.confirmRefund(id, confirmation.amount(), confirmation.reference()));
  }
  @GetMapping("/api/payments/callbacks/vnpay")
  public Object vnpay(@RequestParam Map<String, String> params, HttpServletRequest request) {
    try {
      if (request.getParameterMap().values().stream().anyMatch(values -> values.length != 1)) throw new IllegalArgumentException();
      var result = service.settle(gateway.verifyVnpay(params));
      return Map.of("RspCode", switch (result) {
        case OK -> "00"; case ALREADY -> "02"; case NOT_FOUND -> "01"; case AMOUNT_MISMATCH -> "04"; case INVALID -> "97";
      }, "Message", result.name());
    } catch (IllegalArgumentException e) { return Map.of("RspCode", "97", "Message", "Invalid signature or request"); }
    catch (RuntimeException e) { return Map.of("RspCode", "99", "Message", "Please retry"); }
  }
  @PostMapping("/api/payments/callbacks/momo")
  public ResponseEntity<Void> momo(@RequestBody JsonNode body) {
    try {
      var result = service.settle(gateway.verifyMomo(body));
      return result == PaymentService.Result.OK || result == PaymentService.Result.ALREADY
          ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    } catch (IllegalArgumentException e) { return ResponseEntity.badRequest().build(); }
  }
  @PostMapping("/api/payments/callbacks/zalopay")
  public Object zalopay(@RequestBody JsonNode body) {
    try {
      var result = service.settle(gateway.verifyZalopay(body));
      return Map.of("return_code", result == PaymentService.Result.OK || result == PaymentService.Result.ALREADY ? 1 : 2,
          "return_message", result.name());
    } catch (IllegalArgumentException e) { return Map.of("return_code", -1, "return_message", "Invalid signature or request"); }
    catch (RuntimeException e) { return Map.of("return_code", 0, "return_message", "Please retry"); }
  }
  @PostMapping("/api/payments/callbacks/sepay")
  public ResponseEntity<Map<String, Boolean>> sepay(
      @RequestHeader(value = "X-SePay-Signature", required = false) String signature,
      @RequestHeader(value = "X-SePay-Timestamp", required = false) String timestamp,
      @RequestBody byte[] rawBody) {
    try {
      if (!validSePaySignature(signature, timestamp, rawBody)) return ResponseEntity.status(401).body(Map.of("success", false));
      JsonNode event = json.readTree(rawBody);
      if (!"in".equals(event.path("transferType").asText())) return ResponseEntity.ok(Map.of("success", true));
      long transactionId = event.path("id").asLong(-1);
      String accountNumber = event.path("accountNumber").asText();
      String code = event.path("code").isNull() ? null : event.path("code").asText();
      BigDecimal amount = event.path("transferAmount").decimalValue();
      if (transactionId < 1 || amount.signum() <= 0) return ResponseEntity.badRequest().body(Map.of("success", false));
      service.confirmSePay(transactionId, accountNumber, code, amount);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("success", false));
    }
  }
  private boolean validSePaySignature(String signature, String timestamp, byte[] rawBody) {
    if (!settings.sepayEnabled() || signature == null || timestamp == null) return false;
    try {
      long seconds = Long.parseLong(timestamp);
      if (Math.abs(Instant.now().getEpochSecond() - seconds) > 300) return false;
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(settings.sepaySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] expected = ("sha256=" + java.util.HexFormat.of().formatHex(mac.doFinal((timestamp + "." + new String(rawBody, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8)))).getBytes(StandardCharsets.UTF_8);
      return MessageDigest.isEqual(expected, signature.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) { return false; }
  }
}
