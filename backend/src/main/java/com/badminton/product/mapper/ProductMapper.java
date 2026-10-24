package com.badminton.product.mapper;

import com.badminton.product.dto.response.*;
import com.badminton.product.entity.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ProductMapper {
  private ProductMapper() {}

  public static BigDecimal effectivePrice(Product p) {
    BigDecimal price = p.getSalePrice() != null
            && p.getSalePrice().signum() >= 0
            && p.getSalePrice().compareTo(p.getPrice()) <= 0
        ? p.getSalePrice()
        : p.getPrice();
    // The store charges in whole VND; normalize catalog prices before totals are calculated.
    return price.setScale(0, RoundingMode.HALF_UP);
  }

  public static ProductResponse toResponse(
      Product p, RacketSpecification s, List<ProductImage> images) {
    return new ProductResponse(
        p.getProductId(),
        p.getProductName(),
        p.getSlug(),
        p.getDescription(),
        p.getPrice(),
        p.getSalePrice(),
        effectivePrice(p),
        p.isPromotionPublished(),
        p.getStock(),
        p.getThumbnailUrl(),
        p.getStatus(),
        new ProductResponse.NamedReference(p.getBrand().getBrandId(), p.getBrand().getBrandName()),
        new ProductResponse.NamedReference(
            p.getCategory().getCategoryId(), p.getCategory().getCategoryName()),
        s == null ? null : RacketSpecificationMapper.toResponse(s),
        images.stream().map(ProductImageMapper::toResponse).toList());
  }
}
