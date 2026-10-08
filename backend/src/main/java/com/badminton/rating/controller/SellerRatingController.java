package com.badminton.rating.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.rating.dto.request.*;
import com.badminton.rating.service.SellerRatingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class SellerRatingController {
  private final SellerRatingService service;

  public SellerRatingController(SellerRatingService service) {
    this.service = service;
  }

  @PostMapping("/api/seller-ratings")
  public ApiResponse<?> rate(@Valid @RequestBody SellerRatingRequest r) {
    return ApiResponse.ok(service.rate(r));
  }

  @GetMapping("/api/sellers/{sellerId}")
  public ApiResponse<?> profile(@PathVariable Long sellerId) {
    return ApiResponse.ok(service.profile(sellerId));
  }
}
