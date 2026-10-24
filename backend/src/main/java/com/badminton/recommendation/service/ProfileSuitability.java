package com.badminton.recommendation.service;

import com.badminton.product.entity.RacketSpecification;
import com.badminton.product.dto.response.RacketSpecificationResponse;
import java.io.IOException;
import java.util.*;

/** Versioned business heuristics; only known catalog attributes contribute points. */
public final class ProfileSuitability {
  private static final Properties RULES = load();
  public record Result(Map<String, Boolean> matches, List<String> reasons, List<String> missing) {}

  private static Properties load() {
    var rules = new Properties();
    try (var input = ProfileSuitability.class.getResourceAsStream("/chatbot-suitability.properties")) {
      if (input == null) throw new IllegalStateException("Missing suitability rules");
      rules.load(input);
      return rules;
    } catch (IOException e) { throw new IllegalStateException("Cannot load suitability rules", e); }
  }

  public static Result evaluate(Map<String, List<Object>> profile, RacketSpecification spec) {
    Map<String, Object> facts = new HashMap<>();
    if (spec != null) {
      facts.put("weight", spec.getWeightClass()); facts.put("balance", spec.getBalanceType());
      facts.put("flex", spec.getShaftFlexibility());
    }
    return evaluateFacts(profile, facts);
  }

  public static Result evaluateView(Map<String, List<Object>> profile, RacketSpecificationResponse spec) {
    Map<String, Object> facts = new HashMap<>();
    if (spec != null) {
      facts.put("weight", spec.weightClass()); facts.put("balance", spec.balanceType());
      facts.put("flex", spec.shaftFlexibility());
    }
    return evaluateFacts(profile, facts);
  }

  private static Result evaluateFacts(Map<String, List<Object>> profile, Map<String, Object> facts) {
    var matches = new LinkedHashMap<String, Boolean>();
    var reasons = new ArrayList<String>();
    var missing = new LinkedHashSet<String>();
    for (String rule : new TreeSet<>(RULES.stringPropertyNames())) {
      String[] parts = rule.split("\\.");
      if (parts.length != 3 || !profile.getOrDefault(parts[0], List.of()).contains(parts[1])) continue;
      Object actual = facts.get(parts[2]);
      if (actual == null) { missing.add(parts[2]); continue; }
      boolean matched = Arrays.asList(RULES.getProperty(rule).split(",")).contains(actual.toString());
      matches.put(rule, matched);
      if (matched) reasons.add("Quy tắc " + rule + ": " + parts[2] + "=" + actual + " (+10 điểm)");
    }
    return new Result(Collections.unmodifiableMap(matches), List.copyOf(reasons), List.copyOf(missing));
  }

  private ProfileSuitability() {}
}
