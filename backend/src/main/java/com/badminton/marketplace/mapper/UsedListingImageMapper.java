package com.badminton.marketplace.mapper;

import com.badminton.marketplace.dto.response.UsedListingImageResponse;
import com.badminton.marketplace.entity.UsedListingImage;

public final class UsedListingImageMapper {
  private UsedListingImageMapper() {}

  public static UsedListingImageResponse toResponse(UsedListingImage e) {
    return new UsedListingImageResponse(
        e.getImageId(),
        e.getListing() == null ? null : e.getListing().getListingId(),
        e.getImageUrl(),
        e.getImageType(),
        e.getSortOrder(),
        e.getCreatedAt());
  }
}
