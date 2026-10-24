package com.badminton.chatbot.controller;

import com.badminton.chatbot.dto.ChatRequest;
import com.badminton.chatbot.service.*;
import com.badminton.common.dto.ApiResponse;
import com.badminton.recommendation.mapper.PlayerProfileMapper;
import com.badminton.recommendation.repository.PlayerProfileRepository;
import com.badminton.security.CurrentUser;
import com.badminton.user.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatbotController {
  private final ChatbotService service;
  private final ChatbotRateLimiter limiter;
  private final PlayerProfileRepository profiles;

  public ChatbotController(ChatbotService service, ChatbotRateLimiter limiter,
      PlayerProfileRepository profiles) {
    this.service = service;
    this.limiter = limiter;
    this.profiles = profiles;
  }

  @PostMapping("/api/ai/chat")
  public Object chat(@Valid @RequestBody ChatRequest request, HttpServletRequest http,
      @AuthenticationPrincipal CurrentUser currentUser) {
    limiter.check(http.getRemoteAddr());
    var profile = currentUser != null && currentUser.role() == Role.USER
        ? profiles.findByUserUserId(currentUser.id()).map(PlayerProfileMapper::toResponse).orElse(null)
        : null;
    return ApiResponse.ok("Thành công", service.reply(request, profile));
  }
}
