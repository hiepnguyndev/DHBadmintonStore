package com.badminton.audit.mapper;

import com.badminton.audit.dto.response.AuditLogResponse;
import com.badminton.audit.entity.AuditLog;

public final class AuditLogMapper {
  private AuditLogMapper() {}

  public static AuditLogResponse toResponse(AuditLog e) {
    return new AuditLogResponse(
        e.getAuditId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getAction(),
        e.getEntityType(),
        e.getEntityId(),
        e.getOldData(),
        e.getNewData(),
        e.getIpAddress(),
        e.getCreatedAt());
  }
}
