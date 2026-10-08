package com.badminton.dispute.entity;

import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "dispute_evidences")
public class DisputeEvidence {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "evidence_id")
  private Long evidenceId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "dispute_id", nullable = false)
  private Dispute dispute;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "uploaded_by", nullable = false)
  private User uploadedBy;

  @Column(name = "file_url", nullable = false, length = 500)
  private String fileUrl;

  @Column(name = "file_type", nullable = false, length = 30)
  private String fileType;

  @Column(name = "description", nullable = true, columnDefinition = "TEXT")
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getEvidenceId() {
    return evidenceId;
  }

  public void setEvidenceId(Long value) {
    evidenceId = value;
  }

  public Dispute getDispute() {
    return dispute;
  }

  public void setDispute(Dispute value) {
    dispute = value;
  }

  public User getUploadedBy() {
    return uploadedBy;
  }

  public void setUploadedBy(User value) {
    uploadedBy = value;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String value) {
    fileUrl = value;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String value) {
    fileType = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    description = value;
  }
}
