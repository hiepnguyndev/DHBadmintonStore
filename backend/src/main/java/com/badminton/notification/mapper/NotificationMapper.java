package com.badminton.notification.mapper;

import com.badminton.notification.dto.response.NotificationResponse;
import com.badminton.notification.entity.Notification;

public final class NotificationMapper {
  private NotificationMapper() {}

  public static NotificationResponse toResponse(Notification e) {
    return new NotificationResponse(
        e.getNotificationId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getType(),
        e.getTitle(),
        e.getContent(),
        e.getReferenceType(),
        e.getReferenceId(),
        e.getIsRead(),
        e.getCreatedAt());
  }
}
