package com.badminton.shipment.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.escrow.entity.EscrowStatus;
import com.badminton.marketorder.entity.MarketOrderStatus;
import com.badminton.shipment.dto.request.*;
import com.badminton.shipment.dto.response.*;
import com.badminton.shipment.entity.*;
import com.badminton.shipment.mapper.*;
import com.badminton.shipment.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShipmentService {
  private final ShipmentRepository shipments;
  private final com.badminton.marketorder.service.MarketOrderService market;
  private final Access access;
  private final com.badminton.audit.service.AuditService audit;

  public ShipmentService(
      ShipmentRepository shipments,
      com.badminton.marketorder.service.MarketOrderService market,
      Access access,
      com.badminton.audit.service.AuditService audit) {
    this.shipments = shipments;
    this.market = market;
    this.access = access;
    this.audit = audit;
  }

  public ShipmentResponse create(ShipmentRequest r) {
    var o = market.lock(r.marketOrderId());
    access.owner(o.getSeller().getUserId());
    require(
        o.getOrderStatus() == MarketOrderStatus.WAITING_SHIPMENT
            && market.escrow(o).getStatus() == EscrowStatus.HELD,
        "SHIPMENT_INVALID_STATUS",
        "Đơn chưa sẵn sàng gửi hàng");
    require(
        !shipments.existsByMarketOrderMarketOrderId(o.getMarketOrderId()),
        "SHIPMENT_EXISTS",
        "Đã có vận đơn");
    var s = new Shipment();
    s.setMarketOrder(o);
    s.setCarrier(r.carrier());
    s.setTrackingCode(r.trackingCode());
    s.setShippingStatus(ShipmentStatus.WAITING_PICKUP);
    shipments.saveAndFlush(s);
    o.setOrderStatus(MarketOrderStatus.SHIPPING);
    audit.record("CREATE_SHIPMENT", "Shipment", s.getShipmentId(), null, s.getShippingStatus());
    market.notifyBoth(o, "Người bán đã tạo vận đơn");
    return ShipmentMapper.toResponse(s);
  }

  public ShipmentResponse transition(Long id, ShipmentStatus next) {
    var orderId = found(shipments.orderId(id), "SHIPMENT_NOT_FOUND");
    var o = market.lock(orderId);
    var s = found(shipments.lock(id), "SHIPMENT_NOT_FOUND");
    require(
        o.getOrderStatus() == MarketOrderStatus.SHIPPING
            || o.getOrderStatus() == MarketOrderStatus.DISPUTED,
        "SHIPMENT_INVALID_STATUS",
        "Đơn không còn đang vận chuyển");
    var before = s.getShippingStatus();
    boolean valid =
        switch (before) {
          case WAITING_PICKUP -> next == ShipmentStatus.PICKED_UP || next == ShipmentStatus.FAILED;
          case PICKED_UP -> next == ShipmentStatus.IN_TRANSIT || next == ShipmentStatus.FAILED;
          case IN_TRANSIT -> next == ShipmentStatus.DELIVERED || next == ShipmentStatus.FAILED;
          case FAILED -> next == ShipmentStatus.RETURNED;
          default -> false;
        };
    require(valid, "SHIPMENT_INVALID_STATUS", "Chuyển trạng thái vận đơn không hợp lệ");
    s.setShippingStatus(next);
    if (next == ShipmentStatus.PICKED_UP) s.setShippedAt(LocalDateTime.now(ZoneOffset.UTC));
    if (next == ShipmentStatus.DELIVERED) {
      s.setDeliveredAt(LocalDateTime.now(ZoneOffset.UTC));
      if (o.getOrderStatus() == MarketOrderStatus.SHIPPING)
        o.setOrderStatus(MarketOrderStatus.DELIVERED);
    }
    audit.record("SHIPMENT_STATUS", "Shipment", id, before, next);
    market.notifyBoth(o, "Vận đơn: " + next);
    return ShipmentMapper.toResponse(s);
  }
}
