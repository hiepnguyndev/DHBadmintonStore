package com.badminton.dispute.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.dispute.dto.request.*;
import com.badminton.dispute.dto.response.*;
import com.badminton.dispute.entity.*;
import com.badminton.dispute.mapper.*;
import com.badminton.dispute.repository.*;
import com.badminton.escrow.entity.EscrowStatus;
import com.badminton.marketorder.entity.MarketOrderStatus;
import com.badminton.marketplace.entity.ListingStatus;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisputeService {
  private final DisputeRepository disputes;
  private final DisputeEvidenceService evidenceService;
  private final DisputeEvidenceRepository evidences;
  private final com.badminton.marketorder.service.MarketOrderService market;
  private final Access access;
  private final com.badminton.audit.service.AuditService audit;

  public DisputeService(
      DisputeEvidenceService evidenceService,
      DisputeRepository disputes,
      DisputeEvidenceRepository evidences,
      com.badminton.marketorder.service.MarketOrderService market,
      Access access,
      com.badminton.audit.service.AuditService audit) {
    this.evidenceService = evidenceService;
    this.disputes = disputes;
    this.evidences = evidences;
    this.market = market;
    this.access = access;
    this.audit = audit;
  }

  public DisputeResponse create(DisputeRequest r) {
    var o = market.lock(r.marketOrderId());
    boolean buyer = access.current().id().equals(o.getBuyer().getUserId());
    if (!buyer) access.owner(o.getSeller().getUserId());
    var e = market.escrow(o);
    require(
        (o.getOrderStatus() == MarketOrderStatus.SHIPPING
                || o.getOrderStatus() == MarketOrderStatus.DELIVERED)
            && e.getStatus() == EscrowStatus.HELD,
        "DISPUTE_INVALID_STATUS",
        "Chỉ khi đang giao hoặc đã giao và tiền còn giữ");
    if (o.getOrderStatus() == MarketOrderStatus.SHIPPING)
      bad(
          buyer && r.reason() == DisputeReason.NOT_RECEIVED,
          "Khi đang vận chuyển, người mua có thể báo chưa nhận hàng");
    require(
        !disputes.existsByMarketOrderMarketOrderId(o.getMarketOrderId()),
        "DISPUTE_EXISTS",
        "Mỗi đơn chỉ có một tranh chấp trong MVP");
    var d = new Dispute();
    d.setMarketOrder(o);
    d.setCreatedBy(access.user());
    d.setReason(r.reason());
    d.setDescription(r.description());
    d.setStatus(DisputeStatus.OPEN);
    disputes.saveAndFlush(d);
    o.setOrderStatus(MarketOrderStatus.DISPUTED);
    e.setStatus(EscrowStatus.DISPUTED);
    audit.record("OPEN_DISPUTE", "Dispute", d.getDisputeId(), null, d.getStatus());
    market.notifyBoth(o, "Đã mở tranh chấp; tiền đang được giữ để xử lý");
    return DisputeMapper.toResponse(d);
  }

  public record Detail(
      DisputeResponse dispute, java.util.List<DisputeEvidenceResponse> evidences) {}

  @Transactional(readOnly = true)
  public Detail detail(Long id) {
    var d = found(disputes.findById(id), "DISPUTE_NOT_FOUND");
    market.participant(d.getMarketOrder());
    return new Detail(
        DisputeMapper.toResponse(d),
        evidences.findAll(eq("dispute.disputeId", id)).stream()
            .map(evidenceService::preview)
            .toList());
  }

  @Transactional(readOnly = true)
  public PageResponse<DisputeResponse> list(
      DisputeStatus status, DisputeReason reason, int page, int size) {
    return PageResponse.from(
        disputes
            .findAll(
                Queries.<Dispute>all().and(eq("status", status)).and(eq("reason", reason)),
                page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(DisputeMapper::toResponse));
  }

  public DisputeResponse resolve(Long id, ResolutionRequest r) {
    var orderId = found(disputes.orderId(id), "DISPUTE_NOT_FOUND");
    var o = market.lock(orderId);
    var d = found(disputes.lock(id), "DISPUTE_NOT_FOUND");
    var e = market.escrow(o);
    bad(
        r.resolution() != Resolution.PARTIAL_REFUND
            && r.resolution() != Resolution.RETURN_AND_REFUND,
        "Phương án này thuộc Post-MVP");
    require(
        o.getOrderStatus() == MarketOrderStatus.DISPUTED
            && e.getStatus() == EscrowStatus.DISPUTED
            && d.getStatus() != DisputeStatus.RESOLVED
            && d.getStatus() != DisputeStatus.REJECTED,
        "DISPUTE_INVALID_STATUS",
        "Tranh chấp không còn chờ giải quyết");
    var before = d.getStatus();
    d.setResolution(r.resolution());
    d.setResolutionNote(r.resolutionNote());
    d.setResolvedBy(access.user());
    d.setResolvedAt(LocalDateTime.now(ZoneOffset.UTC));
    d.setStatus(
        r.resolution() == Resolution.REJECT_DISPUTE
            ? DisputeStatus.REJECTED
            : DisputeStatus.RESOLVED);
    if (r.resolution() == Resolution.FULL_REFUND) {
      e.setStatus(EscrowStatus.REFUNDED);
      e.setRefundedAt(d.getResolvedAt());
      o.setOrderStatus(MarketOrderStatus.REFUNDED);
      o.getListing().setStatus(ListingStatus.HIDDEN);
    } else market.complete(o, e);
    audit.record(
        "RESOLVE_DISPUTE",
        "Dispute",
        id,
        before,
        Map.of("status", d.getStatus(), "resolution", r.resolution(), "note", r.resolutionNote()));
    market.notifyBoth(o, "Tranh chấp đã được giải quyết: " + r.resolution());
    return DisputeMapper.toResponse(d);
  }
}
