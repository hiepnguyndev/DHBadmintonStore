package com.badminton.news.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.news.entity.ShopPost;
import com.badminton.news.service.ShopPostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class ShopPostController {
  private final ShopPostService service;
  public ShopPostController(ShopPostService service) { this.service = service; }

  @GetMapping
  public Object list(@RequestParam(required = false) ShopPost.Category category,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
    return ApiResponse.ok("Thành công", service.list(false, null, category, null, page, size));
  }

  @GetMapping("/{id}")
  public Object get(@PathVariable Long id) { return ApiResponse.ok("Thành công", service.get(id)); }
}
