package com.badminton.review.dto.request;

import com.badminton.common.enums.*;
import com.badminton.order.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record ReviewRequest(
    @NotNull Long orderItemId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(max = 5000) String comment) {}
