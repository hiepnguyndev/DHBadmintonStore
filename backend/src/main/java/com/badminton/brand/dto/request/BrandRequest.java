package com.badminton.brand.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record BrandRequest(
    @NotBlank @Size(max = 80) String brandName,
    @Size(max = 10000) String description,
    @Size(max = 500) String logoUrl,
    com.badminton.brand.entity.BrandStatus status) {}
