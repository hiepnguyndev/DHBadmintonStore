package com.badminton.shipment.mapper;

import com.badminton.shipment.dto.response.ShipmentResponse;
import com.badminton.shipment.entity.Shipment;

public final class ShipmentMapper {
  private ShipmentMapper() {}

  public static ShipmentResponse toResponse(Shipment e) {
    return new ShipmentResponse(
        e.getShipmentId(),
        e.getMarketOrder() == null ? null : e.getMarketOrder().getMarketOrderId(),
        e.getCarrier(),
        e.getTrackingCode(),
        e.getShippingStatus(),
        e.getShippedAt(),
        e.getDeliveredAt(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
