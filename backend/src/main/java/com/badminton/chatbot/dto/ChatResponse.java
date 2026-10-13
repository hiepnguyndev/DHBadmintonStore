package com.badminton.chatbot.dto;

import com.badminton.product.dto.response.ProductResponse;
import java.util.List;

public record ChatResponse(
    String resultKind, String answer, List<ProductResponse> products, String engine) {
  @Override
  public String toString() {
    return "ChatResponse[resultKind=" + resultKind + ", products=" + products.size() + ", engine=" + engine + "]";
  }
}
