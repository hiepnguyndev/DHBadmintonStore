package com.badminton.payment;

import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<ShopPayment, Long> {
  Optional<ShopPayment> findByOrderId(Long orderId);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from ShopPayment p where p.orderId=:orderId")
  Optional<ShopPayment> lockByOrderId(@Param("orderId") Long orderId);
  @Query("select p.orderId from ShopPayment p where p.reference=:reference")
  Optional<Long> findOrderIdByReference(@Param("reference") String reference);
  void deleteByOrderIdIn(Collection<Long> orderIds);
}
