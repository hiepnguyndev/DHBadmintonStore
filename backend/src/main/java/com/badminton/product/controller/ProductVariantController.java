package com.badminton.product.controller;
import com.badminton.common.dto.ApiResponse;
import com.badminton.product.dto.request.ProductVariantRequest;
import com.badminton.product.service.ProductVariantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
public class ProductVariantController {
 private final ProductVariantService service;
 public ProductVariantController(ProductVariantService service){this.service=service;}
 @GetMapping("/api/products/{id}/variants") public Object list(@PathVariable Long id){return ApiResponse.ok("Thành công",service.list(id,false));}
 @GetMapping("/api/admin/products/{id}/variants") @PreAuthorize("hasRole('ADMIN')") public Object adminList(@PathVariable Long id){return ApiResponse.ok("Thành công",service.list(id,true));}
 @PostMapping("/api/admin/products/{id}/variants") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED) public Object create(@PathVariable Long id,@Valid @RequestBody ProductVariantRequest r){return ApiResponse.ok("Đã tạo biến thể",service.create(id,r));}
 @PutMapping("/api/admin/product-variants/{variantId}") @PreAuthorize("hasRole('ADMIN')") public Object update(@PathVariable Long variantId,@Valid @RequestBody ProductVariantRequest r){return ApiResponse.ok("Đã cập nhật biến thể",service.update(variantId,r));}
}
