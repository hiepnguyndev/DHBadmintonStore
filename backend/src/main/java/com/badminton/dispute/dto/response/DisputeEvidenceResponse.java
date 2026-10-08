package com.badminton.dispute.dto.response;

import java.time.*;

public record DisputeEvidenceResponse(
    Long evidenceId,
    Long disputeId,
    Long uploadedById,
    String fileUrl,
    String fileType,
    String description,
    LocalDateTime createdAt) {}
