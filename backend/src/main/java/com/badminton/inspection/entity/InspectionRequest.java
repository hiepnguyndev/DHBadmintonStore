package com.badminton.inspection.entity;

import com.badminton.marketplace.entity.UsedListing;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "inspection_requests")
public class InspectionRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inspection_id")
  private Long inspectionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private UsedListing listing;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "inspector_id", nullable = true)
  private User inspector;

  @Column(name = "inspection_code", nullable = false, length = 50)
  private String inspectionCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private InspectionStatus status;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "accepted_at", nullable = true)
  private LocalDateTime acceptedAt;

  @Column(name = "completed_at", nullable = true)
  private LocalDateTime completedAt;

  @Column(name = "note", nullable = true, columnDefinition = "TEXT")
  private String note;

  public Long getInspectionId() {
    return inspectionId;
  }

  public void setInspectionId(Long value) {
    inspectionId = value;
  }

  public UsedListing getListing() {
    return listing;
  }

  public void setListing(UsedListing value) {
    listing = value;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User value) {
    seller = value;
  }

  public User getInspector() {
    return inspector;
  }

  public void setInspector(User value) {
    inspector = value;
  }

  public String getInspectionCode() {
    return inspectionCode;
  }

  public void setInspectionCode(String value) {
    inspectionCode = value;
  }

  public InspectionStatus getStatus() {
    return status;
  }

  public void setStatus(InspectionStatus value) {
    status = value;
  }

  public LocalDateTime getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(LocalDateTime value) {
    requestedAt = value;
  }

  public LocalDateTime getAcceptedAt() {
    return acceptedAt;
  }

  public void setAcceptedAt(LocalDateTime value) {
    acceptedAt = value;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime value) {
    completedAt = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }
}
