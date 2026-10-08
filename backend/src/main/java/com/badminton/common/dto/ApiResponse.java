package com.badminton.common.dto;

public record ApiResponse<T>(boolean success, String message, T data) {
  public static <T> ApiResponse<T> ok(T data) {
    return ok("Thành công", data);
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }
}
