package com.badminton.marketplace.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "used_listing_images")
public class UsedListingImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long imageId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private UsedListing listing;

  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "image_type", nullable = false, length = 30)
  private ImageType imageType;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

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

  public UsedListing getListing() {
    return listing;
  }

  public void setListing(UsedListing value) {
    listing = value;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String value) {
    imageUrl = value;
  }

  public ImageType getImageType() {
    return imageType;
  }

  public void setImageType(ImageType value) {
    imageType = value;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer value) {
    sortOrder = value;
  }
}
