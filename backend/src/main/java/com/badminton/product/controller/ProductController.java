package com.badminton.product.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.product.dto.request.ProductRequest;
import com.badminton.product.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping("/api/products")
  public Object list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String weight,
      @RequestParam(required = false) String balance,
      @RequestParam(required = false) String flex,
      @RequestParam(required = false) String playStyle,
      @RequestParam(required = false) String skillLevel,
      @RequestParam(defaultValue = "false") boolean onSale,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sort) {
    return ApiResponse.ok(
        "Thành công",
        service.list(
            keyword,
            categoryId,
            brandId,
            minPrice,
            maxPrice,
            weight,
            balance,
            flex,
            playStyle,
            skillLevel,
            onSale,
            page,
            size,
            sort));
  }

  @GetMapping("/api/products/{id}")
  public Object get(@PathVariable Long id) {
    return ApiResponse.ok("Thành công", service.get(id));
  }

  @PostMapping("/api/products")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object create(@Valid @RequestBody ProductRequest r) {
    return ApiResponse.ok("Đã tạo sản phẩm", service.save(null, r));
  }

  @PutMapping("/api/products/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object update(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
    return ApiResponse.ok("Đã cập nhật", service.save(id, r));
  }

  @DeleteMapping("/api/products/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Object disable(@PathVariable Long id) {
    service.disable(id);
    return ApiResponse.ok("Đã vô hiệu hóa", null);
  }
}
