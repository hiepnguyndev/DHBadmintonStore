package com.badminton.common.support;

import com.badminton.exception.BusinessException;
import com.badminton.security.CurrentUser;
import com.badminton.user.entity.*;
import com.badminton.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Access {
  private final UserRepository users;

  public Access(UserRepository users) {
    this.users = users;
  }

  public CurrentUser current() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !(a.getPrincipal() instanceof CurrentUser u))
      throw new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Vui lòng đăng nhập");
    return u;
  }

  public CurrentUser optional() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null && a.getPrincipal() instanceof CurrentUser u ? u : null;
  }

  public void owner(Long id) {
    if (!current().id().equals(id))
      throw new BusinessException(
          HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Không có quyền với dữ liệu này");
  }

  public void ownerOrAdmin(Long id) {
    if (current().role() != Role.ADMIN) owner(id);
  }

  public User user() {
    return Checks.found(users.findById(current().id()), "USER_NOT_FOUND");
  }

  public User lockUser() {
    User u = Checks.found(users.findByIdForUpdate(current().id()), "USER_NOT_FOUND");
    Checks.require(u.getStatus() == UserStatus.ACTIVE, "USER_LOCKED", "Tài khoản không khả dụng");
    return u;
  }
}
