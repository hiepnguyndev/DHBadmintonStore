package com.badminton.cart.entity;

import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "carts")
public class Cart extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_id")
  private Long cartId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Long getCartId() {
    return cartId;
  }

  public void setCartId(Long value) {
    cartId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }
}
