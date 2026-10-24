package com.badminton.favorite.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.favorite.service.FavoriteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class FavoriteController {
  private final FavoriteService service;

  public FavoriteController(FavoriteService service) {
    this.service = service;
  }

  @GetMapping("/api/favorites")
  @PreAuthorize("hasRole('USER')")
  public Object list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok("Thành công", service.list(page, size));
  }

  @PostMapping("/api/favorites/{id}")
  @PreAuthorize("hasRole('USER')")
  public Object add(@PathVariable Long id) {
    service.add(id);
    return ApiResponse.ok("Đã lưu yêu thích", null);
  }

  @DeleteMapping("/api/favorites/{id}")
  @PreAuthorize("hasRole('USER')")
  public Object remove(@PathVariable Long id) {
    service.remove(id);
    return ApiResponse.ok("Đã xóa yêu thích", null);
  }
}
