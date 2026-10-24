package com.badminton.user.util;

import com.badminton.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class PhoneNumberNormalizer {
  private PhoneNumberNormalizer() {}

  public static String normalizeOptional(String value) {
    return value == null || value.isBlank() ? null : normalize(value);
  }

  public static String normalize(String value) {
    String phone = value.trim().replaceAll("[\\s().-]", "");
    if (phone.matches("^0[0-9]{9}$")) phone = "+84" + phone.substring(1);
    else if (phone.matches("^84[0-9]{9}$")) phone = "+" + phone;
    if (!phone.matches("^\\+[1-9][0-9]{7,14}$"))
      throw new BusinessException(
          HttpStatus.BAD_REQUEST,
          "PHONE_INVALID",
          "Số điện thoại chưa đúng định dạng. Dùng số Việt Nam 10 chữ số hoặc mã quốc tế.");
    return phone;
  }
}
