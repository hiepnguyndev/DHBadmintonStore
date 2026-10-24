package com.badminton.recommendation.repository;

import com.badminton.recommendation.entity.PlayerProfile;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PlayerProfileRepository
    extends JpaRepository<PlayerProfile, Long>, JpaSpecificationExecutor<PlayerProfile> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from PlayerProfile e where e.profileId=:id")
  Optional<PlayerProfile> lock(@Param("id") Long id);

  Optional<PlayerProfile> findByUserUserId(Long id);
}
