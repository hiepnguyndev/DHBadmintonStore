package com.badminton.marketplace.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record UsedRacketSpecificationResponse(
    Long usedSpecId,
    Long listingId,
    String weightClass,
    String gripSize,
    String stringModel,
    BigDecimal stringTensionLbs,
    String frameCondition,
    String paintCondition,
    String grommetCondition,
    String handleCondition,
    String repairHistory,
    String serialNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
