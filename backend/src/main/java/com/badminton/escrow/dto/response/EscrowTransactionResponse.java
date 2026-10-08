package com.badminton.escrow.dto.response;

import com.badminton.escrow.entity.EscrowStatus;
import java.math.BigDecimal;
import java.time.*;

public record EscrowTransactionResponse(
    Long escrowId,
    Long marketOrderId,
    BigDecimal amount,
    EscrowStatus status,
    String paymentReference,
    LocalDateTime paidAt,
    LocalDateTime heldAt,
    LocalDateTime releasedAt,
    LocalDateTime refundedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
