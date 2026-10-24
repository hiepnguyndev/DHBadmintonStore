package com.badminton.common.dto;

import java.util.Map;

public record ApiError(boolean success, String code, String message, Map<String, String> errors) {
  public static ApiError of(String code, String message) {
    return new ApiError(false, code, message, null);
  }
}
