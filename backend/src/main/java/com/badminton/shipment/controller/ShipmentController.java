package com.badminton.shipment.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.shipment.dto.request.*;
import com.badminton.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShipmentController {
  private final ShipmentService service;

  public ShipmentController(ShipmentService service) {
    this.service = service;
  }

  @PostMapping("/api/shipments")
  public ApiResponse<?> create(@Valid @RequestBody ShipmentRequest r) {
    return ApiResponse.ok(service.create(r));
  }

  @PatchMapping("/api/shipments/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ShipmentStatusRequest r) {
    return ApiResponse.ok(service.transition(id, r.shippingStatus()));
  }
}
