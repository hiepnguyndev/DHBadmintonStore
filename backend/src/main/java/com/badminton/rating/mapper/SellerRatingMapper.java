package com.badminton.rating.mapper;

import com.badminton.rating.dto.response.SellerRatingResponse;
import com.badminton.rating.entity.SellerRating;

public final class SellerRatingMapper {
  private SellerRatingMapper() {}

  public static SellerRatingResponse toResponse(SellerRating e) {
    return new SellerRatingResponse(
        e.getRatingId(),
        e.getMarketOrder() == null ? null : e.getMarketOrder().getMarketOrderId(),
        e.getSeller() == null ? null : e.getSeller().getUserId(),
        e.getBuyer() == null ? null : e.getBuyer().getUserId(),
        e.getRating(),
        e.getComment(),
        e.getCreatedAt());
  }
}
