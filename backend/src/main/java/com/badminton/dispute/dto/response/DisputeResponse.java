package com.badminton.dispute.dto.response;

import com.badminton.dispute.entity.DisputeReason;
import com.badminton.dispute.entity.DisputeStatus;
import com.badminton.dispute.entity.Resolution;
import java.time.*;

public record DisputeResponse(
    Long disputeId,
    Long marketOrderId,
    Long createdById,
    DisputeReason reason,
    String description,
    DisputeStatus status,
    Resolution resolution,
    String resolutionNote,
    Long resolvedById,
    LocalDateTime resolvedAt,
    LocalDateTime createdAt) {}
