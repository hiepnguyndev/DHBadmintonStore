package com.badminton.user.dto.response;

import com.badminton.user.entity.Role;

public record UserResponse(
    Long id, String fullName, String email, String phone, String avatarUrl, Role role) {}
