package com.badminton.marketplace.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.marketplace.dto.request.*;
import com.badminton.marketplace.entity.*;
import com.badminton.marketplace.service.MarketplaceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketplaceController {
  private final MarketplaceService service;

  public MarketplaceController(MarketplaceService service) {
    this.service = service;
  }

  @GetMapping("/api/marketplace/listings")
  public Object list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) ConditionGrade conditionGrade,
      @RequestParam(required = false) Boolean verified,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sort) {
    return ApiResponse.ok(
        "Thành công",
        service.list(
            keyword,
            categoryId,
            brandId,
            conditionGrade,
            verified,
            minPrice,
            maxPrice,
            page,
            size,
            sort,
            false,
            false,
            null));
  }

  @GetMapping("/api/marketplace/listings/{id}")
  public Object detail(@PathVariable Long id) {
    return ApiResponse.ok("Thành công", service.detail(id));
  }

  @GetMapping("/api/marketplace/my-listings")
  @PreAuthorize("hasRole('USER')")
  public Object mine(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) ListingStatus status) {
    return ApiResponse.ok(
        "Thành công",
        service.list(
            null, null, null, null, null, null, null, page, size, null, true, false, status));
  }

  @GetMapping("/api/admin/marketplace/listings")
  @PreAuthorize("hasRole('ADMIN')")
  public Object admin(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) ListingStatus status) {
    return ApiResponse.ok(
        "Thành công",
        service.list(
            null, null, null, null, null, null, null, page, size, null, false, true, status));
  }

  @PostMapping("/api/marketplace/listings")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody ListingRequest r) {
    return ApiResponse.ok("Đã tạo tin nháp", service.save(null, r));
  }

  @PutMapping("/api/marketplace/listings/{id}")
  @PreAuthorize("hasRole('USER')")
  public Object update(@PathVariable Long id, @Valid @RequestBody ListingRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.save(id, r));
  }

  @PostMapping("/api/marketplace/listings/{id}/submit")
  @PreAuthorize("hasRole('USER')")
  public Object submit(@PathVariable Long id) {
    return ApiResponse.ok("Đã gửi duyệt", service.submit(id));
  }

  @PostMapping("/api/admin/marketplace/listings/{id}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public Object approve(@PathVariable Long id) {
    return ApiResponse.ok("Đã duyệt", service.review(id, true, null));
  }

  @PostMapping("/api/admin/marketplace/listings/{id}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public Object reject(@PathVariable Long id, @Valid @RequestBody ReasonRequest r) {
    return ApiResponse.ok("Đã từ chối", service.review(id, false, r.reason()));
  }
}
