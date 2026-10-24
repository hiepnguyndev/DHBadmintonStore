package com.badminton.audit.dto.response;

import java.time.*;

public record AuditLogResponse(
    Long auditId,
    Long userId,
    String action,
    String entityType,
    Long entityId,
    String oldData,
    String newData,
    String ipAddress,
    LocalDateTime createdAt) {}
