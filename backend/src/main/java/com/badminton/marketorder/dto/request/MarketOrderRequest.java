package com.badminton.marketorder.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record MarketOrderRequest(
    @NotNull Long listingId,
    @NotNull Long addressId,
    @NotBlank @Pattern(regexp = "ESCROW_SIMULATION") String paymentMethod) {}
