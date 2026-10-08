package com.badminton.rating.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.marketorder.entity.MarketOrderStatus;
import com.badminton.rating.dto.request.*;
import com.badminton.rating.dto.response.*;
import com.badminton.rating.entity.*;
import com.badminton.rating.mapper.*;
import com.badminton.rating.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerRatingService {
  private final SellerRatingRepository ratings;
  private final com.badminton.marketorder.repository.MarketplaceOrderRepository orders;
  private final com.badminton.marketorder.service.MarketOrderService market;
  private final com.badminton.dispute.repository.DisputeRepository disputes;
  private final com.badminton.user.repository.UserRepository users;
  private final SellerReputationCalculator calculator;
  private final Access access;

  public SellerRatingService(
      SellerRatingRepository ratings,
      com.badminton.marketorder.repository.MarketplaceOrderRepository orders,
      com.badminton.marketorder.service.MarketOrderService market,
      com.badminton.dispute.repository.DisputeRepository disputes,
      com.badminton.user.repository.UserRepository users,
      SellerReputationCalculator calculator,
      Access access) {
    this.ratings = ratings;
    this.orders = orders;
    this.market = market;
    this.disputes = disputes;
    this.users = users;
    this.calculator = calculator;
    this.access = access;
  }

  public SellerRatingResponse rate(SellerRatingRequest r) {
    var o = market.lock(r.marketOrderId());
    access.owner(o.getBuyer().getUserId());
    require(
        o.getOrderStatus() == MarketOrderStatus.COMPLETED,
        "RATING_INVALID_STATUS",
        "Chỉ đánh giá giao dịch đã hoàn thành");
    require(
        !ratings.existsByMarketOrderMarketOrderId(o.getMarketOrderId()),
        "RATING_EXISTS",
        "Đơn đã được đánh giá");
    var rating = new SellerRating();
    rating.setMarketOrder(o);
    rating.setBuyer(o.getBuyer());
    rating.setSeller(o.getSeller());
    rating.setRating(r.rating());
    rating.setComment(r.comment());
    return SellerRatingMapper.toResponse(ratings.saveAndFlush(rating));
  }

  public static boolean sellerCancelled(com.badminton.marketorder.entity.MarketplaceOrder o) {
    return o.getOrderStatus() == MarketOrderStatus.CANCELLED
        && ("SELLER".equals(o.getCancelledByType())
            || "ADMIN".equals(o.getCancelledByType())
                && "SELLER_CANNOT_FULFILL".equals(o.getCancellationReason()));
  }

  public record Profile(
      Long sellerId,
      String fullName,
      LocalDateTime joinedAt,
      BigDecimal averageRating,
      long ratingCount,
      long completedTransactions,
      long eligibleReputationTransactionCount,
      long disputeTransactionCount,
      long sellerCancellationCount,
      SellerReputationCalculator.Score reputation) {}

  @Transactional(readOnly = true)
  public Profile profile(Long id) {
    var seller = found(users.findById(id), "SELLER_NOT_FOUND");
    var transactions = orders.findAll(eq("seller.userId", id));
    long completed =
        transactions.stream()
            .filter(o -> o.getOrderStatus() == MarketOrderStatus.COMPLETED)
            .count();
    var eligible =
        transactions.stream()
            .filter(
                o ->
                    o.getOrderStatus() == MarketOrderStatus.COMPLETED
                        || o.getOrderStatus() == MarketOrderStatus.REFUNDED
                        || sellerCancelled(o))
            .toList();
    long cancellations = eligible.stream().filter(SellerRatingService::sellerCancelled).count();
    long disputed =
        eligible.stream()
            .filter(o -> disputes.existsByMarketOrderMarketOrderId(o.getMarketOrderId()))
            .count();
    var rows = ratings.findAll(eq("seller.userId", id));
    BigDecimal average =
        rows.isEmpty()
            ? null
            : BigDecimal.valueOf(rows.stream().mapToInt(SellerRating::getRating).sum())
                .divide(BigDecimal.valueOf(rows.size()), 8, RoundingMode.HALF_UP);
    long age =
        java.time.temporal.ChronoUnit.MONTHS.between(
            seller.getCreatedAt(), LocalDateTime.now(ZoneOffset.UTC));
    return new Profile(
        id,
        seller.getFullName(),
        seller.getCreatedAt(),
        average == null ? null : average.setScale(2, RoundingMode.HALF_UP),
        rows.size(),
        completed,
        eligible.size(),
        disputed,
        cancellations,
        calculator.calculate(average, completed, eligible.size(), disputed, cancellations, age));
  }
}
