package com.badminton.order.repository;

import com.badminton.order.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Order e where e.orderId=:id")
  Optional<Order> lock(@Param("id") Long id);
}
