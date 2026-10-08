package com.badminton.product.entity;

import com.badminton.common.enums.BalanceType;
import com.badminton.common.enums.PlayStyle;
import com.badminton.common.enums.ShaftFlexibility;
import com.badminton.common.enums.SkillLevel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "racket_specifications")
public class RacketSpecification extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "specification_id")
  private Long specificationId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "weight_class", nullable = false, length = 10)
  private String weightClass;

  @Enumerated(EnumType.STRING)
  @Column(name = "balance_type", nullable = false, length = 30)
  private BalanceType balanceType;

  @Column(name = "balance_point_mm", nullable = true)
  private Integer balancePointMm;

  @Enumerated(EnumType.STRING)
  @Column(name = "shaft_flexibility", nullable = false, length = 30)
  private ShaftFlexibility shaftFlexibility;

  @Column(name = "max_tension_lbs", nullable = true, precision = 5, scale = 2)
  private BigDecimal maxTensionLbs;

  @Enumerated(EnumType.STRING)
  @Column(name = "play_style", nullable = false, length = 30)
  private PlayStyle playStyle;

  @Enumerated(EnumType.STRING)
  @Column(name = "skill_level", nullable = false, length = 30)
  private SkillLevel skillLevel;

  @Column(name = "grip_size", nullable = true, length = 20)
  private String gripSize;

  @Column(name = "racket_length_mm", nullable = true)
  private Integer racketLengthMm;

  @Column(name = "material", nullable = true, length = 255)
  private String material;

  public Long getSpecificationId() {
    return specificationId;
  }

  public void setSpecificationId(Long value) {
    specificationId = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public String getWeightClass() {
    return weightClass;
  }

  public void setWeightClass(String value) {
    weightClass = value;
  }

  public BalanceType getBalanceType() {
    return balanceType;
  }

  public void setBalanceType(BalanceType value) {
    balanceType = value;
  }

  public Integer getBalancePointMm() {
    return balancePointMm;
  }

  public void setBalancePointMm(Integer value) {
    balancePointMm = value;
  }

  public ShaftFlexibility getShaftFlexibility() {
    return shaftFlexibility;
  }

  public void setShaftFlexibility(ShaftFlexibility value) {
    shaftFlexibility = value;
  }

  public BigDecimal getMaxTensionLbs() {
    return maxTensionLbs;
  }

  public void setMaxTensionLbs(BigDecimal value) {
    maxTensionLbs = value;
  }

  public PlayStyle getPlayStyle() {
    return playStyle;
  }

  public void setPlayStyle(PlayStyle value) {
    playStyle = value;
  }

  public SkillLevel getSkillLevel() {
    return skillLevel;
  }

  public void setSkillLevel(SkillLevel value) {
    skillLevel = value;
  }

  public String getGripSize() {
    return gripSize;
  }

  public void setGripSize(String value) {
    gripSize = value;
  }

  public Integer getRacketLengthMm() {
    return racketLengthMm;
  }

  public void setRacketLengthMm(Integer value) {
    racketLengthMm = value;
  }

  public String getMaterial() {
    return material;
  }

  public void setMaterial(String value) {
    material = value;
  }
}
