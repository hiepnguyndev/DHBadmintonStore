package com.badminton.marketplace.entity;

import com.badminton.brand.entity.Brand;
import com.badminton.category.entity.Category;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "used_listings")
public class UsedListing extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "listing_id")
  private Long listingId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Column(name = "listing_code", nullable = false, length = 50)
  private String listingCode;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "model", nullable = false, length = 150)
  private String model;

  @Column(name = "description", nullable = true, columnDefinition = "TEXT")
  private String description;

  @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "selling_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal sellingPrice;

  @Column(name = "purchase_date", nullable = true)
  private LocalDate purchaseDate;

  @Column(name = "usage_months", nullable = false)
  private Integer usageMonths;

  @Enumerated(EnumType.STRING)
  @Column(name = "condition_grade", nullable = false, length = 10)
  private ConditionGrade conditionGrade;

  @Column(name = "condition_score", nullable = true, precision = 4, scale = 2)
  private BigDecimal conditionScore;

  @Column(name = "warranty_status", nullable = false, length = 30)
  private String warrantyStatus;

  @Column(name = "warranty_until", nullable = true)
  private LocalDate warrantyUntil;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private ListingStatus status;

  @Column(name = "view_count", nullable = false)
  private Integer viewCount;

  public Long getListingId() {
    return listingId;
  }

  public void setListingId(Long value) {
    listingId = value;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User value) {
    seller = value;
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

  public String getListingCode() {
    return listingCode;
  }

  public void setListingCode(String value) {
    listingCode = value;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String value) {
    productName = value;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String value) {
    model = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    description = value;
  }

  public BigDecimal getOriginalPrice() {
    return originalPrice;
  }

  public void setOriginalPrice(BigDecimal value) {
    originalPrice = value;
  }

  public BigDecimal getSellingPrice() {
    return sellingPrice;
  }

  public void setSellingPrice(BigDecimal value) {
    sellingPrice = value;
  }

  public LocalDate getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(LocalDate value) {
    purchaseDate = value;
  }

  public Integer getUsageMonths() {
    return usageMonths;
  }

  public void setUsageMonths(Integer value) {
    usageMonths = value;
  }

  public ConditionGrade getConditionGrade() {
    return conditionGrade;
  }

  public void setConditionGrade(ConditionGrade value) {
    conditionGrade = value;
  }

  public BigDecimal getConditionScore() {
    return conditionScore;
  }

  public void setConditionScore(BigDecimal value) {
    conditionScore = value;
  }

  public String getWarrantyStatus() {
    return warrantyStatus;
  }

  public void setWarrantyStatus(String value) {
    warrantyStatus = value;
  }

  public LocalDate getWarrantyUntil() {
    return warrantyUntil;
  }

  public void setWarrantyUntil(LocalDate value) {
    warrantyUntil = value;
  }

  public ListingStatus getStatus() {
    return status;
  }

  public void setStatus(ListingStatus value) {
    status = value;
  }

  public Integer getViewCount() {
    return viewCount;
  }

  public void setViewCount(Integer value) {
    viewCount = value;
  }
}
