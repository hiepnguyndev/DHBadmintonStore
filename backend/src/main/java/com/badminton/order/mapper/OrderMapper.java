package com.badminton.order.mapper;

import com.badminton.order.dto.response.OrderResponse;
import com.badminton.order.entity.Order;

public final class OrderMapper {
  private OrderMapper() {}

  public static OrderResponse toResponse(Order e) {
    return new OrderResponse(
        e.getOrderId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getOrderCode(),
        e.getReceiverName(),
        e.getReceiverPhone(),
        e.getShippingAddress(),
        e.getSubtotalAmount(),
        e.getShippingFee(),
        e.getDiscountAmount(),
        e.getTotalAmount(),
        e.getPaymentMethod(),
        e.getPaymentStatus(),
        e.getOrderStatus(),
        e.getNote(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
