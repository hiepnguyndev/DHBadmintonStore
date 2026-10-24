package com.badminton.dispute.mapper;

import com.badminton.dispute.dto.response.DisputeEvidenceResponse;
import com.badminton.dispute.entity.DisputeEvidence;

public final class DisputeEvidenceMapper {
  private DisputeEvidenceMapper() {}

  public static DisputeEvidenceResponse toResponse(DisputeEvidence e) {
    return new DisputeEvidenceResponse(
        e.getEvidenceId(),
        e.getDispute() == null ? null : e.getDispute().getDisputeId(),
        e.getUploadedBy() == null ? null : e.getUploadedBy().getUserId(),
        e.getFileUrl(),
        e.getFileType(),
        e.getDescription(),
        e.getCreatedAt());
  }
}
