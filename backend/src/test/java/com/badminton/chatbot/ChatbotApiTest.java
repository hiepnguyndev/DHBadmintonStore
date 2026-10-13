package com.badminton.chatbot;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.chatbot.controller.ChatbotController;
import com.badminton.chatbot.dto.ChatResponse;
import com.badminton.chatbot.service.*;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.security.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ChatbotController.class,
    properties = "app.cors.allowed-origins=http://localhost:5173")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class,
    GlobalExceptionHandler.class, ChatbotRateLimiter.class})
class ChatbotApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean ChatbotService service;
  @MockitoBean JwtService jwt;
  @MockitoBean CustomUserDetailsService details;

  @Test
  void anonymousChatUsesExistingApiEnvelope() throws Exception {
    when(service.reply(any())).thenReturn(new ChatResponse("NEED_INPUT", "Chào bạn", List.of(), "LOCAL_RULES"));
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Xin chào\",\"history\":[],\"language\":\"vi\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.engine").value("LOCAL_RULES"));
  }

  @Test
  void rejectsBlankMessagesAndNullHistoryEntries() throws Exception {
    for (String body : List.of("{\"message\":\" \"}", "{\"message\":\"Xin chào\",\"history\":[null]}")) {
      mvc.perform(post("/api/ai/chat").contentType("application/json").content(body))
          .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
    verifyNoInteractions(service);
  }

  @Test
  void rejectsOversizedMessagesAndUnsupportedLanguage() throws Exception {
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"" + "a".repeat(1001) + "\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Hello\",\"language\":\"invalid\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void rejectsOversizedHistoryAndUnknownFields() throws Exception {
    String history = String.join(",", java.util.Collections.nCopies(11, "\"Hello\""));
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Hello\",\"history\":[" + history + "]}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Hello\",\"userId\":1}"))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    verifyNoInteractions(service);
  }

  @Test
  void doesNotOpenOtherAiMethodsOrPrivateEndpoints() throws Exception {
    mvc.perform(get("/api/ai/chat")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/ai/other").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }
}
