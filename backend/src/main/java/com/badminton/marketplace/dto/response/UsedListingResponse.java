package com.badminton.marketplace.dto.response;

import com.badminton.marketplace.entity.ConditionGrade;
import com.badminton.marketplace.entity.ListingStatus;
import java.math.BigDecimal;
import java.time.*;

public record UsedListingResponse(
    Long listingId,
    Long sellerId,
    Long categoryId,
    Long brandId,
    String listingCode,
    String productName,
    String model,
    String description,
    BigDecimal originalPrice,
    BigDecimal sellingPrice,
    LocalDate purchaseDate,
    Integer usageMonths,
    ConditionGrade conditionGrade,
    BigDecimal conditionScore,
    String warrantyStatus,
    LocalDate warrantyUntil,
    ListingStatus status,
    Integer viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
