package com.badminton.shipment.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ShipmentStatusRequest(
    @NotNull com.badminton.shipment.entity.ShipmentStatus shippingStatus) {}
