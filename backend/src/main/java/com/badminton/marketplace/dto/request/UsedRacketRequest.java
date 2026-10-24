package com.badminton.marketplace.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record UsedRacketRequest(
    @NotBlank @Pattern(regexp = "[2-6]U") String weightClass,
    @Size(max = 20) String gripSize,
    @Size(max = 100) String stringModel,
    @DecimalMin("1") @DecimalMax("99") BigDecimal stringTensionLbs,
    @NotBlank @Size(max = 50) String frameCondition,
    @NotBlank @Size(max = 50) String paintCondition,
    @NotBlank @Size(max = 50) String grommetCondition,
    @NotBlank @Size(max = 50) String handleCondition,
    @Size(max = 5000) String repairHistory,
    @Size(max = 100) String serialNumber) {}
