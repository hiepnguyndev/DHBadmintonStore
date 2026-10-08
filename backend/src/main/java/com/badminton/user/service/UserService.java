package com.badminton.user.service;

import com.badminton.exception.BusinessException;
import com.badminton.security.PasswordPolicy;
import com.badminton.user.dto.request.*;
import com.badminton.user.dto.response.*;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.*;
import com.badminton.user.util.PhoneNumberNormalizer;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserRepository users;
  private final UserMapper mapper;
  private final PasswordEncoder passwords;

  public UserService(UserRepository users, UserMapper mapper, PasswordEncoder passwords) {
    this.users = users;
    this.mapper = mapper;
    this.passwords = passwords;
  }

  private User activeUser(Long id, boolean lock) {
    User u =
        (lock ? users.findByIdForUpdate(id) : users.findById(id))
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));
    if (u.getStatus() != UserStatus.ACTIVE)
      throw new BusinessException(HttpStatus.FORBIDDEN, "USER_LOCKED", "Tài khoản không khả dụng");
    return u;
  }

  @Transactional(readOnly = true)
  public UserResponse me(Long id) {
    return mapper.toResponse(activeUser(id, false));
  }

  @Transactional
  public UserResponse update(Long id, UpdateUserRequest r) {
    User u = activeUser(id, true);
    String phone = PhoneNumberNormalizer.normalizeOptional(r.phone());
    if (phone != null && !phone.equals(u.getPhone()) && users.existsByPhone(phone))
      throw new BusinessException(
          HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "Số điện thoại đã được đăng ký");
    u.updateProfile(r.fullName().trim(), phone, r.avatarUrl());
    return mapper.toResponse(u);
  }

  @Transactional
  public void changePassword(Long id, ChangePasswordRequest r) {
    User u = activeUser(id, true);
    if (r.currentPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
        || !passwords.matches(r.currentPassword(), u.getPasswordHash()))
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "CURRENT_PASSWORD_INVALID", "Mật khẩu hiện tại không đúng");
    PasswordPolicy.validateNew(r.newPassword(), r.confirmPassword());
    u.changePassword(passwords.encode(r.newPassword()));
  }
}
