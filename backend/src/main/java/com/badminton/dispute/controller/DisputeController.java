package com.badminton.dispute.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.dispute.dto.request.*;
import com.badminton.dispute.entity.*;
import com.badminton.dispute.service.DisputeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class DisputeController {
  private final DisputeService service;

  public DisputeController(DisputeService service) {
    this.service = service;
  }

  @PostMapping("/api/disputes")
  public ApiResponse<?> create(@Valid @RequestBody DisputeRequest r) {
    return ApiResponse.ok(service.create(r));
  }

  @GetMapping("/api/disputes/{id}")
  public ApiResponse<?> detail(@PathVariable Long id) {
    return ApiResponse.ok(service.detail(id));
  }

  @GetMapping("/api/admin/disputes")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> list(
      @RequestParam(required = false) DisputeStatus status,
      @RequestParam(required = false) DisputeReason reason,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(status, reason, page, size));
  }

  @PostMapping("/api/admin/disputes/{id}/resolve")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> resolve(@PathVariable Long id, @Valid @RequestBody ResolutionRequest r) {
    return ApiResponse.ok(service.resolve(id, r));
  }
}
