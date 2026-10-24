package com.badminton.recommendation.dto.response;

import com.badminton.product.dto.response.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** A chatbot sized result. A null score means no supported soft preference was scored. */
public record DialogueRecommendationResponse(
    String action,
    List<Item> results,
    List<String> warnings) {
  public record Item(
      Long productId,
      String productName,
      BigDecimal effectivePrice,
      Integer matchScore,
      Map<String, Integer> scoreBreakdown,
      List<String> reasons,
      List<VariantOption> availableVariants,
      ProductResponse product) {}

  public record VariantOption(String color, String size, int stock) {}
}
