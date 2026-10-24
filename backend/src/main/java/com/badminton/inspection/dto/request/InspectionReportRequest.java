package com.badminton.inspection.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record InspectionReportRequest(
    @NotNull Boolean modelVerified,
    @NotNull Boolean serialVerified,
    @NotBlank @Size(max = 50) String frameCondition,
    @NotBlank @Size(max = 50) String paintCondition,
    @NotBlank @Size(max = 50) String grommetCondition,
    @NotBlank @Size(max = 50) String handleCondition,
    @NotNull Boolean crackDetected,
    @NotNull Boolean repairDetected,
    @NotNull @DecimalMin("0") @DecimalMax("10") BigDecimal overallScore,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal recommendedMinPrice,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal recommendedMaxPrice,
    @Size(max = 5000) String notes) {}
