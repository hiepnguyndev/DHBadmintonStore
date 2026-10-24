package com.badminton.escrow.repository;

import com.badminton.escrow.entity.EscrowTransaction;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EscrowTransactionRepository
    extends JpaRepository<EscrowTransaction, Long>, JpaSpecificationExecutor<EscrowTransaction> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from EscrowTransaction e where e.escrowId=:id")
  Optional<EscrowTransaction> lock(@Param("id") Long id);

  Optional<EscrowTransaction> findByMarketOrderMarketOrderId(Long id);

  boolean existsByMarketOrderMarketOrderId(Long id);
}
