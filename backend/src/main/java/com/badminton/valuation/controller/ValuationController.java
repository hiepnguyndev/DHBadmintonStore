package com.badminton.valuation.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.valuation.dto.request.ValuationRequest;
import com.badminton.valuation.service.ValuationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValuationController {
  private final ValuationService service;

  public ValuationController(ValuationService service) {
    this.service = service;
  }

  @PostMapping("/api/valuations")
  @PreAuthorize("hasRole('USER')")
  public Object create(@Valid @RequestBody ValuationRequest r) {
    return ApiResponse.ok("Khoảng giá tham khảo", service.create(r));
  }

  @GetMapping("/api/valuations/listing/{listingId}")
  public Object history(@PathVariable Long listingId) {
    return ApiResponse.ok("Lịch sử định giá", service.history(listingId));
  }
}
