package com.badminton.dispute.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.dispute.dto.response.*;
import com.badminton.dispute.entity.*;
import com.badminton.dispute.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisputeEvidenceService {
  private final DisputeRepository disputes;
  private final DisputeEvidenceRepository evidences;
  private final com.badminton.marketorder.service.MarketOrderService market;
  private final com.badminton.common.storage.ImageStorage storage;
  private final Access access;
  private final com.badminton.audit.service.AuditService audit;

  public DisputeEvidenceService(
      DisputeRepository disputes,
      DisputeEvidenceRepository evidences,
      com.badminton.marketorder.service.MarketOrderService market,
      com.badminton.common.storage.ImageStorage storage,
      Access access,
      com.badminton.audit.service.AuditService audit) {
    this.disputes = disputes;
    this.evidences = evidences;
    this.market = market;
    this.storage = storage;
    this.access = access;
    this.audit = audit;
  }

  public DisputeEvidenceResponse upload(
      Long id, org.springframework.web.multipart.MultipartFile file, String description) {
    var orderId = found(disputes.orderId(id), "DISPUTE_NOT_FOUND");
    var o = market.lock(orderId);
    boolean buyer = access.current().id().equals(o.getBuyer().getUserId());
    if (!buyer) access.owner(o.getSeller().getUserId());
    var d = found(disputes.lock(id), "DISPUTE_NOT_FOUND");
    require(
        d.getStatus() != DisputeStatus.RESOLVED && d.getStatus() != DisputeStatus.REJECTED,
        "DISPUTE_INVALID_STATUS",
        "Tranh chấp đã kết thúc");
    bad(evidences.count(eq("dispute.disputeId", id)) < 10, "Tối đa 10 ảnh bằng chứng");
    bad(description == null || description.length() <= 2000, "Mô tả tối đa 2000 ký tự");
    var saved = storage.store(file, true);
    var e = new DisputeEvidence();
    e.setDispute(d);
    e.setUploadedBy(access.user());
    e.setFileUrl(saved.key());
    e.setFileType(saved.mime());
    e.setDescription(description);
    evidences.saveAndFlush(e);
    audit.record("UPLOAD_EVIDENCE", "Dispute", id, null, Map.of("evidenceId", e.getEvidenceId()));
    return preview(e);
  }

  public DisputeEvidenceResponse preview(DisputeEvidence e) {
    return new DisputeEvidenceResponse(
        e.getEvidenceId(),
        e.getDispute().getDisputeId(),
        e.getUploadedBy().getUserId(),
        storage.privatePreview(e.getFileUrl(), e.getFileType()),
        e.getFileType(),
        e.getDescription(),
        e.getCreatedAt());
  }
}
