package com.badminton.rating.dto.response;

import java.time.*;

public record SellerRatingResponse(
    Long ratingId,
    Long marketOrderId,
    Long sellerId,
    Long buyerId,
    Byte rating,
    String comment,
    LocalDateTime createdAt) {}
