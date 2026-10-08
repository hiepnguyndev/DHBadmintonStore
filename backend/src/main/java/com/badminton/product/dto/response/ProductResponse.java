package com.badminton.product.dto.response;

import com.badminton.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
    Long id,
    String productName,
    String slug,
    String description,
    BigDecimal price,
    BigDecimal salePrice,
    BigDecimal effectivePrice,
    Integer stock,
    String thumbnailUrl,
    ProductStatus status,
    NamedReference brand,
    NamedReference category,
    RacketSpecificationResponse specification,
    List<ProductImageResponse> images) {
  public record NamedReference(Long id, String name) {}
}
