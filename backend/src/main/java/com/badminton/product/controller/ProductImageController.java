package com.badminton.product.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.product.service.ProductImageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductImageController {
  private final ProductImageService service;

  public ProductImageController(ProductImageService service) {
    this.service = service;
  }

  @PostMapping(value = "/api/products/{id}/images", consumes = "multipart/form-data")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> upload(
      @PathVariable Long id, @RequestParam org.springframework.web.multipart.MultipartFile file) {
    return ApiResponse.ok(service.upload(id, file));
  }
}
