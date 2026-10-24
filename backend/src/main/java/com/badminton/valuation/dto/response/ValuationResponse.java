package com.badminton.valuation.dto.response;

import com.badminton.valuation.entity.ValuationMethod;
import java.math.BigDecimal;
import java.time.*;

public record ValuationResponse(
    Long valuationId,
    Long listingId,
    BigDecimal referencePrice,
    BigDecimal ageFactor,
    BigDecimal conditionFactor,
    BigDecimal warrantyFactor,
    BigDecimal accessoryFactor,
    BigDecimal repairFactor,
    BigDecimal demandFactor,
    BigDecimal estimatedPrice,
    BigDecimal suggestedMinPrice,
    BigDecimal suggestedMaxPrice,
    ValuationMethod valuationMethod,
    BigDecimal conditionScore,
    String algorithmVersion,
    String note,
    LocalDateTime createdAt) {}
