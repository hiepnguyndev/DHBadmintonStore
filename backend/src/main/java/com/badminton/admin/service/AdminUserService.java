package com.badminton.admin.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.user.dto.response.UserResponse;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserService {
  private final com.badminton.user.repository.UserRepository users;
  private final UserMapper mapper = new UserMapper();
  private final Access access;
  private final com.badminton.audit.service.AuditService audit;

  public AdminUserService(
      com.badminton.user.repository.UserRepository users,
      Access access,
      com.badminton.audit.service.AuditService audit) {
    this.users = users;
    this.access = access;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public PageResponse<UserResponse> list(String keyword, int page, int size) {
    return PageResponse.from(
        users
            .findAll(
                Queries.<User>contains("fullName", keyword)
                    .or(contains("email", keyword))
                    .and((root, query, cb) -> cb.notEqual(root.get("status"), UserStatus.DELETED)),
                page(page, size, null, "createdAt", Set.of("createdAt", "email", "fullName")))
            .map(mapper::toResponse));
  }

  @Transactional(readOnly = true)
  public UserResponse detail(Long id) {
    return mapper.toResponse(found(users.findById(id), "USER_NOT_FOUND"));
  }

  public UserResponse lock(Long id, boolean locked) {
    bad(!access.current().id().equals(id), "Không tự khóa tài khoản quản trị hiện tại");
    var u = found(users.findByIdForUpdate(id), "USER_NOT_FOUND");
    require(
        u.getStatus() == UserStatus.ACTIVE || u.getStatus() == UserStatus.LOCKED,
        "USER_INVALID_STATUS",
        "Trạng thái tài khoản không hỗ trợ thao tác này");
    var before = u.getStatus();
    u.setStatus(locked ? UserStatus.LOCKED : UserStatus.ACTIVE);
    audit.record(locked ? "LOCK_USER" : "UNLOCK_USER", "User", id, before, u.getStatus());
    return mapper.toResponse(u);
  }
}
