package com.badminton.recommendation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "recommendation_weights")
public class RecommendationWeight extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "weight_id")
  private Long weightId;

  @Column(name = "skill_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal skillWeight;

  @Column(name = "play_style_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal playStyleWeight;

  @Column(name = "wrist_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal wristWeight;

  @Column(name = "weight_class_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal weightClassWeight;

  @Column(name = "flex_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal flexWeight;

  @Column(name = "budget_weight", nullable = false, precision = 5, scale = 2)
  private BigDecimal budgetWeight;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  public Long getWeightId() {
    return weightId;
  }

  public void setWeightId(Long value) {
    weightId = value;
  }

  public BigDecimal getSkillWeight() {
    return skillWeight;
  }

  public void setSkillWeight(BigDecimal value) {
    skillWeight = value;
  }

  public BigDecimal getPlayStyleWeight() {
    return playStyleWeight;
  }

  public void setPlayStyleWeight(BigDecimal value) {
    playStyleWeight = value;
  }

  public BigDecimal getWristWeight() {
    return wristWeight;
  }

  public void setWristWeight(BigDecimal value) {
    wristWeight = value;
  }

  public BigDecimal getWeightClassWeight() {
    return weightClassWeight;
  }

  public void setWeightClassWeight(BigDecimal value) {
    weightClassWeight = value;
  }

  public BigDecimal getFlexWeight() {
    return flexWeight;
  }

  public void setFlexWeight(BigDecimal value) {
    flexWeight = value;
  }

  public BigDecimal getBudgetWeight() {
    return budgetWeight;
  }

  public void setBudgetWeight(BigDecimal value) {
    budgetWeight = value;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean value) {
    isActive = value;
  }
}
