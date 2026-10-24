package com.badminton.inspection.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.inspection.dto.request.*;
import com.badminton.inspection.entity.InspectionStatus;
import com.badminton.inspection.service.InspectionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class InspectionController {
  private final InspectionService service;

  public InspectionController(InspectionService service) {
    this.service = service;
  }

  @PostMapping("/api/inspections")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody InspectionCreateRequest r) {
    return ApiResponse.ok("Đã gửi yêu cầu", service.create(r));
  }

  @GetMapping("/api/inspections/my")
  @PreAuthorize("hasRole('USER')")
  public Object mine(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(true, page, size, null));
  }

  @GetMapping("/api/inspector/inspections")
  @PreAuthorize("hasAnyRole('INSPECTOR','ADMIN')")
  public Object list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) InspectionStatus status) {
    return ApiResponse.ok("Thành công", service.list(false, page, size, status));
  }

  @PostMapping("/api/inspector/inspections/{id}/accept")
  @PreAuthorize("hasAnyRole('INSPECTOR','ADMIN')")
  public Object accept(@PathVariable Long id) {
    return ApiResponse.ok("Đã nhận kiểm định", service.accept(id));
  }

  @PostMapping("/api/inspector/inspections/{id}/start")
  @PreAuthorize("hasAnyRole('INSPECTOR','ADMIN')")
  public Object start(@PathVariable Long id) {
    return ApiResponse.ok("Đã bắt đầu", service.start(id));
  }

  @PostMapping("/api/inspector/inspections/{id}/report")
  @PreAuthorize("hasAnyRole('INSPECTOR','ADMIN')")
  public Object report(@PathVariable Long id, @Valid @RequestBody InspectionReportRequest r) {
    return ApiResponse.ok("Đã hoàn thành kiểm định", service.report(id, r));
  }
}
