package com.badminton.security;

import com.badminton.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

public final class PasswordPolicy {
  private PasswordPolicy() {}

  public static void validateNew(String password, String confirmation) {
    if (!password.equals(confirmation))
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp");
    if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "PASSWORD_LENGTH",
          "Mật khẩu cần ít nhất 8 ký tự và tối đa 72 byte UTF-8");
  }
}
