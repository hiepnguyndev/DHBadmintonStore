package com.badminton.escrow.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.escrow.entity.EscrowStatus;
import com.badminton.marketorder.entity.MarketOrderStatus;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EscrowService {
  private final com.badminton.marketorder.service.MarketOrderService market;
  private final Access access;
  private final com.badminton.audit.service.AuditService audit;

  public EscrowService(
      com.badminton.marketorder.service.MarketOrderService market,
      Access access,
      com.badminton.audit.service.AuditService audit) {
    this.market = market;
    this.access = access;
    this.audit = audit;
  }

  public com.badminton.marketorder.dto.response.MarketOrderDetail pay(Long id) {
    var o = market.lock(id);
    access.owner(o.getBuyer().getUserId());
    var e = market.escrow(o);
    require(
        o.getOrderStatus() == MarketOrderStatus.PENDING_PAYMENT
            && e.getStatus() == EscrowStatus.WAITING_PAYMENT,
        "ESCROW_INVALID_STATUS",
        "Đơn không ở trạng thái chờ thanh toán");
    e.setStatus(EscrowStatus.HELD);
    e.setPaymentReference(code("SIM"));
    e.setPaidAt(LocalDateTime.now(ZoneOffset.UTC));
    e.setHeldAt(e.getPaidAt());
    o.setOrderStatus(MarketOrderStatus.PAID);
    audit.record(
        "SIMULATED_PAYMENT",
        "MarketplaceOrder",
        id,
        MarketOrderStatus.PENDING_PAYMENT,
        MarketOrderStatus.PAID);
    o.setOrderStatus(MarketOrderStatus.WAITING_SHIPMENT);
    market.notifyBoth(o, "Đã giữ tiền mô phỏng; chờ người bán gửi hàng");
    return market.view(o);
  }
}
