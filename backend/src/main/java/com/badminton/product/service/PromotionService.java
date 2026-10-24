package com.badminton.product.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.product.dto.request.PromotionRequest;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.entity.*;
import com.badminton.product.repository.ProductRepository;
import java.math.*;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromotionService {
  private final ProductRepository products;
  private final ProductService catalog;

  public PromotionService(ProductRepository products, ProductService catalog) {
    this.products = products;
    this.catalog = catalog;
  }

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> list(String keyword, Boolean published, int page, int size) {
    Specification<Product> filter = (r, q, b) -> b.conjunction();
    if (keyword != null && !keyword.isBlank()) filter = filter.and(contains("productName", keyword));
    if (published != null) filter = filter.and(eq("promotionPublished", published));
    var result = products.findAll(filter,
        page(page, size, "productId,desc", "productId", Set.of("productId")));
    return new PageResponse<>(catalog.views(result.getContent()), result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  public ProductResponse publish(Long id, PromotionRequest request) {
    Product product = found(products.lock(id), "PRODUCT_NOT_FOUND");
    bad(product.getStatus() == ProductStatus.ACTIVE || product.getStatus() == ProductStatus.OUT_OF_STOCK,
        "Không thể đăng ưu đãi cho sản phẩm ngừng kinh doanh");
    BigDecimal price = product.getPrice();
    bad(price.signum() > 0, "Giá gốc phải lớn hơn 0 để áp dụng ưu đãi");
    BigDecimal value = request.value();
    BigDecimal salePrice;
    if (request.discountType() == PromotionRequest.DiscountType.PERCENT) {
      bad(value.signum() > 0 && value.compareTo(new BigDecimal("100")) <= 0,
          "Mức giảm phải lớn hơn 0 và không quá 100%");
      salePrice = price.multiply(new BigDecimal("100").subtract(value))
          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    } else {
      salePrice = money(value);
    }
    bad(salePrice.signum() >= 0 && salePrice.compareTo(price) < 0,
        "Giá ưu đãi phải từ 0 và thấp hơn giá gốc");
    product.setSalePrice(salePrice);
    product.setPromotionPublished(true);
    products.saveAndFlush(product);
    return catalog.view(product);
  }

  public ProductResponse end(Long id) {
    Product product = found(products.lock(id), "PRODUCT_NOT_FOUND");
    product.setPromotionPublished(false);
    product.setSalePrice(null);
    products.saveAndFlush(product);
    return catalog.view(product);
  }
}
