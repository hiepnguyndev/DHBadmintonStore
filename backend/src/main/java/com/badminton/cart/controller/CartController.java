package com.badminton.cart.controller;

import com.badminton.cart.dto.request.*;
import com.badminton.cart.service.CartService;
import com.badminton.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {
  private final CartService service;

  public CartController(CartService service) {
    this.service = service;
  }

  @GetMapping("/api/cart")
  @PreAuthorize("hasRole('USER')")
  public Object get() {
    return ApiResponse.ok("Thành công", service.get());
  }

  @PostMapping("/api/cart/items")
  @PreAuthorize("hasRole('USER')")
  public Object add(@Valid @RequestBody CartItemRequest r) {
    return ApiResponse.ok("Đã thêm vào giỏ", service.add(r));
  }

  @PutMapping("/api/cart/items/{id}")
  @PreAuthorize("hasRole('USER')")
  public Object update(@PathVariable Long id, @Valid @RequestBody QuantityRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.update(id, r));
  }

  @DeleteMapping("/api/cart/items/{id}")
  @PreAuthorize("hasRole('USER')")
  public Object remove(@PathVariable Long id) {
    service.remove(id);
    return ApiResponse.ok("Đã xóa", null);
  }
}
