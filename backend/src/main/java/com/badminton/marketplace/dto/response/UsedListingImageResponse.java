package com.badminton.marketplace.dto.response;

import com.badminton.marketplace.entity.ImageType;
import java.time.*;

public record UsedListingImageResponse(
    Long imageId,
    Long listingId,
    String imageUrl,
    ImageType imageType,
    Integer sortOrder,
    LocalDateTime createdAt) {}
