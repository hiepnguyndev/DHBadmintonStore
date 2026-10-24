package com.badminton.category.controller;

import com.badminton.category.dto.request.CategoryRequest;
import com.badminton.category.service.CategoryService;
import com.badminton.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class CategoryController {
  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping("/api/categories")
  public Object list() {
    return ApiResponse.ok("Thành công", service.list());
  }

  @PostMapping("/api/categories")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody CategoryRequest r) {
    return ApiResponse.ok("Đã tạo danh mục", service.save(null, r));
  }

  @PutMapping("/api/categories/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object update(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.save(id, r));
  }
}
