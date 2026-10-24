package com.badminton.valuation.service;

import static com.badminton.common.support.Checks.bad;

import com.badminton.marketplace.entity.ConditionGrade;
import java.math.*;

public final class ConditionScoreCalculator {
  private ConditionScoreCalculator() {}

  public static BigDecimal score(
      BigDecimal frame,
      BigDecimal crack,
      BigDecimal paint,
      BigDecimal grommet,
      BigDecimal handle,
      BigDecimal repair) {
    var values = new BigDecimal[] {frame, crack, paint, grommet, handle, repair};
    var weights = new String[] {"0.35", "0.25", "0.10", "0.10", "0.10", "0.10"};
    BigDecimal sum = BigDecimal.ZERO;
    for (int i = 0; i < values.length; i++) {
      bad(
          values[i] != null && values[i].signum() >= 0 && values[i].compareTo(BigDecimal.TEN) <= 0,
          "Điểm thành phần trong 0–10");
      sum = sum.add(values[i].multiply(new BigDecimal(weights[i])));
    }
    return sum.setScale(2, RoundingMode.HALF_UP);
  }

  public static ConditionGrade grade(BigDecimal score) {
    bad(
        score != null && score.signum() >= 0 && score.compareTo(BigDecimal.TEN) <= 0,
        "Condition Score trong 0–10");
    score = score.setScale(2, RoundingMode.HALF_UP);
    return score.compareTo(new BigDecimal("9.5")) >= 0
        ? ConditionGrade.S
        : score.compareTo(new BigDecimal("8.5")) >= 0
            ? ConditionGrade.A
            : score.compareTo(new BigDecimal("7")) >= 0
                ? ConditionGrade.B
                : score.compareTo(new BigDecimal("5")) >= 0 ? ConditionGrade.C : ConditionGrade.D;
  }
}
