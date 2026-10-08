package com.badminton.audit.controller;

import com.badminton.audit.service.AuditService;
import com.badminton.common.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
  private final AuditService service;

  public AuditController(AuditService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<?> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(page, size));
  }
}
