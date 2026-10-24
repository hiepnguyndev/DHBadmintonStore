package com.badminton.recommendation.dto.request;

import java.util.List;
import java.util.Map;

/** Internal bridge payload matching Dialogue Manager v1's action/slots state. */
public record DialogueRecommendationRequest(
    String action,
    Map<String, List<Object>> filters,
    Map<String, List<Object>> preferences,
    Map<String, List<Object>> exclusions,
    Integer limit,
    List<Long> excludedProductIds,
    List<Long> productIds) {
  public DialogueRecommendationRequest(String action, Map<String, List<Object>> filters,
      Map<String, List<Object>> preferences, Map<String, List<Object>> exclusions, Integer limit) {
    this(action, filters, preferences, exclusions, limit, List.of(), List.of());
  }
}
