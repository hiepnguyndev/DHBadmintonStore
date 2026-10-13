package com.badminton.order.mapper;

import com.badminton.order.dto.response.OrderItemResponse;
import com.badminton.order.entity.OrderItem;

public final class OrderItemMapper {
  private OrderItemMapper() {}

  public static OrderItemResponse toResponse(OrderItem e) {
    return new OrderItemResponse(
        e.getOrderItemId(),
        e.getOrder() == null ? null : e.getOrder().getOrderId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getProductName(),
        e.getProductImage(),
        e.getVariant()==null?null:e.getVariant().getVariantId(),
        e.getVariantSku(),
        e.getVariantColor(),
        e.getVariantSize(),
        e.getQuantity(),
        e.getUnitPrice(),
        e.getTotalPrice());
  }
}
