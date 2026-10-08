package com.badminton.recommendation.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.recommendation.dto.request.PlayerProfileRequest;
import com.badminton.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class RecommendationController {
  private final RecommendationService service;

  public RecommendationController(RecommendationService service) {
    this.service = service;
  }

  @GetMapping("/api/player-profile")
  @PreAuthorize("hasRole('USER')")
  public Object profile() {
    return ApiResponse.ok("Thành công", service.profile());
  }

  @PostMapping("/api/player-profile")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody PlayerProfileRequest r) {
    return ApiResponse.ok("Đã tạo hồ sơ", service.save(r, true));
  }

  @PutMapping("/api/player-profile")
  @PreAuthorize("hasRole('USER')")
  public Object update(@Valid @RequestBody PlayerProfileRequest r) {
    return ApiResponse.ok("Đã cập nhật hồ sơ", service.save(r, false));
  }

  @GetMapping("/api/recommendations")
  @PreAuthorize("hasRole('USER')")
  public Object recommendations(@RequestParam(defaultValue = "10") int limit) {
    return ApiResponse.ok("Gợi ý dựa trên nhu cầu của bạn", service.recommend(limit));
  }
}
