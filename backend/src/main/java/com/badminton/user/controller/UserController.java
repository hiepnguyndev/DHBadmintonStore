package com.badminton.user.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.security.CurrentUser;
import com.badminton.user.dto.request.*;
import com.badminton.user.dto.response.UserResponse;
import com.badminton.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService users;

  public UserController(UserService users) {
    this.users = users;
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> me(@AuthenticationPrincipal CurrentUser user) {
    return ApiResponse.ok("Thành công", users.me(user.id()));
  }

  @PutMapping("/me")
  public ApiResponse<UserResponse> update(
      @AuthenticationPrincipal CurrentUser user, @Valid @RequestBody UpdateUserRequest request) {
    return ApiResponse.ok("Đã cập nhật hồ sơ", users.update(user.id(), request));
  }

  @PutMapping("/change-password")
  public ApiResponse<Void> changePassword(
      @AuthenticationPrincipal CurrentUser user,
      @Valid @RequestBody ChangePasswordRequest request) {
    users.changePassword(user.id(), request);
    return ApiResponse.ok("Đã đổi mật khẩu", null);
  }
}
