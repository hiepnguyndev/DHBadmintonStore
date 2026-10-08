package com.badminton.rating.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record SellerRatingRequest(
    @NotNull Long marketOrderId,
    @NotNull @Min(1) @Max(5) Byte rating,
    @Size(max = 2000) String comment) {}
