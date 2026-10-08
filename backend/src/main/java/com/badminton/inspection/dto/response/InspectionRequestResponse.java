package com.badminton.inspection.dto.response;

import com.badminton.inspection.entity.InspectionStatus;
import java.time.*;

public record InspectionRequestResponse(
    Long inspectionId,
    Long listingId,
    Long sellerId,
    Long inspectorId,
    String inspectionCode,
    InspectionStatus status,
    LocalDateTime requestedAt,
    LocalDateTime acceptedAt,
    LocalDateTime completedAt,
    String note) {}
