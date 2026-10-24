package com.badminton.auth.service;

import com.badminton.auth.dto.request.*;
import com.badminton.auth.dto.response.*;
import com.badminton.exception.BusinessException;
import com.badminton.security.*;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.UserRepository;
import com.badminton.user.util.PhoneNumberNormalizer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtService jwt;
  private final UserMapper mapper;
  private final String dummyHash;

  public AuthService(
      UserRepository users, PasswordEncoder passwords, JwtService jwt, UserMapper mapper) {
    this.users = users;
    this.passwords = passwords;
    this.jwt = jwt;
    this.mapper = mapper;
    dummyHash = passwords.encode(java.util.UUID.randomUUID().toString());
  }

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    PasswordPolicy.validateNew(request.password(), request.confirmPassword());
    String email = normalizeEmail(request.email());
    String phone = PhoneNumberNormalizer.normalizeOptional(request.phone());
    if ((email == null) == (phone == null))
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "CONTACT_REQUIRED",
          "Chọn đúng một phương thức liên hệ: email hoặc số điện thoại.");
    if (email != null && users.existsByEmail(email))
      throw new BusinessException(
          HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được đăng ký");
    if (phone != null && users.existsByPhone(phone))
      throw new BusinessException(
          HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "Số điện thoại đã được đăng ký");

    User user =
        User.register(
            request.fullName().trim(), email, passwords.encode(request.password()), phone);
    users.saveAndFlush(user);
    return new RegisterResponse(user.getUserId(), user.getEmail());
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    String phone = PhoneNumberNormalizer.normalizeOptional(request.phone());
    if ((email == null) == (phone == null))
      throw invalidCredentials();
    Optional<User> candidate =
        email != null ? users.findByEmail(email) : users.findByPhone(phone);
    String hash = candidate.map(User::getPasswordHash).orElse(dummyHash);
    boolean valid =
        request.password().getBytes(StandardCharsets.UTF_8).length <= 72
            && passwords.matches(request.password(), hash);
    if (candidate.isEmpty() || !valid) throw invalidCredentials();
    User user = candidate.get();
    if (user.getStatus() != UserStatus.ACTIVE)
      throw new BusinessException(
          HttpStatus.FORBIDDEN, "USER_LOCKED", "Tài khoản không được phép đăng nhập");
    return new AuthResponse(jwt.issue(user), mapper.toResponse(user));
  }

  private String normalizeEmail(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private BusinessException invalidCredentials() {
    return new BusinessException(
        HttpStatus.UNAUTHORIZED,
        "AUTH_INVALID_CREDENTIALS",
        "Email/số điện thoại hoặc mật khẩu không đúng.");
  }
}
