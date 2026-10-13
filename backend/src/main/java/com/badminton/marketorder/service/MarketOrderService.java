package com.badminton.marketorder.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.audit.service.AuditService;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.dispute.dto.response.*;
import com.badminton.dispute.entity.*;
import com.badminton.dispute.mapper.*;
import com.badminton.dispute.repository.*;
import com.badminton.escrow.dto.response.*;
import com.badminton.escrow.entity.*;
import com.badminton.escrow.mapper.*;
import com.badminton.escrow.repository.*;
import com.badminton.marketorder.dto.request.*;
import com.badminton.marketorder.dto.response.*;
import com.badminton.marketorder.entity.*;
import com.badminton.marketorder.mapper.*;
import com.badminton.marketorder.repository.*;
import com.badminton.marketplace.dto.response.*;
import com.badminton.marketplace.entity.*;
import com.badminton.marketplace.mapper.*;
import com.badminton.marketplace.repository.*;
import com.badminton.notification.service.NotificationService;
import com.badminton.rating.dto.response.*;
import com.badminton.rating.entity.*;
import com.badminton.rating.mapper.*;
import com.badminton.rating.repository.*;
import com.badminton.shipment.dto.response.*;
import com.badminton.shipment.entity.*;
import com.badminton.shipment.mapper.*;
import com.badminton.shipment.repository.*;
import com.badminton.user.repository.AddressRepository;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarketOrderService {
  private final MarketplaceOrderRepository orders;
  private final UsedListingRepository listings;
  private final EscrowTransactionRepository escrows;
  private final ShipmentRepository shipments;
  private final DisputeRepository disputes;
  private final SellerRatingRepository ratings;
  private final AddressRepository addresses;
  private final Access access;
  private final AuditService audit;
  private final NotificationService notifications;

  public MarketOrderService(
      MarketplaceOrderRepository orders,
      UsedListingRepository listings,
      EscrowTransactionRepository escrows,
      ShipmentRepository shipments,
      DisputeRepository disputes,
      SellerRatingRepository ratings,
      AddressRepository addresses,
      Access access,
      AuditService audit,
      NotificationService notifications) {
    this.orders = orders;
    this.listings = listings;
    this.escrows = escrows;
    this.shipments = shipments;
    this.disputes = disputes;
    this.ratings = ratings;
    this.addresses = addresses;
    this.access = access;
    this.audit = audit;
    this.notifications = notifications;
  }

  @org.springframework.beans.factory.annotation.Value("${app.marketplace.shipping-fee:0}")
  private BigDecimal shippingFee;

  @org.springframework.beans.factory.annotation.Value("${app.marketplace.platform-fee:0}")
  private BigDecimal platformFee;

  // Every transaction locks Listing then Order. Never reverse this lock order.
  public MarketplaceOrder lock(Long id) {
    var listingId = found(orders.listingId(id), "MARKET_ORDER_NOT_FOUND");
    found(listings.lock(listingId), "LISTING_NOT_FOUND");
    return found(orders.lock(id), "MARKET_ORDER_NOT_FOUND");
  }

  public void participant(MarketplaceOrder o) {
    if (!access.current().id().equals(o.getBuyer().getUserId()))
      access.ownerOrAdmin(o.getSeller().getUserId());
  }

  public EscrowTransaction escrow(MarketplaceOrder o) {
    return found(escrows.findByMarketOrderMarketOrderId(o.getMarketOrderId()), "ESCROW_NOT_FOUND");
  }

  public MarketOrderDetail view(MarketplaceOrder o) {
    return new MarketOrderDetail(
        MarketplaceOrderMapper.toResponse(o),
        EscrowTransactionMapper.toResponse(escrow(o)),
        shipments
            .findByMarketOrderMarketOrderId(o.getMarketOrderId())
            .map(ShipmentMapper::toResponse)
            .orElse(null),
        disputes
            .findByMarketOrderMarketOrderId(o.getMarketOrderId())
            .map(Dispute::getDisputeId)
            .orElse(null),
        ratings.existsByMarketOrderMarketOrderId(o.getMarketOrderId()));
  }

  @Transactional(readOnly = true)
  public MarketOrderDetail detail(Long id) {
    var o = found(orders.findById(id), "MARKET_ORDER_NOT_FOUND");
    participant(o);
    return view(o);
  }

  @Transactional(readOnly = true)
  public PageResponse<MarketplaceOrderResponse> list(boolean selling, int page, int size) {
    return PageResponse.from(
        orders
            .findAll(
                eq(selling ? "seller.userId" : "buyer.userId", access.current().id()),
                page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(MarketplaceOrderMapper::toResponse));
  }

  public MarketOrderDetail purchase(MarketOrderRequest r) {
    var user = access.user();
    var listing = found(listings.lock(r.listingId()), "LISTING_NOT_FOUND");
    require(
        listing.getStatus() == ListingStatus.AVAILABLE,
        "LISTING_UNAVAILABLE",
        "Tin đăng không còn khả dụng");
    bad(
        !user.getUserId().equals(listing.getSeller().getUserId()),
        "Không thể mua tin đăng của chính mình");
    var address =
        found(
            addresses.findByAddressIdAndUserUserId(r.addressId(), user.getUserId()),
            "ADDRESS_NOT_FOUND");
    var o = new MarketplaceOrder();
    o.setListing(listing);
    o.setBuyer(user);
    o.setSeller(listing.getSeller());
    o.setOrderCode(code("MKT"));
    o.setItemPrice(listing.getSellingPrice());
    o.setShippingFee(money(shippingFee));
    o.setPlatformFee(money(platformFee));
    o.setTotalAmount(money(o.getItemPrice().add(shippingFee).add(platformFee)));
    o.setReceiverName(address.getReceiverName());
    o.setReceiverPhone(address.getReceiverPhone());
    o.setShippingAddress(
        address.getDetailAddress()
            + ", "
            + address.getWard()
            + ", "
            + address.getProvince());
    o.setOrderStatus(MarketOrderStatus.PENDING_PAYMENT);
    orders.saveAndFlush(o);
    listing.setStatus(ListingStatus.RESERVED);
    var e = new EscrowTransaction();
    e.setMarketOrder(o);
    e.setAmount(o.getTotalAmount());
    e.setStatus(EscrowStatus.WAITING_PAYMENT);
    escrows.saveAndFlush(e);
    audit.record("PURCHASE", "MarketplaceOrder", o.getMarketOrderId(), null, o.getOrderStatus());
    notifyBoth(o, "Đơn Marketplace đã tạo");
    return view(o);
  }

  public MarketOrderDetail cancel(Long id) {
    var o = lock(id);
    access.owner(o.getBuyer().getUserId());
    cancelPending(o, "BUYER", "BUYER_CHANGED_MIND");
    return view(o);
  }

  public void expire(Long id, LocalDateTime cutoff) {
    var o = lock(id);
    if (o.getOrderStatus() == MarketOrderStatus.PENDING_PAYMENT
        && o.getCreatedAt().isBefore(cutoff)) cancelPending(o, "SYSTEM", "PAYMENT_TIMEOUT");
  }

  private void cancelPending(MarketplaceOrder o, String actor, String reason) {
    var e = escrow(o);
    require(
        o.getOrderStatus() == MarketOrderStatus.PENDING_PAYMENT
            && e.getStatus() == EscrowStatus.WAITING_PAYMENT,
        "MARKET_ORDER_INVALID_STATUS",
        "Chỉ hủy trước khi thanh toán");
    o.setOrderStatus(MarketOrderStatus.CANCELLED);
    o.setCancelledByType(actor);
    o.setCancellationReason(reason);
    e.setStatus(EscrowStatus.CANCELLED);
    o.getListing().setStatus(ListingStatus.AVAILABLE);
    audit.record(
        "CANCEL",
        "MarketplaceOrder",
        o.getMarketOrderId(),
        MarketOrderStatus.PENDING_PAYMENT,
        Map.of("status", o.getOrderStatus(), "cancelledByType", actor, "reason", reason));
    notifyBoth(o, "Đơn Marketplace đã hủy");
  }

  public MarketOrderDetail confirm(Long id) {
    var o = lock(id);
    access.owner(o.getBuyer().getUserId());
    var e = escrow(o);
    require(
        o.getOrderStatus() == MarketOrderStatus.DELIVERED && e.getStatus() == EscrowStatus.HELD,
        "MARKET_ORDER_INVALID_STATUS",
        "Chỉ xác nhận khi đã giao hàng và tiền đang được giữ");
    complete(o, e);
    audit.record(
        "CONFIRM_RECEIVED",
        "MarketplaceOrder",
        id,
        MarketOrderStatus.DELIVERED,
        o.getOrderStatus());
    notifyBoth(o, "Giao dịch đã hoàn tất");
    return view(o);
  }

  public void complete(MarketplaceOrder o, EscrowTransaction e) {
    o.setOrderStatus(MarketOrderStatus.COMPLETED);
    e.setStatus(EscrowStatus.RELEASED);
    e.setReleasedAt(LocalDateTime.now(ZoneOffset.UTC));
    o.getListing().setStatus(ListingStatus.SOLD);
  }

  public void notifyBoth(MarketplaceOrder o, String title) {
    for (var u : List.of(o.getBuyer(), o.getSeller()))
      notifications.send(
          u, "MARKET_ORDER", title, o.getOrderCode(), "MarketplaceOrder", o.getMarketOrderId());
  }
}
