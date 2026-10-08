package com.badminton.category.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "categories")
public class Category extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "category_name", nullable = false, length = 100)
  private String categoryName;

  @Column(name = "slug", nullable = false, length = 100)
  private String slug;

  @Column(name = "description", nullable = true, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CategoryStatus status = CategoryStatus.ACTIVE;

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long value) {
    categoryId = value;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String value) {
    categoryName = value;
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

  public CategoryStatus getStatus() {
    return status;
  }

  public void setStatus(CategoryStatus value) {
    status = value;
  }
}
