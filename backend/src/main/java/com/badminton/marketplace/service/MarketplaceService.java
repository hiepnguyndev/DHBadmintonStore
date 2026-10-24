package com.badminton.marketplace.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.audit.service.AuditService;
import com.badminton.brand.repository.BrandRepository;
import com.badminton.category.repository.CategoryRepository;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.inspection.entity.*;
import com.badminton.inspection.mapper.InspectionReportMapper;
import com.badminton.inspection.service.InspectionReportResolver;
import com.badminton.marketplace.dto.request.*;
import com.badminton.marketplace.dto.response.*;
import com.badminton.marketplace.entity.*;
import com.badminton.marketplace.mapper.*;
import com.badminton.marketplace.repository.*;
import com.badminton.notification.service.NotificationService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketplaceService {
  private final UsedListingRepository listings;
  private final UsedRacketSpecificationRepository specs;
  private final UsedListingImageRepository images;
  private final CategoryRepository categories;
  private final BrandRepository brands;
  private final InspectionReportResolver resolver;
  private final AuditService audit;
  private final NotificationService notifications;
  private final Access access;
  private final com.badminton.inspection.repository.InspectionRequestRepository inspections;

  public MarketplaceService(
      com.badminton.inspection.repository.InspectionRequestRepository inspections,
      UsedListingRepository listings,
      UsedRacketSpecificationRepository specs,
      UsedListingImageRepository images,
      CategoryRepository categories,
      BrandRepository brands,
      InspectionReportResolver resolver,
      AuditService audit,
      NotificationService notifications,
      Access access) {
    this.inspections = inspections;
    this.listings = listings;
    this.specs = specs;
    this.images = images;
    this.categories = categories;
    this.brands = brands;
    this.resolver = resolver;
    this.audit = audit;
    this.notifications = notifications;
    this.access = access;
  }

  public ListingDetail view(UsedListing l) {
    var report = resolver.current(l.getListingId());
    return new ListingDetail(
        UsedListingMapper.toResponse(l),
        images.findByListingListingIdOrderBySortOrderAsc(l.getListingId()).stream()
            .map(UsedListingImageMapper::toResponse)
            .toList(),
        specs
            .findByListingListingId(l.getListingId())
            .map(UsedRacketSpecificationMapper::toResponse)
            .orElse(null),
        report.isPresent(),
        report.isPresent() ? "INSPECTION" : "SELLER",
        report.map(InspectionReportMapper::toResponse).orElse(null),
        new ListingDetail.SellerSummary(l.getSeller().getUserId(), l.getSeller().getFullName()));
  }

  @Transactional(readOnly = true)
  public ListingDetail detail(Long id) {
    var l = found(listings.findById(id), "LISTING_NOT_FOUND");
    if (!Set.of(ListingStatus.AVAILABLE, ListingStatus.RESERVED, ListingStatus.SOLD)
        .contains(l.getStatus())) {
      boolean assigned =
          access.optional() != null
              && access.current().role() == com.badminton.user.entity.Role.INSPECTOR
              && inspections.existsByListingListingIdAndInspectorUserId(id, access.current().id());
      if (!assigned) access.ownerOrAdmin(l.getSeller().getUserId());
    }
    return view(l);
  }

  @Transactional(readOnly = true)
  public PageResponse<ListingDetail> list(
      String keyword,
      Long categoryId,
      Long brandId,
      ConditionGrade grade,
      Boolean verified,
      BigDecimal min,
      BigDecimal max,
      int page,
      int size,
      String sort,
      boolean mine,
      boolean admin,
      ListingStatus status) {
    org.springframework.data.jpa.domain.Specification<UsedListing> f =
        admin
            ? all()
            : mine
                ? eq("seller.userId", access.current().id())
                : eq("status", ListingStatus.AVAILABLE);
    if (status != null && (mine || admin)) f = f.and(eq("status", status));
    if (keyword != null && !keyword.isBlank()) f = f.and(contains("productName", keyword));
    if (categoryId != null) f = f.and(eq("category.categoryId", categoryId));
    if (brandId != null) f = f.and(eq("brand.brandId", brandId));
    if (grade != null) f = f.and(eq("conditionGrade", grade));
    if (min != null) f = f.and((r, q, b) -> b.ge(r.get("sellingPrice"), min));
    if (max != null) f = f.and((r, q, b) -> b.le(r.get("sellingPrice"), max));
    bad(min == null || max == null || min.compareTo(max) <= 0, "Khoảng giá không hợp lệ");
    if (verified != null) {
      f =
          f.and(
              (r, q, b) -> {
                var sq = q.subquery(Long.class);
                var report = sq.from(InspectionReport.class);
                var invalid = q.subquery(LocalDateTime.class);
                var a = invalid.from(com.badminton.audit.entity.AuditLog.class);
                invalid
                    .select(b.greatest(a.<LocalDateTime>get("createdAt")))
                    .where(
                        b.equal(a.get("entityType"), "UsedListing"),
                        b.equal(a.get("entityId"), r.get("listingId")),
                        b.equal(a.get("action"), "INVALIDATE_INSPECTION"));
                sq.select(report.get("reportId"))
                    .where(
                        b.equal(report.get("inspection").get("listing"), r),
                        b.equal(report.get("inspection").get("status"), InspectionStatus.COMPLETED),
                        b.or(
                            b.isNull(invalid),
                            b.greaterThan(report.get("inspection").get("requestedAt"), invalid)));
                return verified ? b.exists(sq) : b.not(b.exists(sq));
              });
    }
    return PageResponse.from(
        listings
            .findAll(
                f,
                page(
                    page,
                    size,
                    sort,
                    "createdAt",
                    Set.of("createdAt", "sellingPrice", "listingId")))
            .map(this::view));
  }

  public ListingDetail save(Long id, ListingRequest r) {
    var user = access.user();
    UsedListing l = id == null ? new UsedListing() : found(listings.lock(id), "LISTING_NOT_FOUND");
    boolean edited = id != null;
    if (edited) {
      access.owner(l.getSeller().getUserId());
      require(
          Set.of(ListingStatus.DRAFT, ListingStatus.REJECTED, ListingStatus.AVAILABLE)
              .contains(l.getStatus()),
          "LISTING_INVALID_STATUS",
          "Chỉ sửa bản nháp, bị từ chối hoặc đang khả dụng");
    }
    var before = edited ? UsedListingMapper.toResponse(l) : null;
    // Compare only inspection-relevant fields: price/description alone do not invalidate the
    // report.
    var oldSpec =
        edited ? specs.findByListingListingId(id) : Optional.<UsedRacketSpecification>empty();
    boolean relevant =
        edited
            && (!Objects.equals(l.getBrand().getBrandId(), r.brandId())
                || !Objects.equals(l.getCategory().getCategoryId(), r.categoryId())
                || !Objects.equals(l.getModel(), r.model())
                || !Objects.equals(l.getProductName(), r.productName())
                || !Objects.equals(l.getConditionGrade(), r.conditionGrade())
                || !Objects.equals(
                    oldSpec
                        .map(UsedRacketSpecificationMapper::toResponse)
                        .map(
                            s ->
                                Arrays.asList(
                                    s.weightClass(),
                                    s.gripSize(),
                                    s.stringModel(),
                                    s.stringTensionLbs(),
                                    s.frameCondition(),
                                    s.paintCondition(),
                                    s.grommetCondition(),
                                    s.handleCondition(),
                                    s.repairHistory(),
                                    s.serialNumber()))
                        .orElse(null),
                    r.racketSpecification() == null
                        ? null
                        : Arrays.asList(
                            r.racketSpecification().weightClass(),
                            r.racketSpecification().gripSize(),
                            r.racketSpecification().stringModel(),
                            r.racketSpecification().stringTensionLbs(),
                            r.racketSpecification().frameCondition(),
                            r.racketSpecification().paintCondition(),
                            r.racketSpecification().grommetCondition(),
                            r.racketSpecification().handleCondition(),
                            r.racketSpecification().repairHistory(),
                            r.racketSpecification().serialNumber())));
    l.setCategory(found(categories.findById(r.categoryId()), "CATEGORY_NOT_FOUND"));
    l.setBrand(found(brands.findById(r.brandId()), "BRAND_NOT_FOUND"));
    l.setProductName(r.productName());
    l.setModel(r.model());
    l.setDescription(r.description());
    l.setOriginalPrice(r.originalPrice());
    l.setSellingPrice(r.sellingPrice());
    l.setPurchaseDate(r.purchaseDate());
    l.setUsageMonths(r.usageMonths());
    l.setConditionGrade(r.conditionGrade());
    l.setWarrantyStatus(r.warrantyStatus());
    l.setWarrantyUntil(r.warrantyUntil());
    if (!edited) {
      l.setSeller(user);
      l.setListingCode(code("LST"));
      l.setViewCount(0);
      l.setStatus(ListingStatus.DRAFT);
    } else if (l.getStatus() == ListingStatus.AVAILABLE) l.setStatus(ListingStatus.PENDING_REVIEW);
    if (relevant) l.setConditionScore(null);
    listings.saveAndFlush(l);
    if (r.racketSpecification() != null) {
      var x = r.racketSpecification();
      var s = oldSpec.orElseGet(UsedRacketSpecification::new);
      s.setListing(l);
      s.setWeightClass(x.weightClass());
      s.setGripSize(x.gripSize());
      s.setStringModel(x.stringModel());
      s.setStringTensionLbs(x.stringTensionLbs());
      s.setFrameCondition(x.frameCondition());
      s.setPaintCondition(x.paintCondition());
      s.setGrommetCondition(x.grommetCondition());
      s.setHandleCondition(x.handleCondition());
      s.setRepairHistory(x.repairHistory());
      s.setSerialNumber(x.serialNumber());
      specs.saveAndFlush(s);
    } else if (edited && oldSpec.isPresent()) {
      specs.delete(oldSpec.get());
      specs.flush();
    }
    if (relevant)
      audit.record(
          "INVALIDATE_INSPECTION",
          "UsedListing",
          l.getListingId(),
          before,
          UsedListingMapper.toResponse(l));
    else
      audit.record(
          edited ? "UPDATE_LISTING" : "CREATE_LISTING",
          "UsedListing",
          l.getListingId(),
          before,
          UsedListingMapper.toResponse(l));
    return view(l);
  }

  public ListingDetail submit(Long id) {
    var l = found(listings.lock(id), "LISTING_NOT_FOUND");
    access.owner(l.getSeller().getUserId());
    require(
        l.getStatus() == ListingStatus.DRAFT || l.getStatus() == ListingStatus.REJECTED,
        "LISTING_INVALID_STATUS",
        "Chỉ gửi duyệt bản nháp hoặc tin bị từ chối");
    require(
        !images.findByListingListingIdOrderBySortOrderAsc(id).isEmpty(),
        "LISTING_IMAGE_REQUIRED",
        "Cần ít nhất một ảnh sản phẩm");
    l.setStatus(ListingStatus.PENDING_REVIEW);
    audit.record("SUBMIT_LISTING", "UsedListing", id, null, Map.of("status", l.getStatus()));
    return view(l);
  }

  public ListingDetail review(Long id, boolean approve, String reason) {
    var l = found(listings.lock(id), "LISTING_NOT_FOUND");
    require(
        l.getStatus() == ListingStatus.PENDING_REVIEW,
        "LISTING_INVALID_STATUS",
        "Tin không chờ duyệt");
    l.setStatus(approve ? ListingStatus.AVAILABLE : ListingStatus.REJECTED);
    audit.record(
        approve ? "APPROVE_LISTING" : "REJECT_LISTING",
        "UsedListing",
        id,
        Map.of("status", "PENDING_REVIEW"),
        Map.of("status", l.getStatus(), "reason", reason == null ? "" : reason));
    notifications.send(
        l.getSeller(),
        "LISTING_REVIEW",
        "Kết quả duyệt tin",
        approve ? "Tin đã được duyệt" : reason,
        "UsedListing",
        id);
    return view(l);
  }
}
