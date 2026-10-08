package com.badminton.statistics.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.statistics.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatisticsController {
  private final StatisticsService service;

  public StatisticsController(StatisticsService service) {
    this.service = service;
  }

  @GetMapping("/api/admin/statistics/overview")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> overview() {
    return ApiResponse.ok(service.overview());
  }

  @GetMapping("/api/admin/statistics/revenue")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> revenue(
      @RequestParam java.time.LocalDate from,
      @RequestParam java.time.LocalDate to,
      @RequestParam(defaultValue = "MONTH") String groupBy) {
    return ApiResponse.ok(service.revenue(from, to, groupBy));
  }

  @GetMapping("/api/admin/statistics/top-products")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> products() {
    return ApiResponse.ok(service.topProducts());
  }

  @GetMapping("/api/admin/statistics/top-sellers")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> sellers() {
    return ApiResponse.ok(service.topSellers());
  }
}
