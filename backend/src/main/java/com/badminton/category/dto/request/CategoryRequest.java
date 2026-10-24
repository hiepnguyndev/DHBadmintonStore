package com.badminton.category.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record CategoryRequest(
    @NotBlank @Size(max = 80) String categoryName,
    @Size(max = 10000) String description,
    com.badminton.category.entity.CategoryStatus status) {}
