package com.badminton.inspection.repository;

import com.badminton.inspection.entity.InspectionRequest;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface InspectionRequestRepository
    extends JpaRepository<InspectionRequest, Long>, JpaSpecificationExecutor<InspectionRequest> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from InspectionRequest e where e.inspectionId=:id")
  Optional<InspectionRequest> lock(@Param("id") Long id);

  boolean existsByListingListingIdAndStatusIn(
      Long listingId, Collection<com.badminton.inspection.entity.InspectionStatus> statuses);

  @Query("select e.listing.listingId from InspectionRequest e where e.inspectionId=:id")
  Optional<Long> listingId(@Param("id") Long id);

  boolean existsByListingListingIdAndInspectorUserId(Long listingId, Long inspectorId);
}
