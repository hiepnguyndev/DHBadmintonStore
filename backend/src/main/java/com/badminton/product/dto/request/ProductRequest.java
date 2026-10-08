package com.badminton.product.dto.request;

import com.badminton.common.enums.*;
import com.badminton.product.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record ProductRequest(
    @NotNull Long categoryId,
    @NotNull Long brandId,
    @NotBlank @Size(max = 230) String productName,
    @Size(max = 20000) String description,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal price,
    @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal salePrice,
    @NotNull @Min(0) Integer stock,
    @NotNull ProductStatus status,
    @Valid RacketRequest specification) {}
