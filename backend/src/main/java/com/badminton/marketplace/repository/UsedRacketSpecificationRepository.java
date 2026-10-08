package com.badminton.marketplace.repository;

import com.badminton.marketplace.entity.UsedRacketSpecification;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsedRacketSpecificationRepository
    extends JpaRepository<UsedRacketSpecification, Long>,
        JpaSpecificationExecutor<UsedRacketSpecification> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from UsedRacketSpecification e where e.usedSpecId=:id")
  Optional<UsedRacketSpecification> lock(@Param("id") Long id);

  Optional<UsedRacketSpecification> findByListingListingId(Long id);
}
