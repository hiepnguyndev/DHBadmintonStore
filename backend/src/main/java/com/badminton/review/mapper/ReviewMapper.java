package com.badminton.review.mapper;

import com.badminton.review.dto.response.ReviewResponse;
import com.badminton.review.entity.Review;

public final class ReviewMapper {
  private ReviewMapper() {}

  public static ReviewResponse toResponse(Review e) {
    return new ReviewResponse(
        e.getReviewId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getOrderItem() == null ? null : e.getOrderItem().getOrderItemId(),
        e.getRating(),
        e.getComment(),
        e.getStatus(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
