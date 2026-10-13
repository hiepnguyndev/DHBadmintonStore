package com.badminton.news.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.news.dto.ShopPostRequest;
import com.badminton.news.entity.ShopPost;
import com.badminton.news.service.ShopPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShopPostController {
  private final ShopPostService service;
  public AdminShopPostController(ShopPostService service) { this.service = service; }

  @GetMapping
  public Object list(@RequestParam(required = false) String keyword,
      @RequestParam(required = false) ShopPost.Category category,
      @RequestParam(required = false) ShopPost.Status status,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
    return ApiResponse.ok("Thành công", service.list(true, keyword, category, status, page, size));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public Object create(@Valid @RequestPart("post") ShopPostRequest request,
      @RequestPart(value = "cover", required = false) MultipartFile cover) {
    return ApiResponse.ok("Đã lưu bài viết", service.save(null, request, cover));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Object update(@PathVariable Long id, @Valid @RequestPart("post") ShopPostRequest request,
      @RequestPart(value = "cover", required = false) MultipartFile cover) {
    return ApiResponse.ok("Đã lưu bài viết", service.save(id, request, cover));
  }

  @DeleteMapping("/{id}")
  public Object archive(@PathVariable Long id) { return ApiResponse.ok("Đã lưu trữ bài viết", service.archive(id)); }
}
