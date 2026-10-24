package com.badminton.security;

import com.badminton.user.entity.Role;

public record CurrentUser(Long id, Role role) {}
