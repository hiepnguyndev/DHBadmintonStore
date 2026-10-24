package com.badminton.cart.dto.request;

import com.badminton.common.enums.*;
import com.badminton.order.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record CartItemRequest(
    @NotNull Long productId, Long variantId, @NotNull @Min(1) @Max(10000) Integer quantity) {}
