package com.badminton.cart.dto.response;

import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.dto.response.ProductVariantResponse;
import java.math.BigDecimal;
import java.util.List;

public record CartView(Long cartId, List<Item> items, BigDecimal subtotalAmount) {
  public record Item(
      Long cartItemId,
      ProductResponse product,
      ProductVariantResponse variant,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal totalPrice) {}
}
