package com.badminton.chatbot.service;

import com.badminton.exception.BusinessException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ChatbotRateLimiter {
  private record Window(long expires, int requests) {}
  private final Map<String, Window> windows = new HashMap<>();

  public synchronized void check(String address) {
    long now = System.nanoTime();
    windows.entrySet().removeIf(entry -> entry.getValue().expires() <= now);
    Window window = windows.get(address);
    if (window == null && windows.size() >= 4096 || window != null && window.requests() >= 30) {
      throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "CHAT_RATE_LIMIT",
          "Bạn gửi tin nhắn quá nhanh. Vui lòng thử lại sau một phút.");
    }
    windows.put(address, new Window(window == null ? now + 60_000_000_000L : window.expires(),
        window == null ? 1 : window.requests() + 1));
  }
}
