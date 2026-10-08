package com.badminton.escrow.mapper;

import com.badminton.escrow.dto.response.EscrowTransactionResponse;
import com.badminton.escrow.entity.EscrowTransaction;

public final class EscrowTransactionMapper {
  private EscrowTransactionMapper() {}

  public static EscrowTransactionResponse toResponse(EscrowTransaction e) {
    return new EscrowTransactionResponse(
        e.getEscrowId(),
        e.getMarketOrder() == null ? null : e.getMarketOrder().getMarketOrderId(),
        e.getAmount(),
        e.getStatus(),
        e.getPaymentReference(),
        e.getPaidAt(),
        e.getHeldAt(),
        e.getReleasedAt(),
        e.getRefundedAt(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
