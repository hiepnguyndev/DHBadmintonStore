package com.badminton.security;

import com.badminton.user.entity.UserStatus;
import com.badminton.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService {
  private final UserRepository users;

  public CustomUserDetailsService(UserRepository users) {
    this.users = users;
  }

  // Resolve current status and role from the database on every authenticated request.
  public CurrentUser loadActiveUser(Long id) {
    var u =
        users.findById(id).orElseThrow(() -> new BadCredentialsException("Invalid authentication"));
    if (u.getStatus() != UserStatus.ACTIVE) throw new BadCredentialsException("Inactive account");
    return new CurrentUser(u.getUserId(), u.getRole());
  }
}
