package com.badminton.order.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record OrderItemResponse(
    Long orderItemId,
    Long orderId,
    Long productId,
    String productName,
    String productImage,
    Long variantId,
    String variantSku,
    String variantColor,
    String variantSize,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice) {}
