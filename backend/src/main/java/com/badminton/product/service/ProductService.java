package com.badminton.product.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.brand.repository.BrandRepository;
import com.badminton.category.repository.CategoryRepository;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.product.dto.request.*;
import com.badminton.product.dto.response.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.*;
import com.badminton.product.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {
  private final ProductRepository products;
  private final CategoryRepository categories;
  private final BrandRepository brands;
  private final RacketSpecificationRepository specs;
  private final ProductImageRepository images;
  private final ProductVariantRepository variants;
  private final Access access;

  public ProductService(
      ProductRepository products,
      CategoryRepository categories,
      BrandRepository brands,
      RacketSpecificationRepository specs,
      ProductImageRepository images,
      ProductVariantRepository variants,
      Access access) {
    this.products = products;
    this.categories = categories;
    this.brands = brands;
    this.specs = specs;
    this.images = images;
    this.variants = variants;
    this.access = access;
  }

  public ProductResponse view(Product p) {
    return ProductMapper.toResponse(
        p,
        specs.findByProductProductId(p.getProductId()).orElse(null),
        images.findByProductProductIdOrderBySortOrderAsc(p.getProductId()));
  }

  /** Maps a product batch with two bulk lookups instead of two extra queries per product. */
  public List<ProductResponse> views(Collection<Product> productBatch) {
    if (productBatch.isEmpty()) return List.of();
    var ids = productBatch.stream().map(Product::getProductId).toList();
    var specByProduct = new HashMap<Long, RacketSpecification>();
    specs.findByProductProductIdIn(ids)
        .forEach(s -> specByProduct.put(s.getProduct().getProductId(), s));
    var imagesByProduct = new HashMap<Long, List<ProductImage>>();
    images.findByProductProductIdInOrderBySortOrderAsc(ids)
        .forEach(i -> imagesByProduct.computeIfAbsent(i.getProduct().getProductId(), k -> new ArrayList<>()).add(i));
    return productBatch.stream()
        .map(p -> ProductMapper.toResponse(p, specByProduct.get(p.getProductId()),
            imagesByProduct.getOrDefault(p.getProductId(), List.of())))
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductResponse get(Long id) {
    Product p = found(products.findById(id), "PRODUCT_NOT_FOUND");
    require(
        p.getStatus() == ProductStatus.ACTIVE
            || p.getStatus() == ProductStatus.OUT_OF_STOCK
            || access.optional() != null
                && access.optional().role() == com.badminton.user.entity.Role.ADMIN,
        "PRODUCT_NOT_FOUND",
        "Sản phẩm không khả dụng");
    return view(p);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> list(
      String keyword,
      Long categoryId,
      Long brandId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String weight,
      String balance,
      String flex,
      String playStyle,
      String skillLevel,
      boolean onSale,
      int page,
      int size,
      String sort) {
    org.springframework.data.jpa.domain.Specification<Product> filter =
        (r, q, b) -> r.get("status").in(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK);
    if (onSale)
      filter = filter.and((r, q, b) ->
          b.and(b.isTrue(r.get("promotionPublished")), b.isNotNull(r.get("salePrice")),
              b.greaterThanOrEqualTo(r.<BigDecimal>get("salePrice"), BigDecimal.ZERO),
              b.lessThan(r.<BigDecimal>get("salePrice"), r.<BigDecimal>get("price"))));
    if (keyword != null && !keyword.isBlank())
      filter = filter.and(contains("productName", keyword));
    if (categoryId != null) filter = filter.and(eq("category.categoryId", categoryId));
    if (brandId != null) filter = filter.and(eq("brand.brandId", brandId));
    bad(minPrice == null || minPrice.signum() >= 0, "Giá tối thiểu không âm");
    bad(maxPrice == null || maxPrice.signum() >= 0, "Giá tối đa không âm");
    bad(
        minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0,
        "Khoảng giá không hợp lệ");
    if (minPrice != null)
      filter =
          filter.and(
              (r, q, b) ->
                  b.ge(b.coalesce(r.<BigDecimal>get("salePrice"), r.get("price")), minPrice));
    if (maxPrice != null)
      filter =
          filter.and(
              (r, q, b) ->
                  b.le(b.coalesce(r.<BigDecimal>get("salePrice"), r.get("price")), maxPrice));
    if (weight != null
        || balance != null
        || flex != null
        || playStyle != null
        || skillLevel != null)
      filter =
          filter.and(
              (r, q, b) -> {
                var sq = q.subquery(Long.class);
                var s = sq.from(RacketSpecification.class);
                var terms = new ArrayList<jakarta.persistence.criteria.Predicate>();
                terms.add(b.equal(s.get("product"), r));
                if (weight != null) terms.add(b.equal(s.get("weightClass"), weight));
                if (balance != null)
                  terms.add(b.equal(s.get("balanceType").as(String.class), balance));
                if (flex != null)
                  terms.add(b.equal(s.get("shaftFlexibility").as(String.class), flex));
                if (playStyle != null)
                  terms.add(b.equal(s.get("playStyle").as(String.class), playStyle));
                if (skillLevel != null)
                  terms.add(b.equal(s.get("skillLevel").as(String.class), skillLevel));
                sq.select(s.get("specificationId"))
                    .where(terms.toArray(jakarta.persistence.criteria.Predicate[]::new));
                return b.exists(sq);
              });
    var result = products.findAll(
                filter,
                page(
                    page,
                    size,
                    sort,
                    "createdAt",
                    Set.of("createdAt", "price", "productName", "productId")));
    return new PageResponse<>(views(result.getContent()), result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages());
  }

  public ProductResponse save(Long id, ProductRequest r) {
    bad(
        r.salePrice() == null || r.salePrice().compareTo(r.price()) <= 0,
        "salePrice phải <= price");
    Product p = id == null ? new Product() : found(products.lock(id), "PRODUCT_NOT_FOUND");
    p.setCategory(found(categories.findById(r.categoryId()), "CATEGORY_NOT_FOUND"));
    p.setBrand(found(brands.findById(r.brandId()), "BRAND_NOT_FOUND"));
    p.setProductName(r.productName().trim());
    p.setDescription(r.description());
    p.setPrice(r.price());
    p.setSalePrice(r.salePrice());
    boolean variantManaged=id!=null&&variants.existsByProductProductId(id);
    if(!variantManaged)p.setStock(r.stock());
    p.setStatus(
        variantManaged
            ? (r.status() == ProductStatus.INACTIVE
                ? ProductStatus.INACTIVE
                : p.getStock() == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE)
            : (r.stock() == 0 && r.status() == ProductStatus.ACTIVE
                ? ProductStatus.OUT_OF_STOCK
                : r.status()));
    if (id == null) p.setSlug(slug(r.productName()));
    if (p.getStatus() == ProductStatus.INACTIVE || p.getSalePrice() == null
        || p.getSalePrice().compareTo(p.getPrice()) >= 0) {
      p.setPromotionPublished(false);
    }
    products.saveAndFlush(p);
    if (r.specification() != null) {
      RacketSpecification s =
          specs.findByProductProductId(p.getProductId()).orElseGet(RacketSpecification::new);
      var x = r.specification();
      s.setProduct(p);
      s.setWeightClass(x.weightClass());
      s.setBalanceType(x.balanceType());
      s.setBalancePointMm(x.balancePointMm());
      s.setShaftFlexibility(x.shaftFlexibility());
      s.setMaxTensionLbs(x.maxTensionLbs());
      s.setPlayStyle(x.playStyle());
      s.setSkillLevel(x.skillLevel());
      s.setGripSize(x.gripSize());
      s.setRacketLengthMm(x.racketLengthMm());
      s.setMaterial(x.material());
      specs.saveAndFlush(s);
    } else {
      specs.findByProductProductId(p.getProductId()).ifPresent(specs::delete);
      specs.flush();
    }
    return view(p);
  }

  public void disable(Long id) {
    Product product = found(products.lock(id), "PRODUCT_NOT_FOUND");
    product.setStatus(ProductStatus.INACTIVE);
    product.setPromotionPublished(false);
  }
}
