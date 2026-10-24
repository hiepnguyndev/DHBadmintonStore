package com.badminton.order.entity;

import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "orders")
public class Order extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long orderId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "order_code", nullable = false, length = 50)
  private String orderCode;

  @Column(name = "receiver_name", nullable = false, length = 100)
  private String receiverName;

  @Column(name = "receiver_phone", nullable = false, length = 20)
  private String receiverPhone;

  @Column(name = "shipping_address", nullable = false, length = 500)
  private String shippingAddress;

  @Column(name = "subtotal_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal subtotalAmount;

  @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
  private BigDecimal shippingFee;

  @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal discountAmount;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 30)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false, length = 30)
  private PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_status", nullable = false, length = 30)
  private OrderStatus orderStatus;

  @Column(name = "note", nullable = true, columnDefinition = "TEXT")
  private String note;

  @Column(name = "inventory_committed", nullable = false)
  private Boolean inventoryCommitted = false;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long value) {
    orderId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }

  public String getOrderCode() {
    return orderCode;
  }

  public void setOrderCode(String value) {
    orderCode = value;
  }

  public String getReceiverName() {
    return receiverName;
  }

  public void setReceiverName(String value) {
    receiverName = value;
  }

  public String getReceiverPhone() {
    return receiverPhone;
  }

  public void setReceiverPhone(String value) {
    receiverPhone = value;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(String value) {
    shippingAddress = value;
  }

  public BigDecimal getSubtotalAmount() {
    return subtotalAmount;
  }

  public void setSubtotalAmount(BigDecimal value) {
    subtotalAmount = value;
  }

  public BigDecimal getShippingFee() {
    return shippingFee;
  }

  public void setShippingFee(BigDecimal value) {
    shippingFee = value;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  public void setDiscountAmount(BigDecimal value) {
    discountAmount = value;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal value) {
    totalAmount = value;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod value) {
    paymentMethod = value;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus value) {
    paymentStatus = value;
  }

  public OrderStatus getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(OrderStatus value) {
    orderStatus = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    note = value;
  }

  public Boolean getInventoryCommitted() { return inventoryCommitted; }

  public void setInventoryCommitted(Boolean value) { inventoryCommitted = value; }
}
