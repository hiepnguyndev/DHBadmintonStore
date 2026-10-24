package com.badminton.recommendation.repository;

import com.badminton.recommendation.entity.RecommendationLog;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecommendationLogRepository
    extends JpaRepository<RecommendationLog, Long>, JpaSpecificationExecutor<RecommendationLog> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from RecommendationLog e where e.logId=:id")
  Optional<RecommendationLog> lock(@Param("id") Long id);
}
