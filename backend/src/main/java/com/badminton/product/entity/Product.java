package com.badminton.product.entity;

import com.badminton.brand.entity.Brand;
import com.badminton.category.entity.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "products")
public class Product extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "slug", nullable = false, length = 255)
  private String slug;

  @Column(name = "description", nullable = true, columnDefinition = "TEXT")
  private String description;

  @Column(name = "price", nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Column(name = "sale_price", nullable = true, precision = 15, scale = 2)
  private BigDecimal salePrice;

  @Column(name = "promotion_published", nullable = false)
  private boolean promotionPublished;

  @Column(name = "stock", nullable = false)
  private Integer stock;

  @Column(name = "thumbnail_url", nullable = true, length = 500)
  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private ProductStatus status;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long value) {
    productId = value;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category value) {
    category = value;
  }

  public Brand getBrand() {
    return brand;
  }

  public void setBrand(Brand value) {
    brand = value;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String value) {
    productName = value;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String value) {
    slug = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    description = value;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal value) {
    price = value;
  }

  public BigDecimal getSalePrice() {
    return salePrice;
  }

  public void setSalePrice(BigDecimal value) {
    salePrice = value;
  }

  public boolean isPromotionPublished() {
    return promotionPublished;
  }

  public void setPromotionPublished(boolean value) {
    promotionPublished = value;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer value) {
    stock = value;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String value) {
    thumbnailUrl = value;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public void setStatus(ProductStatus value) {
    status = value;
  }
}
