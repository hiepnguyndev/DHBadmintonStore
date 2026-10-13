package com.badminton.user.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateUserRequest(
    @NotBlank @Size(max = 100) String fullName,
    @Size(max = 20) String phone,
    @Size(max = 500)
        @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Avatar phải là URL HTTP/HTTPS")
        String avatarUrl,
    @Past LocalDate dateOfBirth,
    @Pattern(regexp = "^(|MALE|FEMALE|OTHER)$") String gender,
    @Min(80) @Max(260) Integer heightCm,
    @DecimalMin("20") @DecimalMax("300") @Digits(integer = 3, fraction = 2) BigDecimal weightKg) {
  public UpdateUserRequest(String fullName, String phone, String avatarUrl) {
    this(fullName, phone, avatarUrl, null, null, null, null);
  }
}
