package com.badminton.rating.service;

import java.math.*;
import org.springframework.stereotype.Component;

@Component
public class SellerReputationCalculator {
  public record Score(
      BigDecimal reputationScore,
      BigDecimal disputeRate,
      BigDecimal cancellationRate,
      boolean newSeller,
      BigDecimal ratingScore,
      int completedScore,
      int disputeScore,
      int cancellationScore,
      int ageScore) {}

  public Score calculate(
      BigDecimal average,
      long completed,
      long eligible,
      long disputed,
      long cancelled,
      long months) {
    BigDecimal dr =
        eligible == 0
            ? null
            : BigDecimal.valueOf(disputed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(eligible), 8, RoundingMode.HALF_UP);
    BigDecimal cr =
        eligible == 0
            ? null
            : BigDecimal.valueOf(cancelled)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(eligible), 8, RoundingMode.HALF_UP);
    BigDecimal rs = average == null ? BigDecimal.ZERO : average.multiply(BigDecimal.valueOf(8));
    int cs =
        completed == 0 ? 0 : completed < 5 ? 5 : completed < 20 ? 10 : completed < 50 ? 15 : 20;
    int ds =
        dr == null
            ? 0
            : dr.signum() == 0
                ? 20
                : dr.compareTo(BigDecimal.valueOf(2)) <= 0
                    ? 18
                    : dr.compareTo(BigDecimal.valueOf(5)) <= 0
                        ? 14
                        : dr.compareTo(BigDecimal.TEN) <= 0 ? 8 : 3;
    int xs =
        cr == null
            ? 0
            : cr.signum() == 0
                ? 10
                : cr.compareTo(BigDecimal.valueOf(2)) <= 0
                    ? 9
                    : cr.compareTo(BigDecimal.valueOf(5)) <= 0
                        ? 7
                        : cr.compareTo(BigDecimal.TEN) <= 0 ? 4 : 0;
    int age = months < 1 ? 2 : months < 3 ? 4 : months < 6 ? 6 : months < 12 ? 8 : 10;
    return new Score(
        rs.add(BigDecimal.valueOf(cs + ds + xs + age)).setScale(2, RoundingMode.HALF_UP),
        dr == null ? null : dr.setScale(2, RoundingMode.HALF_UP),
        cr == null ? null : cr.setScale(2, RoundingMode.HALF_UP),
        eligible == 0,
        rs.setScale(2, RoundingMode.HALF_UP),
        cs,
        ds,
        xs,
        age);
  }
}
