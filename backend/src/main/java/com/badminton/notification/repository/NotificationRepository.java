package com.badminton.notification.repository;

import com.badminton.notification.entity.Notification;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository
    extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Notification e where e.notificationId=:id")
  Optional<Notification> lock(@Param("id") Long id);
}
