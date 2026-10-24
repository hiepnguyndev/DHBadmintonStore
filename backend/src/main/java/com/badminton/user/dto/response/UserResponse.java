package com.badminton.user.dto.response;

import com.badminton.user.entity.Role;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UserResponse(
    Long id, String fullName, String email, String phone, String avatarUrl, Role role,
    LocalDate dateOfBirth, String gender, Integer heightCm, BigDecimal weightKg) {
  public UserResponse(Long id, String fullName, String email, String phone, String avatarUrl, Role role) {
    this(id, fullName, email, phone, avatarUrl, role, null, null, null, null);
  }
}
