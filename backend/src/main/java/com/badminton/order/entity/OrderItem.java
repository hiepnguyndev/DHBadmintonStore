package com.badminton.order.entity;

import com.badminton.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id")
  private Long orderItemId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "product_image", nullable = true, length = 500)
  private String productImage;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPrice;

  public Long getOrderItemId() {
    return orderItemId;
  }

  public void setOrderItemId(Long value) {
    orderItemId = value;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order value) {
    order = value;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product value) {
    product = value;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String value) {
    productName = value;
  }

  public String getProductImage() {
    return productImage;
  }

  public void setProductImage(String value) {
    productImage = value;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    quantity = value;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal value) {
    unitPrice = value;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal value) {
    totalPrice = value;
  }
}
