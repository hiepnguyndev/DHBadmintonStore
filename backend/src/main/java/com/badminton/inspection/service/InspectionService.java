package com.badminton.inspection.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.audit.service.AuditService;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.inspection.dto.request.*;
import com.badminton.inspection.dto.response.*;
import com.badminton.inspection.entity.*;
import com.badminton.inspection.mapper.*;
import com.badminton.inspection.repository.*;
import com.badminton.marketplace.repository.UsedListingRepository;
import com.badminton.notification.service.NotificationService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class InspectionService {
  private final InspectionRequestRepository requests;
  private final InspectionReportRepository reports;
  private final UsedListingRepository listings;
  private final InspectionReportResolver resolver;
  private final Access access;
  private final AuditService audit;
  private final NotificationService notifications;

  public InspectionService(
      InspectionRequestRepository requests,
      InspectionReportRepository reports,
      UsedListingRepository listings,
      InspectionReportResolver resolver,
      Access access,
      AuditService audit,
      NotificationService notifications) {
    this.requests = requests;
    this.reports = reports;
    this.listings = listings;
    this.resolver = resolver;
    this.access = access;
    this.audit = audit;
    this.notifications = notifications;
  }

  public InspectionRequestResponse create(InspectionCreateRequest r) {
    var l = found(listings.lock(r.listingId()), "LISTING_NOT_FOUND");
    access.owner(l.getSeller().getUserId());
    require(
        !requests.existsByListingListingIdAndStatusIn(
            l.getListingId(),
            Set.of(
                InspectionStatus.REQUESTED,
                InspectionStatus.ACCEPTED,
                InspectionStatus.INSPECTING)),
        "INSPECTION_ALREADY_EXISTS",
        "Đã có yêu cầu kiểm định đang hoạt động");
    var i = new InspectionRequest();
    i.setListing(l);
    i.setSeller(l.getSeller());
    i.setInspectionCode(code("INS"));
    i.setStatus(InspectionStatus.REQUESTED);
    i.setRequestedAt(LocalDateTime.now(ZoneOffset.UTC));
    i.setNote(r.note());
    return InspectionRequestMapper.toResponse(requests.saveAndFlush(i));
  }

  @Transactional(readOnly = true)
  public PageResponse<InspectionRequestResponse> list(
      boolean my, int page, int size, InspectionStatus status) {
    org.springframework.data.jpa.domain.Specification<InspectionRequest> f =
        my ? eq("seller.userId", access.current().id()) : all();
    if (status != null) f = f.and(eq("status", status));
    return PageResponse.from(
        requests
            .findAll(f, page(page, size, null, "requestedAt", Set.of("requestedAt")))
            .map(InspectionRequestMapper::toResponse));
  }

  private InspectionRequest locked(Long id) {
    var listingId = found(requests.listingId(id), "INSPECTION_NOT_FOUND");
    listings.lock(listingId);
    return found(requests.lock(id), "INSPECTION_NOT_FOUND");
  }

  public InspectionRequestResponse accept(Long id) {
    var i = locked(id);
    require(
        i.getStatus() == InspectionStatus.REQUESTED,
        "INSPECTION_INVALID_STATUS",
        "Yêu cầu đã được nhận hoặc kết thúc");
    i.setInspector(access.user());
    i.setAcceptedAt(LocalDateTime.now(ZoneOffset.UTC));
    i.setStatus(InspectionStatus.ACCEPTED);
    audit.record(
        "ACCEPT_INSPECTION",
        "InspectionRequest",
        id,
        null,
        Map.of("inspectorId", access.current().id()));
    return InspectionRequestMapper.toResponse(i);
  }

  public InspectionRequestResponse start(Long id) {
    var i = locked(id);
    access.owner(i.getInspector() == null ? null : i.getInspector().getUserId());
    require(
        i.getStatus() == InspectionStatus.ACCEPTED,
        "INSPECTION_INVALID_STATUS",
        "Yêu cầu phải ở ACCEPTED");
    i.setStatus(InspectionStatus.INSPECTING);
    return InspectionRequestMapper.toResponse(i);
  }

  public InspectionReportResponse report(Long id, InspectionReportRequest r) {
    var i = locked(id);
    access.owner(i.getInspector() == null ? null : i.getInspector().getUserId());
    require(
        i.getStatus() == InspectionStatus.INSPECTING,
        "INSPECTION_INVALID_STATUS",
        "Chỉ lập báo cáo khi đang kiểm định");
    bad(r.recommendedMinPrice().compareTo(r.recommendedMaxPrice()) <= 0, "Khoảng giá không hợp lệ");
    InspectionReport p = new InspectionReport();
    p.setInspection(i);
    p.setModelVerified(r.modelVerified());
    p.setSerialVerified(r.serialVerified());
    p.setFrameCondition(r.frameCondition());
    p.setPaintCondition(r.paintCondition());
    p.setGrommetCondition(r.grommetCondition());
    p.setHandleCondition(r.handleCondition());
    p.setCrackDetected(r.crackDetected());
    p.setRepairDetected(r.repairDetected());
    p.setOverallScore(r.overallScore().setScale(2, RoundingMode.HALF_UP));
    p.setRecommendedMinPrice(r.recommendedMinPrice());
    p.setRecommendedMaxPrice(r.recommendedMaxPrice());
    p.setNotes(r.notes());
    reports.saveAndFlush(p);
    i.setStatus(InspectionStatus.COMPLETED);
    i.setCompletedAt(LocalDateTime.now(ZoneOffset.UTC));
    requests.flush();
    if (resolver.current(i.getListing().getListingId()).isPresent()) {
      i.getListing().setConditionScore(p.getOverallScore());
      i.getListing()
          .setConditionGrade(
              com.badminton.valuation.service.ConditionScoreCalculator.grade(p.getOverallScore()));
    }
    audit.record(
        "COMPLETE_INSPECTION", "InspectionRequest", id, null, InspectionReportMapper.toResponse(p));
    notifications.send(
        i.getSeller(),
        "INSPECTION_COMPLETED",
        "Đã kiểm định sản phẩm",
        "Xem báo cáo kiểm định và tình trạng hiệu lực.",
        "UsedListing",
        i.getListing().getListingId());
    return InspectionReportMapper.toResponse(p);
  }
}
