package com.badminton.valuation;

import static org.assertj.core.api.Assertions.*;

import com.badminton.marketplace.entity.ConditionGrade;
import com.badminton.valuation.service.ConditionScoreCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ConditionScoreTest {
  @Test
  void normalizeBeforeGrade() {
    assertThat(ConditionScoreCalculator.grade(new BigDecimal("9.495"))).isEqualTo(ConditionGrade.S);
    assertThat(ConditionScoreCalculator.grade(new BigDecimal("8.495"))).isEqualTo(ConditionGrade.A);
    assertThat(ConditionScoreCalculator.grade(new BigDecimal("6.995"))).isEqualTo(ConditionGrade.B);
    assertThat(ConditionScoreCalculator.grade(new BigDecimal("4.995"))).isEqualTo(ConditionGrade.C);
    assertThat(ConditionScoreCalculator.grade(new BigDecimal("4.994"))).isEqualTo(ConditionGrade.D);
  }

  @Test
  void componentWeights() {
    assertThat(
            ConditionScoreCalculator.score(
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO))
        .isEqualByComparingTo("3.50");
  }
}
