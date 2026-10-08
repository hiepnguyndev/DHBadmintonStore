package com.badminton.cart.mapper;

import com.badminton.cart.dto.response.CartResponse;
import com.badminton.cart.entity.Cart;

public final class CartMapper {
  private CartMapper() {}

  public static CartResponse toResponse(Cart e) {
    return new CartResponse(
        e.getCartId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
