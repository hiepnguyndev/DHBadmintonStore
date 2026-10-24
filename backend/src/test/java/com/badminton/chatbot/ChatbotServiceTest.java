package com.badminton.chatbot;

import static org.assertj.core.api.Assertions.assertThat;
import com.badminton.chatbot.dto.ChatRequest;
import com.badminton.chatbot.service.ChatbotService;
import com.badminton.product.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ChatbotServiceTest {
  @Test void reportsUnconfiguredWithoutGeneratingRecommendations() {
    var service = new ChatbotService(RestClient.builder(), "", "gpt-4.1-mini", "", "openai/gpt-oss-20b",
        org.mockito.Mockito.mock(ProductService.class));
    for (String language : List.of("vi", "en")) {
      var response = service.reply(new ChatRequest("Recommend a racket", List.of(), language));
      assertThat(response.resultKind()).isEqualTo("TEXT");
      assertThat(response.engine()).isEqualTo("LOCAL_CATALOG");
      assertThat(response.products()).isEmpty();
      assertThat(response.contextToken()).isNull();
      assertThat(response.answer()).contains(language.equals("en") ? "temporarily unavailable" : "tạm hết hạn mức");
    }
  }
}
