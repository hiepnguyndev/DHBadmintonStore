package com.badminton.audit.entity;

import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_id")
  private Long auditId;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @Column(name = "old_data", nullable = true, columnDefinition = "JSON")
  private String oldData;

  @Column(name = "new_data", nullable = true, columnDefinition = "JSON")
  private String newData;

  @Column(name = "ip_address", nullable = true, length = 50)
  private String ipAddress;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getAuditId() {
    return auditId;
  }

  public void setAuditId(Long value) {
    auditId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String value) {
    action = value;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String value) {
    entityType = value;
  }

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long value) {
    entityId = value;
  }

  public String getOldData() {
    return oldData;
  }

  public void setOldData(String value) {
    oldData = value;
  }

  public String getNewData() {
    return newData;
  }

  public void setNewData(String value) {
    newData = value;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String value) {
    ipAddress = value;
  }
}
