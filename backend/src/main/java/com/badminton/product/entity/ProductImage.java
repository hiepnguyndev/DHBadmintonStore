package com.badminton.product.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long imageId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(name = "is_primary", nullable = false)
  private Boolean isPrimary;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getImageId() {
    return imageId;
  }

  public void setImageId(Long value) {
    imageId = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String value) {
    imageUrl = value;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer value) {
    sortOrder = value;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean value) {
    isPrimary = value;
  }
}
