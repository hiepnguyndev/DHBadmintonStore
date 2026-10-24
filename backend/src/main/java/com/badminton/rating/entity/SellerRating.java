package com.badminton.rating.entity;

import com.badminton.marketorder.entity.MarketplaceOrder;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "seller_ratings")
public class SellerRating {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rating_id")
  private Long ratingId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "market_order_id", nullable = false)
  private MarketplaceOrder marketOrder;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @Column(name = "rating", nullable = false, columnDefinition = "TINYINT")
  private Byte rating;

  @Column(name = "comment", nullable = true, columnDefinition = "TEXT")
  private String comment;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now(ZoneOffset.UTC);
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getRatingId() {
    return ratingId;
  }

  public void setRatingId(Long value) {
    ratingId = value;
  }

  public MarketplaceOrder getMarketOrder() {
    return marketOrder;
  }

  public void setMarketOrder(MarketplaceOrder value) {
    marketOrder = value;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User value) {
    seller = value;
  }

  public User getBuyer() {
    return buyer;
  }

  public void setBuyer(User value) {
    buyer = value;
  }

  public Byte getRating() {
    return rating;
  }

  public void setRating(Byte value) {
    rating = value;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String value) {
    comment = value;
  }
}
