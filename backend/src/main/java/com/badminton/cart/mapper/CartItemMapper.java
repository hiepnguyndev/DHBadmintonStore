package com.badminton.cart.mapper;

import com.badminton.cart.dto.response.CartItemResponse;
import com.badminton.cart.entity.CartItem;

public final class CartItemMapper {
  private CartItemMapper() {}

  public static CartItemResponse toResponse(CartItem e) {
    return new CartItemResponse(
        e.getCartItemId(),
        e.getCart() == null ? null : e.getCart().getCartId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getQuantity(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
