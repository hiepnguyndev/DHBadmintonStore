package com.badminton.inspection.mapper;

import com.badminton.inspection.dto.response.InspectionReportResponse;
import com.badminton.inspection.entity.InspectionReport;

public final class InspectionReportMapper {
  private InspectionReportMapper() {}

  public static InspectionReportResponse toResponse(InspectionReport e) {
    return new InspectionReportResponse(
        e.getReportId(),
        e.getInspection() == null ? null : e.getInspection().getInspectionId(),
        e.getModelVerified(),
        e.getSerialVerified(),
        e.getFrameCondition(),
        e.getPaintCondition(),
        e.getGrommetCondition(),
        e.getHandleCondition(),
        e.getCrackDetected(),
        e.getRepairDetected(),
        e.getOverallScore(),
        e.getRecommendedMinPrice(),
        e.getRecommendedMaxPrice(),
        e.getNotes(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
