package com.badminton.brand.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "brands")
public class Brand extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "brand_id")
  private Long brandId;

  @Column(name = "brand_name", nullable = false, length = 100)
  private String brandName;

  @Column(name = "slug", nullable = false, length = 100)
  private String slug;

  @Column(name = "description", nullable = true, columnDefinition = "TEXT")
  private String description;

  @Column(name = "logo_url", nullable = true, length = 500)
  private String logoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private BrandStatus status = BrandStatus.ACTIVE;

  public Long getBrandId() {
    return brandId;
  }

  public void setBrandId(Long value) {
    brandId = value;
  }

  public String getBrandName() {
    return brandName;
  }

  public void setBrandName(String value) {
    brandName = value;
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

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String value) {
    logoUrl = value;
  }

  public BrandStatus getStatus() {
    return status;
  }

  public void setStatus(BrandStatus value) {
    status = value;
  }
}
