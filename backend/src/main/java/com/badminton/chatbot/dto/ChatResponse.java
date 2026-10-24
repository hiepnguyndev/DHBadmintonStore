package com.badminton.chatbot.dto;

import com.badminton.product.dto.response.ProductResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ChatResponse(
    String resultKind, String answer, List<ProductResponse> products, String engine,
    @JsonInclude(JsonInclude.Include.NON_NULL) String contextToken) {
  public ChatResponse(String resultKind, String answer, List<ProductResponse> products, String engine) {
    this(resultKind, answer, products, engine, null);
  }

  @Override
  public String toString() {
    return "ChatResponse[resultKind=" + resultKind + ", products=" + products.size()
        + ", engine=" + engine + ", contextToken=<redacted>]";
  }
}
