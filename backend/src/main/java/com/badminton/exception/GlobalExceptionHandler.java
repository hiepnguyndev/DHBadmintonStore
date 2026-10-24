package com.badminton.exception;

import com.badminton.common.dto.ApiError;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiError> business(BusinessException e) {
    return ResponseEntity.status(e.getStatus()).body(ApiError.of(e.getCode(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
    var fields = new LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(new ApiError(false, "VALIDATION_ERROR", "Dữ liệu không hợp lệ", fields));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.web.multipart.support.MissingServletRequestPartException.class
  })
  ResponseEntity<ApiError> malformed(Exception e) {
    return ResponseEntity.badRequest()
        .body(
            ApiError.of(
                "INVALID_REQUEST", "Request không hợp lệ hoặc chứa trường không được phép"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> conflict(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(ApiError.of("DATA_CONFLICT", "Dữ liệu bị trùng hoặc đang được sử dụng"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied(AccessDeniedException e) {
    return ResponseEntity.status(403).body(ApiError.of("ACCESS_DENIED", "Không có quyền truy cập"));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ApiError> missing(NoResourceFoundException e) {
    return ResponseEntity.status(404).body(ApiError.of("NOT_FOUND", "Không tìm thấy tài nguyên"));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiError> method(HttpRequestMethodNotSupportedException e) {
    return ResponseEntity.status(405)
        .body(ApiError.of("METHOD_NOT_ALLOWED", "Phương thức không được hỗ trợ"));
  }

  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  ResponseEntity<ApiError> uploadTooLarge(Exception e) {
    return ResponseEntity.status(413).body(ApiError.of("UPLOAD_TOO_LARGE", "Ảnh tối đa 5 MB"));
  }

  @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
  ResponseEntity<ApiError> locked(Exception e) {
    return ResponseEntity.status(409)
        .body(
            ApiError.of(
                "CONCURRENT_UPDATE", "Dữ liệu đang được cập nhật. Vui lòng tải lại và thử lại."));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception e) {
    // Do not log request bodies, JDBC values, passwords or tokens.
    log.error("Unhandled exception type: {}", e.getClass().getName());
    return ResponseEntity.internalServerError()
        .body(ApiError.of("INTERNAL_ERROR", "Có lỗi hệ thống. Vui lòng thử lại sau."));
  }
}
