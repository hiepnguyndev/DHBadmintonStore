package com.badminton.marketorder.dto.response;

public record MarketOrderDetail(
    MarketplaceOrderResponse order,
    com.badminton.escrow.dto.response.EscrowTransactionResponse escrow,
    com.badminton.shipment.dto.response.ShipmentResponse shipment,
    Long disputeId,
    boolean rated) {}
