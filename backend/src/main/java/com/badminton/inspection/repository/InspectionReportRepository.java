package com.badminton.inspection.repository;

import com.badminton.inspection.entity.InspectionReport;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface InspectionReportRepository
    extends JpaRepository<InspectionReport, Long>, JpaSpecificationExecutor<InspectionReport> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from InspectionReport e where e.reportId=:id")
  Optional<InspectionReport> lock(@Param("id") Long id);

  List<InspectionReport> findByInspectionListingListingIdOrderByCreatedAtDesc(Long listingId);
}
