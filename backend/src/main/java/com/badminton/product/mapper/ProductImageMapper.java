package com.badminton.product.mapper;

import com.badminton.product.dto.response.ProductImageResponse;
import com.badminton.product.entity.ProductImage;

public final class ProductImageMapper {
  private ProductImageMapper() {}

  public static ProductImageResponse toResponse(ProductImage e) {
    return new ProductImageResponse(
        e.getImageId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getImageUrl(),
        e.getSortOrder(),
        e.getIsPrimary(),
        e.getCreatedAt());
  }
}
