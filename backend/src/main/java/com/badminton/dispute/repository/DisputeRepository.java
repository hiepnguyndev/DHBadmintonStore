package com.badminton.dispute.repository;

import com.badminton.dispute.entity.Dispute;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DisputeRepository
    extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Dispute e where e.disputeId=:id")
  Optional<Dispute> lock(@Param("id") Long id);

  Optional<Dispute> findByMarketOrderMarketOrderId(Long id);

  boolean existsByMarketOrderMarketOrderId(Long id);

  @Query("select e.marketOrder.marketOrderId from Dispute e where e.disputeId=:id")
  Optional<Long> orderId(@Param("id") Long id);
}
