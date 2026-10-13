package com.badminton.product.repository;

import com.badminton.product.entity.RacketSpecification;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RacketSpecificationRepository
    extends JpaRepository<RacketSpecification, Long>,
        JpaSpecificationExecutor<RacketSpecification> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from RacketSpecification e where e.specificationId=:id")
  Optional<RacketSpecification> lock(@Param("id") Long id);

  Optional<RacketSpecification> findByProductProductId(Long id);

  List<RacketSpecification> findByProductProductIdIn(Collection<Long> ids);
}
