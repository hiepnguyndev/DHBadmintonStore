package com.badminton.marketplace.controller;

import com.badminton.common.dto.ApiResponse;
import com.badminton.marketplace.service.ListingImageService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ListingImageController {
  private final ListingImageService service;

  public ListingImageController(ListingImageService service) {
    this.service = service;
  }

  @PostMapping(value = "/api/marketplace/listings/{id}/images", consumes = "multipart/form-data")
  public ApiResponse<?> upload(
      @PathVariable Long id,
      @RequestParam org.springframework.web.multipart.MultipartFile file,
      @RequestParam(defaultValue = "GENERAL")
          com.badminton.marketplace.entity.ImageType imageType) {
    return ApiResponse.ok(service.upload(id, file, imageType));
  }
}
