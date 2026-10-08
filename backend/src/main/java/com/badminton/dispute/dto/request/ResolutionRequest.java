package com.badminton.dispute.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ResolutionRequest(
    @NotNull com.badminton.dispute.entity.Resolution resolution,
    @NotBlank @Size(max = 5000) String resolutionNote) {}
