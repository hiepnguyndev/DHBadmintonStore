package com.badminton.order.repository;

import com.badminton.order.entity.OrderItem;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository
    extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from OrderItem e where e.orderItemId=:id")
  Optional<OrderItem> lock(@Param("id") Long id);

  List<OrderItem> findByOrderOrderIdOrderByProductProductIdAsc(Long id);

  void deleteByOrderOrderIdIn(Collection<Long> orderIds);
}
