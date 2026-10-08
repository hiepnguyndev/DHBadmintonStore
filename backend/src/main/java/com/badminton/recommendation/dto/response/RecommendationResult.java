package com.badminton.recommendation.dto.response;

import com.badminton.product.dto.response.ProductResponse;
import java.util.*;

public record RecommendationResult(
    Long productId,
    String productName,
    int matchScore,
    List<String> reasons,
    List<String> cautions,
    Map<String, Integer> breakdown,
    ProductResponse product) {}
