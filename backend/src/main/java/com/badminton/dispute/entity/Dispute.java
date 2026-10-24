package com.badminton.dispute.entity;

import com.badminton.marketorder.entity.MarketplaceOrder;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "disputes")
public class Dispute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dispute_id")
  private Long disputeId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "market_order_id", nullable = false)
  private MarketplaceOrder marketOrder;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false, length = 50)
  private DisputeReason reason;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private DisputeStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "resolution", nullable = true, length = 50)
  private Resolution resolution;

  @Column(name = "resolution_note", nullable = true, columnDefinition = "TEXT")
  private String resolutionNote;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "resolved_by", nullable = true)
  private User resolvedBy;

  @Column(name = "resolved_at", nullable = true)
  private LocalDateTime resolvedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getDisputeId() {
    return disputeId;
  }

  public void setDisputeId(Long value) {
    disputeId = value;
  }

  public MarketplaceOrder getMarketOrder() {
    return marketOrder;
  }

  public void setMarketOrder(MarketplaceOrder value) {
    marketOrder = value;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User value) {
    createdBy = value;
  }

  public DisputeReason getReason() {
    return reason;
  }

  public void setReason(DisputeReason value) {
    reason = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    description = value;
  }

  public DisputeStatus getStatus() {
    return status;
  }

  public void setStatus(DisputeStatus value) {
    status = value;
  }

  public Resolution getResolution() {
    return resolution;
  }

  public void setResolution(Resolution value) {
    resolution = value;
  }

  public String getResolutionNote() {
    return resolutionNote;
  }

  public void setResolutionNote(String value) {
    resolutionNote = value;
  }

  public User getResolvedBy() {
    return resolvedBy;
  }

  public void setResolvedBy(User value) {
    resolvedBy = value;
  }

  public LocalDateTime getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(LocalDateTime value) {
    resolvedAt = value;
  }
}
