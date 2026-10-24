package com.badminton.notification.entity;

import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long notificationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "type", nullable = false, length = 50)
  private String type;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "content", nullable = true, columnDefinition = "TEXT")
  private String content;

  @Column(name = "reference_type", nullable = false, length = 50)
  private String referenceType;

  @Column(name = "reference_id", nullable = false)
  private Long referenceId;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(Long value) {
    notificationId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    type = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    title = value;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String value) {
    content = value;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(String value) {
    referenceType = value;
  }

  public Long getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(Long value) {
    referenceId = value;
  }

  public Boolean getIsRead() {
    return isRead;
  }

  public void setIsRead(Boolean value) {
    isRead = value;
  }
}
