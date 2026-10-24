package com.badminton.auth.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @Email @Size(max = 150) String email,
    @Size(max = 30) String phone,
    @NotBlank @Size(max = 72) String password) {
  public LoginRequest(String email, String password) {
    this(email, null, password);
  }
}