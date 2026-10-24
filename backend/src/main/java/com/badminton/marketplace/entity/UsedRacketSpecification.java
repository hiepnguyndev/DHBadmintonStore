package com.badminton.marketplace.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "used_racket_specifications")
public class UsedRacketSpecification extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "used_spec_id")
  private Long usedSpecId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private UsedListing listing;

  @Column(name = "weight_class", nullable = false, length = 10)
  private String weightClass;

  @Column(name = "grip_size", nullable = true, length = 20)
  private String gripSize;

  @Column(name = "string_model", nullable = true, length = 100)
  private String stringModel;

  @Column(name = "string_tension_lbs", nullable = true, precision = 5, scale = 2)
  private BigDecimal stringTensionLbs;

  @Column(name = "frame_condition", nullable = false, length = 50)
  private String frameCondition;

  @Column(name = "paint_condition", nullable = false, length = 50)
  private String paintCondition;

  @Column(name = "grommet_condition", nullable = false, length = 50)
  private String grommetCondition;

  @Column(name = "handle_condition", nullable = false, length = 50)
  private String handleCondition;

  @Column(name = "repair_history", nullable = true, columnDefinition = "TEXT")
  private String repairHistory;

  @Column(name = "serial_number", nullable = true, length = 100)
  private String serialNumber;

  public Long getUsedSpecId() {
    return usedSpecId;
  }

  public void setUsedSpecId(Long value) {
    usedSpecId = value;
  }

  public UsedListing getListing() {
    return listing;
  }

  public void setListing(UsedListing value) {
    listing = value;
  }

  public String getWeightClass() {
    return weightClass;
  }

  public void setWeightClass(String value) {
    weightClass = value;
  }

  public String getGripSize() {
    return gripSize;
  }

  public void setGripSize(String value) {
    gripSize = value;
  }

  public String getStringModel() {
    return stringModel;
  }

  public void setStringModel(String value) {
    stringModel = value;
  }

  public BigDecimal getStringTensionLbs() {
    return stringTensionLbs;
  }

  public void setStringTensionLbs(BigDecimal value) {
    stringTensionLbs = value;
  }

  public String getFrameCondition() {
    return frameCondition;
  }

  public void setFrameCondition(String value) {
    frameCondition = value;
  }

  public String getPaintCondition() {
    return paintCondition;
  }

  public void setPaintCondition(String value) {
    paintCondition = value;
  }

  public String getGrommetCondition() {
    return grommetCondition;
  }

  public void setGrommetCondition(String value) {
    grommetCondition = value;
  }

  public String getHandleCondition() {
    return handleCondition;
  }

  public void setHandleCondition(String value) {
    handleCondition = value;
  }

  public String getRepairHistory() {
    return repairHistory;
  }

  public void setRepairHistory(String value) {
    repairHistory = value;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String value) {
    serialNumber = value;
  }
}
