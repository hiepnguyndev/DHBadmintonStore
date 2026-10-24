package com.badminton.cart.entity;

import com.badminton.product.entity.Product;
import com.badminton.product.entity.ProductVariant;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "cart_items")
public class CartItem extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_item_id")
  private Long cartItemId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  public Long getCartItemId() {
    return cartItemId;
  }

  public void setCartItemId(Long value) {
    cartItemId = value;
  }

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart value) {
    cart = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public ProductVariant getVariant() { return variant; }
  public void setVariant(ProductVariant value) { variant = value; }

  public void setQuantity(Integer value) {
    quantity = value;
  }
}
