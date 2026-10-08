package com.badminton.shipment.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ShipmentRequest(
    @NotNull Long marketOrderId,
    @NotBlank @Size(max = 100) String carrier,
    @NotBlank @Size(max = 100) String trackingCode) {}
