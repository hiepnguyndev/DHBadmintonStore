package com.badminton.recommendation.entity;

import com.badminton.product.entity.Product;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "recommendation_logs")
public class RecommendationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long logId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "match_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal matchScore;

  @Column(name = "reason_json", nullable = false, columnDefinition = "JSON")
  private String reasonJson;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getLogId() {
    return logId;
  }

  public void setLogId(Long value) {
    logId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public BigDecimal getMatchScore() {
    return matchScore;
  }

  public void setMatchScore(BigDecimal value) {
    matchScore = value;
  }

  public String getReasonJson() {
    return reasonJson;
  }

  public void setReasonJson(String value) {
    reasonJson = value;
  }
}
