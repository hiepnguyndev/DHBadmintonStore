package com.badminton.dispute.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.dispute.service.DisputeEvidenceService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DisputeEvidenceController {
  private final DisputeEvidenceService service;

  public DisputeEvidenceController(DisputeEvidenceService service) {
    this.service = service;
  }

  @PostMapping(value = "/api/disputes/{id}/evidences", consumes = "multipart/form-data")
  public ApiResponse<?> upload(
      @PathVariable Long id,
      @RequestParam org.springframework.web.multipart.MultipartFile file,
      @RequestParam(required = false) String description) {
    return ApiResponse.ok(service.upload(id, file, description));
  }
}
