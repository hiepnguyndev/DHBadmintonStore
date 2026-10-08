package com.badminton.cart.dto.response;

import java.time.*;

public record CartResponse(
    Long cartId, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {}
