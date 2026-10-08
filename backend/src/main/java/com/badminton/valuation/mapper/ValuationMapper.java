package com.badminton.valuation.mapper;

import com.badminton.valuation.dto.response.ValuationResponse;
import com.badminton.valuation.entity.Valuation;

public final class ValuationMapper {
  private ValuationMapper() {}

  public static ValuationResponse toResponse(Valuation e) {
    return new ValuationResponse(
        e.getValuationId(),
        e.getListing() == null ? null : e.getListing().getListingId(),
        e.getReferencePrice(),
        e.getAgeFactor(),
        e.getConditionFactor(),
        e.getWarrantyFactor(),
        e.getAccessoryFactor(),
        e.getRepairFactor(),
        e.getDemandFactor(),
        e.getEstimatedPrice(),
        e.getSuggestedMinPrice(),
        e.getSuggestedMaxPrice(),
        e.getValuationMethod(),
        e.getConditionScore(),
        e.getAlgorithmVersion(),
        e.getNote(),
        e.getCreatedAt());
  }
}
