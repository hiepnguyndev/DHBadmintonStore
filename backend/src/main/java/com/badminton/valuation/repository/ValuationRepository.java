package com.badminton.valuation.repository;

import com.badminton.valuation.entity.Valuation;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ValuationRepository
    extends JpaRepository<Valuation, Long>, JpaSpecificationExecutor<Valuation> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Valuation e where e.valuationId=:id")
  Optional<Valuation> lock(@Param("id") Long id);
}
