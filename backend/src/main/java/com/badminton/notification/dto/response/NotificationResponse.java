package com.badminton.notification.dto.response;

import java.time.*;

public record NotificationResponse(
    Long notificationId,
    Long userId,
    String type,
    String title,
    String content,
    String referenceType,
    Long referenceId,
    Boolean isRead,
    LocalDateTime createdAt) {}
