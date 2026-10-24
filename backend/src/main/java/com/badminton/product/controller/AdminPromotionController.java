package com.badminton.product.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.product.dto.request.PromotionRequest;
import com.badminton.product.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {
  private final PromotionService service;

  public AdminPromotionController(PromotionService service) {
    this.service = service;
  }

  @GetMapping
  public Object list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean published,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {
    return ApiResponse.ok("Thành công", service.list(keyword, published, page, size));
  }

  @PutMapping("/{id}/promotion")
  public Object publish(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
    return ApiResponse.ok("Đã đăng ưu đãi", service.publish(id, request));
  }

  @DeleteMapping("/{id}/promotion")
  public Object end(@PathVariable Long id) {
    return ApiResponse.ok("Đã kết thúc ưu đãi", service.end(id));
  }
}
