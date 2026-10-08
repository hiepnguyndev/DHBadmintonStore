package com.badminton.auth.controller;

import com.badminton.auth.dto.request.*;
import com.badminton.auth.dto.response.*;
import com.badminton.auth.service.AuthService;
import com.badminton.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  @SecurityRequirements
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok("Đăng ký thành công", auth.register(request)));
  }

  @PostMapping("/login")
  @SecurityRequirements
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok("Đăng nhập thành công", auth.login(request));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout() {
    return ApiResponse.ok(
        "Hãy xóa token ở client. Access Token đã phát hành vẫn có hiệu lực đến khi hết hạn.", null);
  }
}
