package com.badminton.admin.controller;

import com.badminton.admin.service.AdminUserService;
import com.badminton.common.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminUserController {
  private final AdminUserService service;

  public AdminUserController(AdminUserService service) {
    this.service = service;
  }

  @GetMapping("/api/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(keyword, page, size));
  }

  @GetMapping("/api/admin/users/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> detail(@PathVariable Long id) {
    return ApiResponse.ok(service.detail(id));
  }

  @PostMapping("/api/admin/users/{id}/lock")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> lock(@PathVariable Long id) {
    return ApiResponse.ok(service.lock(id, true));
  }

  @PostMapping("/api/admin/users/{id}/unlock")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> unlock(@PathVariable Long id) {
    return ApiResponse.ok(service.lock(id, false));
  }
}
