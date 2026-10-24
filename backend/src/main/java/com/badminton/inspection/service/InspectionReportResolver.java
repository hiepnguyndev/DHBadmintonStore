package com.badminton.inspection.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.audit.repository.AuditLogRepository;
import com.badminton.common.support.*;
import com.badminton.inspection.entity.*;
import com.badminton.inspection.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InspectionReportResolver {
  private final InspectionReportRepository reports;
  private final AuditLogRepository audits;

  public InspectionReportResolver(InspectionReportRepository reports, AuditLogRepository audits) {
    this.reports = reports;
    this.audits = audits;
  }

  @Transactional(readOnly = true)
  public Optional<InspectionReport> current(Long listingId) {
    var last =
        audits
            .findFirstByEntityTypeAndEntityIdAndActionOrderByCreatedAtDesc(
                "UsedListing", listingId, "INVALIDATE_INSPECTION")
            .map(a -> a.getCreatedAt())
            .orElse(LocalDateTime.MIN);
    return reports.findByInspectionListingListingIdOrderByCreatedAtDesc(listingId).stream()
        .filter(
            r ->
                r.getInspection().getStatus() == InspectionStatus.COMPLETED
                    && r.getInspection().getRequestedAt().isAfter(last))
        .findFirst();
  }
}
