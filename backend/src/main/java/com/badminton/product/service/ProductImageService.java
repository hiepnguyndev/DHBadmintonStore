package com.badminton.product.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductImageService {
  private final com.badminton.product.repository.ProductRepository products;
  private final com.badminton.product.repository.ProductImageRepository images;
  private final com.badminton.common.storage.ImageStorage storage;

  public ProductImageService(
      com.badminton.product.repository.ProductRepository products,
      com.badminton.product.repository.ProductImageRepository images,
      com.badminton.common.storage.ImageStorage storage) {
    this.products = products;
    this.images = images;
    this.storage = storage;
  }

  public Object upload(Long id, org.springframework.web.multipart.MultipartFile file) {
    var p = found(products.lock(id), "PRODUCT_NOT_FOUND");
    long count = images.count(eq("product.productId", id));
    bad(count < 10, "Tối đa 10 ảnh mỗi sản phẩm");
    var saved = storage.store(file, false);
    var image = new com.badminton.product.entity.ProductImage();
    image.setProduct(p);
    image.setImageUrl("/media/" + saved.key());
    image.setSortOrder((int) count);
    image.setIsPrimary(count == 0);
    if (count == 0) p.setThumbnailUrl(image.getImageUrl());
    return com.badminton.product.mapper.ProductImageMapper.toResponse(images.saveAndFlush(image));
  }
}
