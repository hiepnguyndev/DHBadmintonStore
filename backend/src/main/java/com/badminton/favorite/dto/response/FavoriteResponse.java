package com.badminton.favorite.dto.response;

import java.time.*;

public record FavoriteResponse(
    Long favoriteId, Long userId, Long productId, LocalDateTime createdAt) {}
