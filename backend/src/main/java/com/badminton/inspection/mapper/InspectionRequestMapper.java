package com.badminton.inspection.mapper;

import com.badminton.inspection.dto.response.InspectionRequestResponse;
import com.badminton.inspection.entity.InspectionRequest;

public final class InspectionRequestMapper {
  private InspectionRequestMapper() {}

  public static InspectionRequestResponse toResponse(InspectionRequest e) {
    return new InspectionRequestResponse(
        e.getInspectionId(),
        e.getListing() == null ? null : e.getListing().getListingId(),
        e.getSeller() == null ? null : e.getSeller().getUserId(),
        e.getInspector() == null ? null : e.getInspector().getUserId(),
        e.getInspectionCode(),
        e.getStatus(),
        e.getRequestedAt(),
        e.getAcceptedAt(),
        e.getCompletedAt(),
        e.getNote());
  }
}
