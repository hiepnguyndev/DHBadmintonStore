package com.badminton.order.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.order.dto.request.*;
import com.badminton.order.entity.OrderStatus;
import com.badminton.order.service.OrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
  private final OrderService service;

  public OrderController(OrderService service) {
    this.service = service;
  }

  @PostMapping("/api/orders")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object checkout(@Valid @RequestBody CheckoutRequest r) {
    return ApiResponse.ok("Đã đặt hàng", service.checkout(r));
  }

  @GetMapping("/api/orders/my")
  @PreAuthorize("hasRole('USER')")
  public Object mine(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(false, status, null, page, size));
  }

  @GetMapping("/api/orders/{id}")
  public Object detail(@PathVariable Long id) {
    return ApiResponse.ok("Thành công", service.detail(id));
  }

  @PostMapping("/api/orders/{id}/cancel")
  @PreAuthorize("hasRole('USER')")
  public Object cancel(@PathVariable Long id) {
    return ApiResponse.ok("Đã hủy đơn", service.cancel(id));
  }

  @GetMapping("/api/admin/orders")
  @PreAuthorize("hasRole('ADMIN')")
  public Object admin(
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(true, status, date, page, size));
  }

  @PatchMapping("/api/admin/orders/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public Object status(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.transition(id, r.status()));
  }

  public record ResetOrdersRequest(@jakarta.validation.constraints.NotBlank String confirmation) {}

  @PostMapping("/api/admin/orders/reset")
  @PreAuthorize("hasRole('ADMIN')")
  public Object reset(@Valid @RequestBody ResetOrdersRequest request) {
    if (!"XOA_DON_HANG".equals(request.confirmation()))
      throw new com.badminton.exception.BusinessException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "RESET_CONFIRMATION_INVALID",
          "Nhập chính xác XOA_DON_HANG để xác nhận.");
    return ApiResponse.ok("Đã reset đơn hàng và doanh thu", service.resetAll());
  }
}
