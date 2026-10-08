package com.badminton.user.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.security.CurrentUser;
import com.badminton.user.dto.request.AddressRequest;
import com.badminton.user.dto.response.AddressResponse;
import com.badminton.user.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
  private final AddressService addresses;

  public AddressController(AddressService addresses) {
    this.addresses = addresses;
  }

  @GetMapping
  public ApiResponse<List<AddressResponse>> list(@AuthenticationPrincipal CurrentUser user) {
    return ApiResponse.ok("Thành công", addresses.list(user.id()));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<AddressResponse>> create(
      @AuthenticationPrincipal CurrentUser user, @Valid @RequestBody AddressRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok("Đã thêm địa chỉ", addresses.create(user.id(), r)));
  }

  @PutMapping("/{id}")
  public ApiResponse<AddressResponse> update(
      @AuthenticationPrincipal CurrentUser user,
      @PathVariable Long id,
      @Valid @RequestBody AddressRequest r) {
    return ApiResponse.ok("Đã cập nhật địa chỉ", addresses.update(user.id(), id, r));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(
      @AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
    addresses.delete(user.id(), id);
    return ApiResponse.ok("Đã xóa địa chỉ", null);
  }
}
