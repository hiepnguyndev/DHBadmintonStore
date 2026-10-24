package com.badminton.review.entity;

import com.badminton.order.entity.OrderItem;
import com.badminton.product.entity.Product;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "reviews")
public class Review extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "review_id")
  private Long reviewId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_item_id", nullable = false)
  private OrderItem orderItem;

  @Column(name = "rating", nullable = false, columnDefinition = "TINYINT")
  private Byte rating;

  @Column(name = "comment", nullable = true, columnDefinition = "TEXT")
  private String comment;

  @Column(name = "status", length = 20)
  private String status;

  public Long getReviewId() {
    return reviewId;
  }

  public void setReviewId(Long value) {
    reviewId = value;
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

  public OrderItem getOrderItem() {
    return orderItem;
  }

  public void setOrderItem(OrderItem value) {
    orderItem = value;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    status = value;
  }
}
