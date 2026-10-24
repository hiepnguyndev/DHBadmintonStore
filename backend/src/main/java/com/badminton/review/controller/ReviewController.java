package com.badminton.review.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.review.dto.request.ReviewRequest;
import com.badminton.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {
  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  @PostMapping("/api/reviews")
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody ReviewRequest r) {
    return ApiResponse.ok("Đã đánh giá", service.create(r));
  }

  @GetMapping("/api/products/{productId}/reviews")
  public Object list(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(productId, page, size));
  }
}
