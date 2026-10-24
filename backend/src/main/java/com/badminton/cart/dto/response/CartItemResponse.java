package com.badminton.cart.dto.response;

import java.time.*;

public record CartItemResponse(
    Long cartItemId,
    Long cartId,
    Long productId,
    Integer quantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
