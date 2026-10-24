package com.badminton.common.support;

import com.badminton.exception.BusinessException;
import java.math.*;
import java.util.*;
import org.springframework.http.HttpStatus;

public final class Checks {
  private Checks() {}

  public static void require(boolean valid, String code, String message) {
    if (!valid) throw new BusinessException(HttpStatus.CONFLICT, code, message);
  }

  public static void bad(boolean valid, String message) {
    if (!valid) throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
  }

  public static <T> T found(Optional<T> value, String code) {
    return value.orElseThrow(
        () -> new BusinessException(HttpStatus.NOT_FOUND, code, "Không tìm thấy dữ liệu"));
  }

  public static BigDecimal money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  public static String code(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
  }

  public static String slug(String name) {
    return java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "")
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }
}
