package com.badminton.user.dto.request;

import jakarta.validation.constraints.*;

public record AddressRequest(
    @NotBlank @Size(max = 100) String receiverName,
    @NotBlank @Size(max = 20) String receiverPhone,
    @NotBlank @Size(max = 100) String province,
    @NotBlank @Size(max = 100) String ward,
    @NotBlank @Size(max = 255) String detailAddress,
    @NotNull Boolean isDefault) {}
