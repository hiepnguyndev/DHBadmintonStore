package com.badminton.inspection.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "inspection_reports")
public class InspectionReport extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "report_id")
  private Long reportId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "inspection_id", nullable = false)
  private InspectionRequest inspection;

  @Column(name = "model_verified", nullable = false)
  private Boolean modelVerified;

  @Column(name = "serial_verified", nullable = false)
  private Boolean serialVerified;

  @Column(name = "frame_condition", nullable = false, length = 50)
  private String frameCondition;

  @Column(name = "paint_condition", nullable = false, length = 50)
  private String paintCondition;

  @Column(name = "grommet_condition", nullable = false, length = 50)
  private String grommetCondition;

  @Column(name = "handle_condition", nullable = false, length = 50)
  private String handleCondition;

  @Column(name = "crack_detected", nullable = false)
  private Boolean crackDetected;

  @Column(name = "repair_detected", nullable = false)
  private Boolean repairDetected;

  @Column(name = "overall_score", nullable = false, precision = 4, scale = 2)
  private BigDecimal overallScore;

  @Column(name = "recommended_min_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal recommendedMinPrice;

  @Column(name = "recommended_max_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal recommendedMaxPrice;

  @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
  private String notes;

  public Long getReportId() {
    return reportId;
  }

  public void setReportId(Long value) {
    reportId = value;
  }

  public InspectionRequest getInspection() {
    return inspection;
  }

  public void setInspection(InspectionRequest value) {
    inspection = value;
  }

  public Boolean getModelVerified() {
    return modelVerified;
  }

  public void setModelVerified(Boolean value) {
    modelVerified = value;
  }

  public Boolean getSerialVerified() {
    return serialVerified;
  }

  public void setSerialVerified(Boolean value) {
    serialVerified = value;
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

  public Boolean getCrackDetected() {
    return crackDetected;
  }

  public void setCrackDetected(Boolean value) {
    crackDetected = value;
  }

  public Boolean getRepairDetected() {
    return repairDetected;
  }

  public void setRepairDetected(Boolean value) {
    repairDetected = value;
  }

  public BigDecimal getOverallScore() {
    return overallScore;
  }

  public void setOverallScore(BigDecimal value) {
    overallScore = value;
  }

  public BigDecimal getRecommendedMinPrice() {
    return recommendedMinPrice;
  }

  public void setRecommendedMinPrice(BigDecimal value) {
    recommendedMinPrice = value;
  }

  public BigDecimal getRecommendedMaxPrice() {
    return recommendedMaxPrice;
  }

  public void setRecommendedMaxPrice(BigDecimal value) {
    recommendedMaxPrice = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    notes = value;
  }
}
