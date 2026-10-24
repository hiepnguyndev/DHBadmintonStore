package com.badminton.marketorder.entity;

public enum MarketOrderStatus {
  PENDING_PAYMENT,
  PAID,
  WAITING_SHIPMENT,
  SHIPPING,
  DELIVERED,
  COMPLETED,
  CANCELLED,
  DISPUTED,
  REFUNDED
}
