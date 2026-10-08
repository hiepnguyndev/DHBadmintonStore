package com.badminton.auth.dto.response;

import com.badminton.user.dto.response.UserResponse;

public record AuthResponse(String accessToken, UserResponse user) {}
