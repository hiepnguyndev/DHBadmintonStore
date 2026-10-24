package com.badminton.recommendation;

import com.badminton.recommendation.service.ProfileSuitability;
import com.badminton.product.entity.RacketSpecification;
import com.badminton.common.enums.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProfileSuitabilityTest {
  @Test void doublesAndSpeedUseActualWeightAndBalance() {
    var spec = new RacketSpecification();
    spec.setWeightClass("4U"); spec.setBalanceType(BalanceType.HEAD_LIGHT);
    var result = ProfileSuitability.evaluate(Map.of("playType", List.of("DOUBLES"),
        "mainPreference", List.of("SPEED")), spec);
    assertThat(result.matches()).hasSize(4).allSatisfy((key, value) -> assertThat(value).isTrue());
    assertThat(result.reasons()).anyMatch(r -> r.contains("weight=4U"));
    assertThat(result.reasons()).anyMatch(r -> r.contains("balance=HEAD_LIGHT"));
  }

  @Test void missingSpecsDoNotReceivePoints() {
    var result = ProfileSuitability.evaluate(Map.of("skillLevel", List.of("BEGINNER")), null);
    assertThat(result.matches()).isEmpty();
    assertThat(result.reasons()).isEmpty();
    assertThat(result.missing()).containsExactlyInAnyOrder("weight", "balance", "flex");
  }

  @Test void partialSpecOnlyScoresKnownAttribute() {
    var spec = new RacketSpecification(); spec.setWeightClass("5U");
    var result = ProfileSuitability.evaluate(Map.of("skillLevel", List.of("BEGINNER")), spec);
    assertThat(result.matches()).containsOnlyKeys("skillLevel.BEGINNER.weight");
    assertThat(result.missing()).containsExactlyInAnyOrder("balance", "flex");
  }

  @Test void attackAndSinglesUseBalanceRatherThanInventingWeight() {
    var spec = new RacketSpecification(); spec.setBalanceType(BalanceType.HEAD_HEAVY);
    var result = ProfileSuitability.evaluate(Map.of("playStyle", List.of("ATTACK"),
        "playType", List.of("SINGLES")), spec);
    assertThat(result.matches()).hasSize(2).allSatisfy((key, value) -> assertThat(value).isTrue());
    assertThat(result.reasons()).allMatch(r -> r.contains("HEAD_HEAVY"));
  }
}
