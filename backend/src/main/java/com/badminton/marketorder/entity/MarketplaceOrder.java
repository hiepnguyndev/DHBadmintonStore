package com.badminton.marketorder.entity;

import com.badminton.marketplace.entity.UsedListing;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "marketplace_orders")
public class MarketplaceOrder extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "market_order_id")
  private Long marketOrderId;

  @Column(name = "order_code", nullable = false, length = 50)
  private String orderCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private UsedListing listing;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Column(name = "item_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal itemPrice;

  @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
  private BigDecimal shippingFee;

  @Column(name = "platform_fee", nullable = false, precision = 15, scale = 2)
  private BigDecimal platformFee;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_status", nullable = false, length = 30)
  private MarketOrderStatus orderStatus;

  @Column(name = "receiver_name", nullable = false, length = 100)
  private String receiverName;

  @Column(name = "receiver_phone", nullable = false, length = 20)
  private String receiverPhone;

  @Column(name = "shipping_address", nullable = false, length = 500)
  private String shippingAddress;

  @Column(name = "cancelled_by_type", nullable = true, length = 30)
  private String cancelledByType;

  @Column(name = "cancellation_reason", nullable = true, length = 100)
  private String cancellationReason;

  public Long getMarketOrderId() {
    return marketOrderId;
  }

  public void setMarketOrderId(Long value) {
    marketOrderId = value;
  }

  public String getOrderCode() {
    return orderCode;
  }

  public void setOrderCode(String value) {
    orderCode = value;
  }

  public UsedListing getListing() {
    return listing;
  }

  public void setListing(UsedListing value) {
    listing = value;
  }

  public User getBuyer() {
    return buyer;
  }

  public void setBuyer(User value) {
    buyer = value;
  }

  public User getSeller() {
    return seller;
  }

  public void setSeller(User value) {
    seller = value;
  }

  public BigDecimal getItemPrice() {
    return itemPrice;
  }

  public void setItemPrice(BigDecimal value) {
    itemPrice = value;
  }

  public BigDecimal getShippingFee() {
    return shippingFee;
  }

  public void setShippingFee(BigDecimal value) {
    shippingFee = value;
  }

  public BigDecimal getPlatformFee() {
    return platformFee;
  }

  public void setPlatformFee(BigDecimal value) {
    platformFee = value;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal value) {
    totalAmount = value;
  }

  public MarketOrderStatus getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(MarketOrderStatus value) {
    orderStatus = value;
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

  public String getCancelledByType() {
    return cancelledByType;
  }

  public void setCancelledByType(String value) {
    cancelledByType = value;
  }

  public String getCancellationReason() {
    return cancellationReason;
  }

  public void setCancellationReason(String value) {
    cancellationReason = value;
  }
}
