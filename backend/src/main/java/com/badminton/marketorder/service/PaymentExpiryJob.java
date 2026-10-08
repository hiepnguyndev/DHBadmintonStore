package com.badminton.marketorder.service;

import com.badminton.marketorder.entity.MarketOrderStatus;
import com.badminton.marketorder.repository.MarketplaceOrderRepository;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpiryJob {
  private final MarketplaceOrderRepository orders;
  private final MarketOrderService market;
  private final long minutes;

  public PaymentExpiryJob(
      MarketplaceOrderRepository orders,
      MarketOrderService market,
      @Value("${app.marketplace.payment-timeout-minutes:30}") long minutes) {
    this.orders = orders;
    this.market = market;
    this.minutes = minutes;
  }

  @Scheduled(fixedDelayString = "${app.marketplace.expiry-interval-ms:60000}")
  public void expire() {
    var cutoff = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(minutes);
    for (var id : orders.expiredIds(MarketOrderStatus.PENDING_PAYMENT, cutoff)) {
      try {
        market.expire(id, cutoff);
      } catch (org.springframework.dao.TransientDataAccessException ex) {
        org.slf4j.LoggerFactory.getLogger(getClass())
            .warn("Expiry transaction will retry for order {}", id);
      }
    }
  }
}
