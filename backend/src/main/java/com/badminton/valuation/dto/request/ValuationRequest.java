package com.badminton.valuation.dto.request;

import com.badminton.valuation.service.ValuationCalculator.Accessories;
import com.badminton.valuation.service.ValuationCalculator.Repair;
import jakarta.validation.constraints.NotNull;

public record ValuationRequest(
    @NotNull Long listingId,
    @NotNull Accessories accessoryCondition,
    @NotNull Repair repairLevel) {}
