package com.badminton.chatbot.service;

import com.badminton.chatbot.dto.*;
import java.text.NumberFormat;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {
  private final LocalNlp nlp;
  private final ChatbotCatalog catalog;

  public ChatbotService(LocalNlp nlp, ChatbotCatalog catalog) {
    this.nlp = nlp;
    this.catalog = catalog;
  }

  public ChatResponse reply(ChatRequest request) {
    boolean en = "en".equals(request.language());
    var criteria = nlp.extract(request.history(), request.message());
    if (criteria.category() == null) {
      return new ChatResponse("NEED_INPUT", en
          ? "Hello! Are you looking for a racket, shoes, clothing, a bag or strings? What is your budget?"
          : "Chào bạn! Bạn đang tìm vợt, giày, trang phục, túi hay dây cước? Ngân sách của bạn khoảng bao nhiêu?",
          List.of(), "LOCAL_RULES");
    }
    var products = catalog.find(criteria);
    if (products.isEmpty()) {
      return new ChatResponse("NO_MATCH", en
          ? "The store currently has no available products matching these conditions. Would you like to change the budget or preferences?"
          : "Cửa hàng hiện chưa có sản phẩm còn hàng đáp ứng các điều kiện này. Bạn muốn đổi ngân sách hoặc tiêu chí lựa chọn không?",
          List.of(), "LOCAL_RULES");
    }
    String budget = criteria.maxBudget() == null ? "" : (en ? " within " : " trong ngân sách ")
        + NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(criteria.maxBudget()) + " ₫";
    return new ChatResponse("RECOMMENDATIONS", en
        ? "Here are " + products.size() + " available products" + budget + ". Prices and stock come from the store catalog."
        : "Mình tìm thấy " + products.size() + " sản phẩm còn hàng" + budget + ". Giá và tồn kho được lấy từ danh mục cửa hàng.",
        products, "LOCAL_RULES");
  }
}
