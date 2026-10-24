package com.badminton.audit.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.audit.dto.response.*;
import com.badminton.audit.entity.*;
import com.badminton.audit.mapper.*;
import com.badminton.audit.repository.*;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.user.repository.UserRepository;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditService {
  private final AuditLogRepository logs;
  private final Access access;
  private final UserRepository users;
  private final com.fasterxml.jackson.databind.ObjectMapper json;

  public AuditService(
      AuditLogRepository logs,
      Access access,
      UserRepository users,
      com.fasterxml.jackson.databind.ObjectMapper json) {
    this.logs = logs;
    this.access = access;
    this.users = users;
    this.json = json;
  }

  public void record(String action, String entityType, Long entityId, Object before, Object after) {
    AuditLog log = new AuditLog();
    if (access.optional() != null) log.setUser(users.getReferenceById(access.current().id()));
    log.setAction(action);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    try {
      log.setOldData(before == null ? null : json.writeValueAsString(before));
      log.setNewData(after == null ? null : json.writeValueAsString(after));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
    logs.saveAndFlush(log);
  }

  @Transactional(readOnly = true)
  public PageResponse<AuditLogResponse> list(int page, int size) {
    return PageResponse.from(
        logs.findAll(page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(AuditLogMapper::toResponse));
  }
}
