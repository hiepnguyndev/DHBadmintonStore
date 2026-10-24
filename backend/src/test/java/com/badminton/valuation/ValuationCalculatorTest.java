package com.badminton.valuation;

import static org.assertj.core.api.Assertions.*;

import com.badminton.exception.BusinessException;
import com.badminton.marketplace.entity.ConditionGrade;
import com.badminton.valuation.service.ValuationCalculator;
import com.badminton.valuation.service.ValuationCalculator.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValuationCalculatorTest {
  final ValuationCalculator c = new ValuationCalculator();

  @Test
  void ageBoundaries() {
    int[] ages = {0, 2, 3, 5, 6, 11, 12, 23, 24, 36, 37};
    String[] expected = {
      ".95", ".95", ".90", ".90", ".85", ".85", ".75", ".75", ".65", ".65", ".55"
    };
    for (int i = 0; i < ages.length; i++)
      assertThat(c.ageFactor(ages[i])).isEqualByComparingTo(expected[i]);
    assertThatThrownBy(() -> c.ageFactor(-1)).isInstanceOf(BusinessException.class);
  }

  @Test
  void allConditionGrades() {
    String[] expected = {"1.00", ".92", ".82", ".68", ".50"};
    for (var grade : ConditionGrade.values())
      assertThat(c.conditionFactor(grade)).isEqualByComparingTo(expected[grade.ordinal()]);
  }

  @Test
  void warrantyEdgesAndUnknown() {
    var day = LocalDate.of(2026, 10, 8);
    assertThat(c.warrantyFactor(null, false, day)).isEqualByComparingTo(".97");
    assertThat(c.warrantyFactor(null, true, day)).isEqualByComparingTo("1");
    assertThat(c.warrantyFactor(day, true, day)).isEqualByComparingTo("1");
    assertThat(c.warrantyFactor(day.plusMonths(6), false, day)).isEqualByComparingTo("1.02");
    assertThat(c.warrantyFactor(day.plusMonths(6).plusDays(1), false, day))
        .isEqualByComparingTo("1.05");
  }

  @Test
  void accessoryAndRepairMappingsRejectUnknown() {
    assertThat(c.accessoryFactor(Accessories.FULL)).isEqualByComparingTo("1.03");
    assertThat(c.accessoryFactor(Accessories.PARTIAL)).isEqualByComparingTo("1.00");
    assertThat(c.accessoryFactor(Accessories.MANY_MISSING)).isEqualByComparingTo(".97");
    assertThat(c.repairFactor(Repair.NONE)).isEqualByComparingTo("1.00");
    assertThat(c.repairFactor(Repair.MINOR)).isEqualByComparingTo(".90");
    assertThat(c.repairFactor(Repair.STRUCTURAL)).isEqualByComparingTo(".70");
    assertThatThrownBy(() -> c.accessoryFactor(null)).isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> c.repairFactor(null)).isInstanceOf(BusinessException.class);
  }

  @Test
  void approvedExampleAndPriceRange() {
    var p =
        c.range(
            new BigDecimal("3000000"),
            c.ageFactor(10),
            c.conditionFactor(ConditionGrade.A),
            BigDecimal.ONE,
            c.accessoryFactor(Accessories.FULL),
            c.repairFactor(Repair.NONE),
            c.demandFactor());
    assertThat(p.estimatedPrice()).isEqualByComparingTo("2416380.00");
    assertThat(p.suggestedMinPrice()).isEqualByComparingTo("2174742.00");
    assertThat(p.suggestedMaxPrice()).isEqualByComparingTo("2609690.40");
  }

  @Test
  void missingFactorsCannotIssueEstimate() {
    assertThatThrownBy(() -> c.estimate(BigDecimal.TEN, BigDecimal.ONE))
        .isInstanceOf(BusinessException.class);
  }
}
