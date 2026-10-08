package com.badminton.dispute.repository;

import com.badminton.dispute.entity.DisputeEvidence;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DisputeEvidenceRepository
    extends JpaRepository<DisputeEvidence, Long>, JpaSpecificationExecutor<DisputeEvidence> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from DisputeEvidence e where e.evidenceId=:id")
  Optional<DisputeEvidence> lock(@Param("id") Long id);
}
