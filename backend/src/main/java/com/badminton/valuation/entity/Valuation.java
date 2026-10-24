package com.badminton.valuation.entity;

import com.badminton.marketplace.entity.UsedListing;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "valuations")
public class Valuation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "valuation_id")
  private Long valuationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private UsedListing listing;

  @Column(name = "reference_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal referencePrice;

  @Column(name = "age_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal ageFactor;

  @Column(name = "condition_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal conditionFactor;

  @Column(name = "warranty_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal warrantyFactor;

  @Column(name = "accessory_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal accessoryFactor;

  @Column(name = "repair_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal repairFactor;

  @Column(name = "demand_factor", nullable = false, precision = 5, scale = 3)
  private BigDecimal demandFactor;

  @Column(name = "estimated_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal estimatedPrice;

  @Column(name = "suggested_min_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal suggestedMinPrice;

  @Column(name = "suggested_max_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal suggestedMaxPrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "valuation_method", nullable = false, length = 30)
  private ValuationMethod valuationMethod;

  @Column(name = "condition_score", nullable = true, precision = 4, scale = 2)
  private BigDecimal conditionScore;

  @Column(name = "algorithm_version", nullable = false, length = 50)
  private String algorithmVersion;

  @Column(name = "note", nullable = true, columnDefinition = "TEXT")
  private String note;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getValuationId() {
    return valuationId;
  }

  public void setValuationId(Long value) {
    valuationId = value;
  }

  public UsedListing getListing() {
    return listing;
  }

  public void setListing(UsedListing value) {
    listing = value;
  }

  public BigDecimal getReferencePrice() {
    return referencePrice;
  }

  public void setReferencePrice(BigDecimal value) {
    referencePrice = value;
  }

  public BigDecimal getAgeFactor() {
    return ageFactor;
  }

  public void setAgeFactor(BigDecimal value) {
    ageFactor = value;
  }

  public BigDecimal getConditionFactor() {
    return conditionFactor;
  }

  public void setConditionFactor(BigDecimal value) {
    conditionFactor = value;
  }

  public BigDecimal getWarrantyFactor() {
    return warrantyFactor;
  }

  public void setWarrantyFactor(BigDecimal value) {
    warrantyFactor = value;
  }

  public BigDecimal getAccessoryFactor() {
    return accessoryFactor;
  }

  public void setAccessoryFactor(BigDecimal value) {
    accessoryFactor = value;
  }

  public BigDecimal getRepairFactor() {
    return repairFactor;
  }

  public void setRepairFactor(BigDecimal value) {
    repairFactor = value;
  }

  public BigDecimal getDemandFactor() {
    return demandFactor;
  }

  public void setDemandFactor(BigDecimal value) {
    demandFactor = value;
  }

  public BigDecimal getEstimatedPrice() {
    return estimatedPrice;
  }

  public void setEstimatedPrice(BigDecimal value) {
    estimatedPrice = value;
  }

  public BigDecimal getSuggestedMinPrice() {
    return suggestedMinPrice;
  }

  public void setSuggestedMinPrice(BigDecimal value) {
    suggestedMinPrice = value;
  }

  public BigDecimal getSuggestedMaxPrice() {
    return suggestedMaxPrice;
  }

  public void setSuggestedMaxPrice(BigDecimal value) {
    suggestedMaxPrice = value;
  }

  public ValuationMethod getValuationMethod() {
    return valuationMethod;
  }

  public void setValuationMethod(ValuationMethod value) {
    valuationMethod = value;
  }

  public BigDecimal getConditionScore() {
    return conditionScore;
  }

  public void setConditionScore(BigDecimal value) {
    conditionScore = value;
  }

  public String getAlgorithmVersion() {
    return algorithmVersion;
  }

  public void setAlgorithmVersion(String value) {
    algorithmVersion = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }
}
