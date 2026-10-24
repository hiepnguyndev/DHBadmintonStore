package com.badminton.marketorder.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.marketorder.dto.request.*;
import com.badminton.marketorder.service.MarketOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketplaceOrderController {
  private final MarketOrderService service;

  public MarketplaceOrderController(MarketOrderService service) {
    this.service = service;
  }

  @PostMapping("/api/market-orders")
  public ApiResponse<?> purchase(@Valid @RequestBody MarketOrderRequest r) {
    return ApiResponse.ok(service.purchase(r));
  }

  @GetMapping("/api/market-orders/buying")
  public ApiResponse<?> buying(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(false, page, size));
  }

  @GetMapping("/api/market-orders/selling")
  public ApiResponse<?> selling(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(true, page, size));
  }

  @GetMapping("/api/market-orders/{id}")
  public ApiResponse<?> detail(@PathVariable Long id) {
    return ApiResponse.ok(service.detail(id));
  }

  @PostMapping("/api/market-orders/{id}/cancel")
  public ApiResponse<?> cancel(@PathVariable Long id) {
    return ApiResponse.ok(service.cancel(id));
  }

  @PostMapping("/api/market-orders/{id}/confirm-received")
  public ApiResponse<?> confirm(@PathVariable Long id) {
    return ApiResponse.ok(service.confirm(id));
  }
}
