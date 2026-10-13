package com.badminton.chatbot.service;

import com.badminton.category.repository.CategoryRepository;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.entity.*;
import com.badminton.product.repository.ProductRepository;
import com.badminton.product.service.ProductService;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatbotCatalog {
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final ProductService productService;

  public ChatbotCatalog(ProductRepository products, CategoryRepository categories,
      ProductService productService) {
    this.products = products;
    this.categories = categories;
    this.productService = productService;
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> find(LocalNlp.Criteria criteria) {
    var categoryIds = categories.findAll().stream()
        .filter(c -> criteria.category().equals(LocalNlp.category(c.getCategoryName())))
        .map(c -> c.getCategoryId()).toList();
    if (categoryIds.isEmpty()) return List.of();

    Specification<Product> filter = (r, q, b) -> {
      var terms = new ArrayList<jakarta.persistence.criteria.Predicate>();
      terms.add(b.equal(r.get("status"), ProductStatus.ACTIVE));
      terms.add(b.gt(r.get("stock"), 0));
      terms.add(r.get("category").get("categoryId").in(categoryIds));
      var price = b.<BigDecimal>selectCase()
          .when(b.and(b.isNotNull(r.get("salePrice")), b.ge(r.get("salePrice"), 0),
              b.le(r.get("salePrice"), r.<BigDecimal>get("price"))), r.get("salePrice"))
          .otherwise(r.get("price"));
      if (criteria.maxBudget() != null) terms.add(b.le(price, criteria.maxBudget()));

      // Variant-managed products need an actually purchasable variant, not stale aggregate stock.
      var anyVariant = q.subquery(Long.class);
      var any = anyVariant.from(ProductVariant.class);
      anyVariant.select(any.get("variantId")).where(b.equal(any.get("product"), r));
      var availableVariant = q.subquery(Long.class);
      var variant = availableVariant.from(ProductVariant.class);
      var variantTerms = new ArrayList<jakarta.persistence.criteria.Predicate>();
      variantTerms.add(b.equal(variant.get("product"), r));
      variantTerms.add(b.equal(variant.get("status"), "ACTIVE"));
      variantTerms.add(b.gt(variant.get("stock"), 0));
      if (criteria.color() != null) {
        var colors = switch (criteria.color()) {
          case "trang" -> List.of("trắng", "trang", "white");
          case "den" -> List.of("đen", "den", "black");
          case "xanh" -> List.of("xanh", "blue", "green");
          case "do" -> List.of("đỏ", "do", "red");
          case "vang" -> List.of("vàng", "vang", "yellow");
          case "hong" -> List.of("hồng", "hong", "pink");
          default -> List.of("tím", "tim", "purple");
        };
        String pattern = "(^|[^[:alnum:]])(" + String.join("|", colors) + ")([^[:alnum:]]|$)";
        variantTerms.add(b.isTrue(b.function("regexp_like", Boolean.class,
            variant.get("color"), b.literal(pattern), b.literal("i"))));
      }
      availableVariant.select(variant.get("variantId"))
          .where(variantTerms.toArray(jakarta.persistence.criteria.Predicate[]::new));
      terms.add(criteria.color() == null
          ? b.or(b.not(b.exists(anyVariant)), b.exists(availableVariant))
          : b.exists(availableVariant));

      if (criteria.weight() != null || criteria.playStyle() != null || criteria.skillLevel() != null) {
        var specQuery = q.subquery(Long.class);
        var spec = specQuery.from(RacketSpecification.class);
        var specTerms = new ArrayList<jakarta.persistence.criteria.Predicate>();
        specTerms.add(b.equal(spec.get("product"), r));
        if (criteria.weight() != null) specTerms.add(b.equal(spec.get("weightClass"), criteria.weight()));
        if (criteria.playStyle() != null) specTerms.add(b.equal(spec.get("playStyle").as(String.class), criteria.playStyle()));
        if (criteria.skillLevel() != null) specTerms.add(b.equal(spec.get("skillLevel").as(String.class), criteria.skillLevel()));
        specQuery.select(spec.get("specificationId"))
            .where(specTerms.toArray(jakarta.persistence.criteria.Predicate[]::new));
        terms.add(b.exists(specQuery));
      }
      if (q.getResultType() != Long.class) q.orderBy(b.asc(price), b.asc(r.get("productId")));
      return b.and(terms.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
    return productService.views(products.findAll(filter, PageRequest.of(0, 3)).getContent());
  }
}
