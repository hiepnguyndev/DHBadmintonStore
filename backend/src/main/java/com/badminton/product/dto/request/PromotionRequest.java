package com.badminton.product.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PromotionRequest(
    @NotNull DiscountType discountType,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal value) {
  public enum DiscountType { PERCENT, SALE_PRICE }
}
