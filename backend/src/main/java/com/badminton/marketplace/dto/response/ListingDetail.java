package com.badminton.marketplace.dto.response;

import com.badminton.inspection.dto.response.InspectionReportResponse;
import java.util.List;

public record ListingDetail(
    UsedListingResponse listing,
    List<UsedListingImageResponse> images,
    UsedRacketSpecificationResponse specification,
    boolean verified,
    String conditionSource,
    InspectionReportResponse inspectionReport,
    SellerSummary seller) {
  public record SellerSummary(Long id, String name) {}
}
