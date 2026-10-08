package com.badminton.auth.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 100) String fullName,
    @Email @Size(max = 150) String email,
    @Size(max = 30) String phone,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String confirmPassword) {}