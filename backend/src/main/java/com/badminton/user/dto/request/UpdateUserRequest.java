package com.badminton.user.dto.request;

import jakarta.validation.constraints.*;

public record UpdateUserRequest(
    @NotBlank @Size(max = 100) String fullName,
    @Size(max = 20) String phone,
    @Size(max = 500)
        @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Avatar phải là URL HTTP/HTTPS")
        String avatarUrl) {}
