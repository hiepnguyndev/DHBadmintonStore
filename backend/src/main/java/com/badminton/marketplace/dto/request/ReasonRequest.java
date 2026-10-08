package com.badminton.marketplace.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ReasonRequest(@NotBlank @Size(max = 2000) String reason) {}
