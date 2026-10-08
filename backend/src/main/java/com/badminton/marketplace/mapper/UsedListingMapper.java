package com.badminton.marketplace.mapper;

import com.badminton.marketplace.dto.response.UsedListingResponse;
import com.badminton.marketplace.entity.UsedListing;

public final class UsedListingMapper {
  private UsedListingMapper() {}

  public static UsedListingResponse toResponse(UsedListing e) {
    return new UsedListingResponse(
        e.getListingId(),
        e.getSeller() == null ? null : e.getSeller().getUserId(),
        e.getCategory() == null ? null : e.getCategory().getCategoryId(),
        e.getBrand() == null ? null : e.getBrand().getBrandId(),
        e.getListingCode(),
        e.getProductName(),
        e.getModel(),
        e.getDescription(),
        e.getOriginalPrice(),
        e.getSellingPrice(),
        e.getPurchaseDate(),
        e.getUsageMonths(),
        e.getConditionGrade(),
        e.getConditionScore(),
        e.getWarrantyStatus(),
        e.getWarrantyUntil(),
        e.getStatus(),
        e.getViewCount(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
