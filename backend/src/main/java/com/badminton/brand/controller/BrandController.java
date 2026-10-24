package com.badminton.brand.controller;

import com.badminton.brand.dto.request.BrandRequest;
import com.badminton.brand.service.BrandService;
import com.badminton.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class BrandController {
  private final BrandService service;

  public BrandController(BrandService service) {
    this.service = service;
  }

  @GetMapping("/api/brands")
  public Object list() {
    return ApiResponse.ok("Thành công", service.list());
  }

  @PostMapping("/api/brands")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody BrandRequest r) {
    return ApiResponse.ok("Đã tạo thương hiệu", service.save(null, r));
  }

  @PutMapping("/api/brands/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object update(@PathVariable Long id, @Valid @RequestBody BrandRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.save(id, r));
  }

  @DeleteMapping("/api/brands/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object disable(@PathVariable Long id) {
    service.disable(id);
    return ApiResponse.ok("Đã vô hiệu hóa", null);
  }
}
