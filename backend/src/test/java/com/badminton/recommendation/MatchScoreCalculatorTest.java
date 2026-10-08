package com.badminton.recommendation;

import static org.assertj.core.api.Assertions.*;

import com.badminton.common.enums.*;
import com.badminton.product.entity.RacketSpecification;
import com.badminton.recommendation.entity.PlayerProfile;
import com.badminton.recommendation.service.MatchScoreCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MatchScoreCalculatorTest {
  private final MatchScoreCalculator c = new MatchScoreCalculator();

  @Test
  void approvedFlexMatrix() {
    int[][] expected = {{10, 6, 2, 2}, {6, 10, 6, 2}, {2, 6, 10, 6}};
    for (var skill : SkillLevel.values())
      for (var flex : ShaftFlexibility.values())
        assertThat(c.flexScore(skill, flex)).isEqualTo(expected[skill.ordinal()][flex.ordinal()]);
  }

  @Test
  void exactBudgetEdges() {
    BigDecimal min = new BigDecimal("100"), max = new BigDecimal("200");
    String[] prices = {"99.99", "100", "200", "200.01", "220", "220.01", "240", "240.01"};
    int[] expected = {12, 15, 15, 10, 10, 5, 5, 0};
    for (int i = 0; i < prices.length; i++)
      assertThat(c.budgetScore(new BigDecimal(prices[i]), min, max)).isEqualTo(expected[i]);
  }

  @Test
  void allCombinationsRemainDeterministicAndBounded() {
    var p = new PlayerProfile();
    p.setPreferredWeight("4U");
    p.setMinBudget(BigDecimal.ZERO);
    p.setMaxBudget(new BigDecimal("2000000"));
    var r = new RacketSpecification();
    r.setWeightClass("4U");
    for (var skill : SkillLevel.values())
      for (var targetSkill : SkillLevel.values())
        for (var style : PlayStyle.values())
          for (var targetStyle : PlayStyle.values())
            for (var wrist : WristStrength.values())
              for (var flex : ShaftFlexibility.values()) {
                p.setSkillLevel(skill);
                p.setPlayStyle(style);
                p.setWristStrength(wrist);
                r.setSkillLevel(targetSkill);
                r.setPlayStyle(targetStyle);
                r.setShaftFlexibility(flex);
                var result = c.calculate(p, r, BigDecimal.valueOf(1000000));
                assertThat(result.matchScore()).isBetween(0, 100);
                assertThat(result).isEqualTo(c.calculate(p, r, BigDecimal.valueOf(1000000)));
                assertThat(result.breakdown().values().stream().mapToInt(Integer::intValue).sum())
                    .isEqualTo(result.matchScore());
              }
  }

  @Test
  void exactMatchHas100Points() {
    var p = new PlayerProfile();
    p.setSkillLevel(SkillLevel.ADVANCED);
    p.setPlayStyle(PlayStyle.ATTACK);
    p.setWristStrength(WristStrength.STRONG);
    p.setPreferredWeight("3U");
    p.setMinBudget(BigDecimal.ZERO);
    p.setMaxBudget(BigDecimal.valueOf(100));
    var r = new RacketSpecification();
    r.setSkillLevel(SkillLevel.ADVANCED);
    r.setPlayStyle(PlayStyle.ATTACK);
    r.setShaftFlexibility(ShaftFlexibility.STIFF);
    r.setWeightClass("3U");
    assertThat(c.calculate(p, r, BigDecimal.TEN).matchScore()).isEqualTo(100);
  }
}
