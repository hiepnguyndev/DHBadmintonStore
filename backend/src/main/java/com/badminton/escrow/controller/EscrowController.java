package com.badminton.escrow.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.escrow.service.EscrowService;
import org.springframework.web.bind.annotation.*;

@RestController
public class EscrowController {
  private final EscrowService service;

  public EscrowController(EscrowService service) {
    this.service = service;
  }

  @PostMapping("/api/escrow/{marketOrderId}/pay")
  public ApiResponse<?> pay(@PathVariable Long marketOrderId) {
    return ApiResponse.ok(service.pay(marketOrderId));
  }
}
