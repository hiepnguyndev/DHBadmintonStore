package com.badminton.chatbot.controller;

import com.badminton.chatbot.dto.ChatRequest;
import com.badminton.chatbot.service.*;
import com.badminton.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatbotController {
  private final ChatbotService service;
  private final ChatbotRateLimiter limiter;

  public ChatbotController(ChatbotService service, ChatbotRateLimiter limiter) {
    this.service = service;
    this.limiter = limiter;
  }

  @PostMapping("/api/ai/chat")
  public Object chat(@Valid @RequestBody ChatRequest request, HttpServletRequest http) {
    limiter.check(http.getRemoteAddr());
    return ApiResponse.ok("Thành công", service.reply(request));
  }
}
