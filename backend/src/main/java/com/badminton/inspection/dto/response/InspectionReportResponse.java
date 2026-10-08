package com.badminton.inspection.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record InspectionReportResponse(
    Long reportId,
    Long inspectionId,
    Boolean modelVerified,
    Boolean serialVerified,
    String frameCondition,
    String paintCondition,
    String grommetCondition,
    String handleCondition,
    Boolean crackDetected,
    Boolean repairDetected,
    BigDecimal overallScore,
    BigDecimal recommendedMinPrice,
    BigDecimal recommendedMaxPrice,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
