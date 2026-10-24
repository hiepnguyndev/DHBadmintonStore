package com.badminton.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final CustomUserDetailsService users;
  private final SecurityErrorWriter errors;

  public JwtAuthenticationFilter(
      JwtService jwt, CustomUserDetailsService users, SecurityErrorWriter errors) {
    this.jwt = jwt;
    this.users = users;
    this.errors = errors;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null) {
      if (!header.startsWith("Bearer ")) {
        errors.write(response, 401, "AUTH_INVALID_TOKEN", "Phiên đăng nhập không hợp lệ");
        return;
      }
      try {
        CurrentUser user = users.loadActiveUser(jwt.userId(header.substring(7)));
        var auth =
            new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (JwtException | AuthenticationException | IllegalArgumentException e) {
        SecurityContextHolder.clearContext();
        errors.write(
            response, 401, "AUTH_INVALID_TOKEN", "Phiên đăng nhập hết hạn hoặc không hợp lệ");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
