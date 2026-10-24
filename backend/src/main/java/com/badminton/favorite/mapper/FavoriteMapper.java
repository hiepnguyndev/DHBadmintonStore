package com.badminton.favorite.mapper;

import com.badminton.favorite.dto.response.FavoriteResponse;
import com.badminton.favorite.entity.Favorite;

public final class FavoriteMapper {
  private FavoriteMapper() {}

  public static FavoriteResponse toResponse(Favorite e) {
    return new FavoriteResponse(
        e.getFavoriteId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getCreatedAt());
  }
}
