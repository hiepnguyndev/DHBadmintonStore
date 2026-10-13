package com.badminton.chatbot.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record ChatRequest(
    @NotBlank @Size(max = 1000) String message,
    @Size(max = 10) List<@NotBlank @Size(max = 1000) String> history,
    @Pattern(regexp = "vi|en") String language) {
  @Override
  public String toString() {
    return "ChatRequest[message=<redacted>, history=<redacted>, language=" + language + "]";
  }
}
