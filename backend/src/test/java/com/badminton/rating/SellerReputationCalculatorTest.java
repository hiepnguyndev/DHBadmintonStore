package com.badminton.rating;

import static org.assertj.core.api.Assertions.*;

import com.badminton.marketorder.entity.*;
import com.badminton.rating.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SellerReputationCalculatorTest {
  private final SellerReputationCalculator c = new SellerReputationCalculator();

  @Test
  void zeroDenominatorIsUnknownNotZeroPercent() {
    var s = c.calculate(null, 0, 0, 0, 0, 0);
    assertThat(s.disputeRate()).isNull();
    assertThat(s.cancellationRate()).isNull();
    assertThat(s.disputeScore()).isZero();
    assertThat(s.cancellationScore()).isZero();
    assertThat(s.newSeller()).isTrue();
    assertThat(s.reputationScore()).isEqualByComparingTo("2");
  }

  @Test
  void perfectExperiencedSellerHas100() {
    assertThat(c.calculate(BigDecimal.valueOf(5), 50, 50, 0, 0, 12).reputationScore())
        .isEqualByComparingTo("100");
  }

  @Test
  void thresholdsUseUnroundedRates() {
    assertThat(c.calculate(null, 1, 100, 2, 2, 1).disputeScore()).isEqualTo(18);
    assertThat(c.calculate(null, 1, 10000, 201, 201, 1).disputeScore()).isEqualTo(14);
    assertThat(c.calculate(null, 1, 10000, 201, 201, 1).cancellationScore()).isEqualTo(7);
  }

  @Test
  void cancellationAttribution() {
    var o = new MarketplaceOrder();
    o.setOrderStatus(MarketOrderStatus.CANCELLED);
    for (String actor : new String[] {"BUYER", "SYSTEM"}) {
      o.setCancelledByType(actor);
      assertThat(SellerRatingService.sellerCancelled(o)).isFalse();
    }
    o.setCancelledByType("SELLER");
    assertThat(SellerRatingService.sellerCancelled(o)).isTrue();
    o.setCancelledByType("ADMIN");
    o.setCancellationReason("ADMIN_CANCELLED");
    assertThat(SellerRatingService.sellerCancelled(o)).isFalse();
    o.setCancellationReason("SELLER_CANNOT_FULFILL");
    assertThat(SellerRatingService.sellerCancelled(o)).isTrue();
    o.setOrderStatus(MarketOrderStatus.DISPUTED);
    assertThat(SellerRatingService.sellerCancelled(o)).isFalse();
  }
}
