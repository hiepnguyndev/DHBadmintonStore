package com.badminton.notification.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.notification.dto.response.*;
import com.badminton.notification.entity.*;
import com.badminton.notification.mapper.*;
import com.badminton.notification.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
  private final NotificationRepository notifications;
  private final Access access;

  public NotificationService(NotificationRepository notifications, Access access) {
    this.notifications = notifications;
    this.access = access;
  }

  public void send(
      com.badminton.user.entity.User user,
      String type,
      String title,
      String content,
      String referenceType,
      Long referenceId) {
    Notification n = new Notification();
    n.setUser(user);
    n.setType(type);
    n.setTitle(title);
    n.setContent(content);
    n.setReferenceType(referenceType);
    n.setReferenceId(referenceId);
    n.setIsRead(false);
    notifications.save(n);
  }

  @Transactional(readOnly = true)
  public PageResponse<NotificationResponse> list(int page, int size) {
    return PageResponse.from(
        notifications
            .findAll(
                eq("user.userId", access.current().id()),
                page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(NotificationMapper::toResponse));
  }

  public void read(Long id) {
    var n = found(notifications.lock(id), "NOTIFICATION_NOT_FOUND");
    access.owner(n.getUser().getUserId());
    n.setIsRead(true);
  }

  public void readAll() {
    notifications.findAll(eq("user.userId", access.current().id())).forEach(n -> n.setIsRead(true));
  }
}
