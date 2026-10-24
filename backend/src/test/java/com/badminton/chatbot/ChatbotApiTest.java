package com.badminton.chatbot;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.chatbot.controller.ChatbotController;
import com.badminton.chatbot.dto.ChatResponse;
import com.badminton.chatbot.service.*;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.recommendation.repository.PlayerProfileRepository;
import com.badminton.recommendation.entity.PlayerProfile;
import com.badminton.user.entity.Role;
import com.badminton.security.*;
import java.util.List;
import java.util.Optional;
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
  @MockitoBean PlayerProfileRepository profiles;

  @Test
  void anonymousChatUsesExistingApiEnvelope() throws Exception {
    when(service.reply(any(), isNull())).thenReturn(new ChatResponse("NOT_CONFIGURED", "Chào bạn", List.of(), "NONE"));
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Xin chào\",\"history\":[],\"language\":\"vi\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.engine").value("NONE"))
        .andExpect(jsonPath("$.data.answer").value("Chào bạn"))
        .andExpect(jsonPath("$.data.contextToken").doesNotExist());
  }

  @Test
  void authenticatedChatLoadsOnlyThatUsersSavedPlayerProfile() throws Exception {
    when(jwt.userId("valid-token")).thenReturn(42L);
    when(details.loadActiveUser(42L)).thenReturn(new CurrentUser(42L, Role.USER));
    when(profiles.findByUserUserId(42L)).thenReturn(Optional.of(mock(PlayerProfile.class)));
    when(service.reply(any(), any())).thenReturn(new ChatResponse("TEXT", "Đã cá nhân hóa", List.of(), "GROQ"));

    mvc.perform(post("/api/ai/chat").header("Authorization", "Bearer valid-token")
            .contentType("application/json")
            .content("{\"message\":\"Tư vấn vợt cho tôi\",\"history\":[],\"language\":\"vi\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.answer").value("Đã cá nhân hóa"));

    verify(profiles).findByUserUserId(42L);
    verify(service).reply(any(), argThat(profile -> profile != null));
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
    mvc.perform(post("/api/ai/chat").contentType("application/json")
            .content("{\"message\":\"Hello\",\"contextToken\":\"" + "x".repeat(129) + "\"}"))
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
