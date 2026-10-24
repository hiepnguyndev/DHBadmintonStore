package com.badminton.marketorder.dto.response;

import com.badminton.marketorder.entity.MarketOrderStatus;
import java.math.BigDecimal;
import java.time.*;

public record MarketplaceOrderResponse(
    Long marketOrderId,
    String orderCode,
    Long listingId,
    Long buyerId,
    Long sellerId,
    BigDecimal itemPrice,
    BigDecimal shippingFee,
    BigDecimal platformFee,
    BigDecimal totalAmount,
    MarketOrderStatus orderStatus,
    String receiverName,
    String receiverPhone,
    String shippingAddress,
    String cancelledByType,
    String cancellationReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
