package com.badminton.marketorder.mapper;

import com.badminton.marketorder.dto.response.MarketplaceOrderResponse;
import com.badminton.marketorder.entity.MarketplaceOrder;

public final class MarketplaceOrderMapper {
  private MarketplaceOrderMapper() {}

  public static MarketplaceOrderResponse toResponse(MarketplaceOrder e) {
    return new MarketplaceOrderResponse(
        e.getMarketOrderId(),
        e.getOrderCode(),
        e.getListing() == null ? null : e.getListing().getListingId(),
        e.getBuyer() == null ? null : e.getBuyer().getUserId(),
        e.getSeller() == null ? null : e.getSeller().getUserId(),
        e.getItemPrice(),
        e.getShippingFee(),
        e.getPlatformFee(),
        e.getTotalAmount(),
        e.getOrderStatus(),
        e.getReceiverName(),
        e.getReceiverPhone(),
        e.getShippingAddress(),
        e.getCancelledByType(),
        e.getCancellationReason(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
