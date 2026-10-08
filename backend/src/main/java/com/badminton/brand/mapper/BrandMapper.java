package com.badminton.brand.mapper;

import com.badminton.brand.dto.response.BrandResponse;
import com.badminton.brand.entity.Brand;

public final class BrandMapper {
  private BrandMapper() {}

  public static BrandResponse toResponse(Brand e) {
    return new BrandResponse(
        e.getBrandId(),
        e.getBrandName(),
        e.getSlug(),
        e.getDescription(),
        e.getLogoUrl(),
        e.getStatus(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
