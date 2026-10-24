package com.badminton.notification.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
public class NotificationController {
  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping("/api/notifications")
  public Object list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(page, size));
  }

  @PatchMapping("/api/notifications/{id}/read")
  public Object read(@PathVariable Long id) {
    service.read(id);
    return ApiResponse.ok("Đã đọc", null);
  }

  @PatchMapping("/api/notifications/read-all")
  public Object all() {
    service.readAll();
    return ApiResponse.ok("Đã đọc tất cả", null);
  }
}
