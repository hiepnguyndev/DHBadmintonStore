package com.badminton.shipment.entity;

import com.badminton.marketorder.entity.MarketplaceOrder;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "shipments")
public class Shipment extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "shipment_id")
  private Long shipmentId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "market_order_id", nullable = false)
  private MarketplaceOrder marketOrder;

  @Column(name = "carrier", nullable = false, length = 100)
  private String carrier;

  @Column(name = "tracking_code", nullable = false, length = 100)
  private String trackingCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "shipping_status", nullable = false, length = 30)
  private ShipmentStatus shippingStatus;

  @Column(name = "shipped_at", nullable = true)
  private LocalDateTime shippedAt;

  @Column(name = "delivered_at", nullable = true)
  private LocalDateTime deliveredAt;

  public Long getShipmentId() {
    return shipmentId;
  }

  public void setShipmentId(Long value) {
    shipmentId = value;
  }

  public MarketplaceOrder getMarketOrder() {
    return marketOrder;
  }

  public void setMarketOrder(MarketplaceOrder value) {
    marketOrder = value;
  }

  public String getCarrier() {
    return carrier;
  }

  public void setCarrier(String value) {
    carrier = value;
  }

  public String getTrackingCode() {
    return trackingCode;
  }

  public void setTrackingCode(String value) {
    trackingCode = value;
  }

  public ShipmentStatus getShippingStatus() {
    return shippingStatus;
  }

  public void setShippingStatus(ShipmentStatus value) {
    shippingStatus = value;
  }

  public LocalDateTime getShippedAt() {
    return shippedAt;
  }

  public void setShippedAt(LocalDateTime value) {
    shippedAt = value;
  }

  public LocalDateTime getDeliveredAt() {
    return deliveredAt;
  }

  public void setDeliveredAt(LocalDateTime value) {
    deliveredAt = value;
  }
}
