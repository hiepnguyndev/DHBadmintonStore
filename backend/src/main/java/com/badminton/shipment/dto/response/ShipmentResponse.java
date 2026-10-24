package com.badminton.shipment.dto.response;

import com.badminton.shipment.entity.ShipmentStatus;
import java.time.*;

public record ShipmentResponse(
    Long shipmentId,
    Long marketOrderId,
    String carrier,
    String trackingCode,
    ShipmentStatus shippingStatus,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
