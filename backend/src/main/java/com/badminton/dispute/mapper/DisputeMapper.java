package com.badminton.dispute.mapper;

import com.badminton.dispute.dto.response.DisputeResponse;
import com.badminton.dispute.entity.Dispute;

public final class DisputeMapper {
  private DisputeMapper() {}

  public static DisputeResponse toResponse(Dispute e) {
    return new DisputeResponse(
        e.getDisputeId(),
        e.getMarketOrder() == null ? null : e.getMarketOrder().getMarketOrderId(),
        e.getCreatedBy() == null ? null : e.getCreatedBy().getUserId(),
        e.getReason(),
        e.getDescription(),
        e.getStatus(),
        e.getResolution(),
        e.getResolutionNote(),
        e.getResolvedBy() == null ? null : e.getResolvedBy().getUserId(),
        e.getResolvedAt(),
        e.getCreatedAt());
  }
}
