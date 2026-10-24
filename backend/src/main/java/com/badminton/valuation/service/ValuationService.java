package com.badminton.valuation.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.inspection.service.InspectionReportResolver;
import com.badminton.marketplace.entity.*;
import com.badminton.marketplace.repository.*;
import com.badminton.product.repository.ProductRepository;
import com.badminton.valuation.dto.response.*;
import com.badminton.valuation.entity.*;
import com.badminton.valuation.mapper.*;
import com.badminton.valuation.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ValuationService {
  private final ValuationRepository valuations;
  private final UsedListingRepository listings;
  private final UsedRacketSpecificationRepository specs;
  private final ProductRepository products;
  private final InspectionReportResolver resolver;
  private final ValuationCalculator calculator;
  private final Access access;

  public ValuationService(
      ValuationRepository valuations,
      UsedListingRepository listings,
      UsedRacketSpecificationRepository specs,
      ProductRepository products,
      InspectionReportResolver resolver,
      ValuationCalculator calculator,
      Access access) {
    this.valuations = valuations;
    this.listings = listings;
    this.specs = specs;
    this.products = products;
    this.resolver = resolver;
    this.calculator = calculator;
    this.access = access;
  }

  public ValuationResponse create(com.badminton.valuation.dto.request.ValuationRequest r) {
    var l = found(listings.lock(r.listingId()), "LISTING_NOT_FOUND");
    access.owner(l.getSeller().getUserId());
    bad(
        r.accessoryCondition() != null && r.repairLevel() != null,
        "Cần phân loại phụ kiện và sửa chữa");
    var report = resolver.current(l.getListingId());
    var grade = l.getConditionGrade();
    BigDecimal score = null;
    String conditionSource = "SELLER_GRADE";
    if (report.isPresent()) {
      var inspection = report.get();
      require(
          Boolean.TRUE.equals(inspection.getRepairDetected())
              == (r.repairLevel() != ValuationCalculator.Repair.NONE),
          "REPAIR_CONFLICT",
          "Mức sửa chữa không khớp báo cáo kiểm định hợp lệ");
      score = inspection.getOverallScore().setScale(2, RoundingMode.HALF_UP);
      grade = ConditionScoreCalculator.grade(score);
      conditionSource = "INSPECTION_REPORT:" + inspection.getReportId();
    }
    // Compare names in Java: MySQL accent-insensitive collation is broader than D15.
    var matches =
        products
            .findAll(
                (root, query, cb) ->
                    cb.and(
                        cb.equal(
                            root.get("status"), com.badminton.product.entity.ProductStatus.ACTIVE),
                        cb.equal(root.get("brand").get("brandId"), l.getBrand().getBrandId())))
            .stream()
            .filter(p -> p.getProductName().trim().equalsIgnoreCase(l.getProductName().trim()))
            .toList();
    var reference =
        matches.size() == 1
            ? com.badminton.product.mapper.ProductMapper.effectivePrice(matches.get(0))
            : l.getOriginalPrice();
    var v = new Valuation();
    v.setListing(l);
    v.setReferencePrice(reference);
    v.setAgeFactor(calculator.ageFactor(l.getUsageMonths()));
    v.setConditionFactor(calculator.conditionFactor(grade));
    v.setWarrantyFactor(
        calculator.warrantyFactor(
            l.getWarrantyUntil(),
            "EXPIRED".equalsIgnoreCase(l.getWarrantyStatus()),
            LocalDate.now(ZoneOffset.UTC)));
    v.setAccessoryFactor(calculator.accessoryFactor(r.accessoryCondition()));
    v.setRepairFactor(calculator.repairFactor(r.repairLevel()));
    v.setDemandFactor(calculator.demandFactor());
    var range =
        calculator.range(
            reference,
            v.getAgeFactor(),
            v.getConditionFactor(),
            v.getWarrantyFactor(),
            v.getAccessoryFactor(),
            v.getRepairFactor(),
            v.getDemandFactor());
    v.setEstimatedPrice(range.estimatedPrice());
    v.setSuggestedMinPrice(range.suggestedMinPrice());
    v.setSuggestedMaxPrice(range.suggestedMaxPrice());
    v.setValuationMethod(ValuationMethod.RULE_BASED);
    v.setConditionScore(score);
    v.setAlgorithmVersion("D04-D15-v1");
    v.setNote(
        "accessoryCondition="
            + r.accessoryCondition()
            + ";repairLevel="
            + r.repairLevel()
            + ";conditionSource="
            + conditionSource
            + ";referenceSource="
            + (matches.size() == 1
                ? "PRODUCT:" + matches.get(0).getProductId()
                : "LISTING_ORIGINAL_PRICE"));
    return ValuationMapper.toResponse(valuations.saveAndFlush(v));
  }

  @Transactional(readOnly = true)
  public List<ValuationResponse> history(Long id) {
    var l = found(listings.findById(id), "LISTING_NOT_FOUND");
    if (l.getStatus() != ListingStatus.AVAILABLE
        && l.getStatus() != ListingStatus.SOLD
        && l.getStatus() != ListingStatus.RESERVED) access.ownerOrAdmin(l.getSeller().getUserId());
    return valuations
        .findAll(
            eq("listing.listingId", id),
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        .stream()
        .map(ValuationMapper::toResponse)
        .toList();
  }
}
