package com.badminton.marketplace.dto.request;

import com.badminton.common.enums.*;
import com.badminton.marketplace.entity.ConditionGrade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record ListingRequest(
    @NotNull Long categoryId,
    @NotNull Long brandId,
    @NotBlank @Size(max = 255) String productName,
    @NotBlank @Size(max = 150) String model,
    @NotBlank @Size(max = 20000) String description,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal originalPrice,
    @NotNull @DecimalMin("0.01") @Digits(integer = 13, fraction = 2) BigDecimal sellingPrice,
    @PastOrPresent LocalDate purchaseDate,
    @NotNull @Min(0) @Max(1200) Integer usageMonths,
    @NotNull ConditionGrade conditionGrade,
    @NotBlank @Size(max = 30) String warrantyStatus,
    LocalDate warrantyUntil,
    @Valid UsedRacketRequest racketSpecification) {}
