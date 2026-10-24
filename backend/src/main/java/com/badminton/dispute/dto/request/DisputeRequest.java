package com.badminton.dispute.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record DisputeRequest(
    @NotNull Long marketOrderId,
    @NotNull com.badminton.dispute.entity.DisputeReason reason,
    @NotBlank @Size(max = 5000) String description) {}
