package com.badminton.favorite.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.favorite.entity.*;
import com.badminton.favorite.repository.*;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.repository.ProductRepository;
import com.badminton.product.service.ProductService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteService {
  private final FavoriteRepository favorites;
  private final ProductRepository products;
  private final ProductService catalog;
  private final Access access;

  public FavoriteService(
      FavoriteRepository favorites,
      ProductRepository products,
      ProductService catalog,
      Access access) {
    this.favorites = favorites;
    this.products = products;
    this.catalog = catalog;
    this.access = access;
  }

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> list(int page, int size) {
    return PageResponse.from(
        favorites
            .findAll(
                eq("user.userId", access.current().id()),
                page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(f -> catalog.view(f.getProduct())));
  }

  public void add(Long id) {
    var u = access.lockUser();
    var p = found(products.findById(id), "PRODUCT_NOT_FOUND");
    if (favorites.findByUserUserIdAndProductProductId(u.getUserId(), id).isPresent()) return;
    Favorite f = new Favorite();
    f.setUser(u);
    f.setProduct(p);
    favorites.save(f);
  }

  public void remove(Long id) {
    access.lockUser();
    favorites
        .findByUserUserIdAndProductProductId(access.current().id(), id)
        .ifPresent(favorites::delete);
  }
}
