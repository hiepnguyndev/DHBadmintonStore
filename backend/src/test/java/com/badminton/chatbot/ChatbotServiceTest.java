package com.badminton.chatbot;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badminton.chatbot.dto.ChatRequest;
import com.badminton.chatbot.service.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatbotServiceTest {
  private final ChatbotCatalog catalog = mock(ChatbotCatalog.class);
  private final ChatbotService service = new ChatbotService(new LocalNlp(), catalog);

  @Test
  void asksForMissingCategoryWithoutReadingCatalog() {
    var response = service.reply(new ChatRequest("Xin chào", List.of(), "vi"));
    assertThat(response.resultKind()).isEqualTo("NEED_INPUT");
    assertThat(response.engine()).isEqualTo("LOCAL_RULES");
    assertThat(response.products()).isEmpty();
    verifyNoInteractions(catalog);
  }

  @Test
  void emptyCatalogNeverCreatesDemoProducts() {
    when(catalog.find(any())).thenReturn(List.of());
    var response = service.reply(new ChatRequest("Vợt dưới 1 triệu", List.of(), "vi"));
    assertThat(response.resultKind()).isEqualTo("NO_MATCH");
    assertThat(response.answer()).contains("chưa có sản phẩm còn hàng");
    assertThat(response.products()).isEmpty();
  }

  @Test
  void answersInRequestedLanguage() {
    var response = service.reply(new ChatRequest("Hello", null, "en"));
    assertThat(response.answer()).startsWith("Hello!");
  }

  @Test
  void debugRepresentationsDoNotIncludeConversationContents() {
    var request = new ChatRequest("private-message", List.of("private-history"), "vi");
    assertThat(request.toString()).doesNotContain("private-message", "private-history");
    assertThat(service.reply(request).toString()).doesNotContain("answer=");
  }

  @Test
  void limiterBoundsRequestsAndSeparatesClients() {
    var limiter = new ChatbotRateLimiter();
    for (int i = 0; i < 30; i++) limiter.check("127.0.0.1");
    assertThatThrownBy(() -> limiter.check("127.0.0.1"))
        .isInstanceOf(com.badminton.exception.BusinessException.class)
        .hasMessageContaining("một phút");
    assertThatCode(() -> limiter.check("127.0.0.2")).doesNotThrowAnyException();
  }
}
