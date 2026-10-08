package com.badminton.recommendation.repository;

import com.badminton.recommendation.entity.RecommendationWeight;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecommendationWeightRepository
    extends JpaRepository<RecommendationWeight, Long>,
        JpaSpecificationExecutor<RecommendationWeight> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from RecommendationWeight e where e.weightId=:id")
  Optional<RecommendationWeight> lock(@Param("id") Long id);
}
