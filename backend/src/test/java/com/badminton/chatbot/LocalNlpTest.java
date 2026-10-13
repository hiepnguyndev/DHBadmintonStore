package com.badminton.chatbot;

import static org.assertj.core.api.Assertions.*;

import com.badminton.chatbot.service.LocalNlp;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LocalNlpTest {
  private final LocalNlp nlp = new LocalNlp();

  @ParameterizedTest
  @CsvSource({
    "Vợt dưới 1 triệu, RACKET, 1000000",
    "VOT DUOI 1.5 TRIEU, RACKET, 1500000",
    "'Giày dưới 1,5 triệu', SHOES, 1500000",
    "Áo ngân sách 500k, APPAREL, 500000",
    "Túi không quá 1.500.000đ, BAG, 1500000",
    "'Dây cước tối đa 300,000 VND', STRING, 300000",
    "Racket under 2 million, RACKET, 2000000"
  })
  void recognizesCategoriesAndRealVndAmounts(String message, String category, String budget) {
    var criteria = nlp.extract(List.of(), message);
    assertThat(criteria.category()).isEqualTo(category);
    assertThat(criteria.maxBudget()).isEqualByComparingTo(budget);
  }

  @Test
  void followsUpWithoutRepeatingCategoryAndBudget() {
    var criteria = nlp.extract(List.of("Vợt dưới 2 triệu", "Màu xanh"), "rẻ hơn, 4U cho người mới chơi");
    assertThat(criteria.category()).isEqualTo("RACKET");
    assertThat(criteria.maxBudget()).isEqualByComparingTo("1600000");
    assertThat(criteria.color()).isEqualTo("xanh");
    assertThat(criteria.weight()).isEqualTo("4U");
    assertThat(criteria.skillLevel()).isEqualTo("BEGINNER");
  }

  @Test
  void switchingCategoriesDropsRacketOnlyConditions() {
    var criteria = nlp.extract(List.of("Vợt 4U tấn công cho người mới chơi dưới 2 triệu"), "đổi sang giày");
    assertThat(criteria.category()).isEqualTo("SHOES");
    assertThat(criteria.weight()).isNull();
    assertThat(criteria.playStyle()).isNull();
    assertThat(criteria.skillLevel()).isNull();
  }

  @Test
  void resetDoesNotCarryPreviousConversation() {
    var criteria = nlp.extract(List.of("Vợt dưới 2 triệu màu xanh"), "bắt đầu lại");
    assertThat(criteria.category()).isNull();
    assertThat(criteria.maxBudget()).isNull();
    assertThat(criteria.color()).isNull();
  }

  @Test
  void removesOptionalFiltersAndDoesNotConfuseVietnameseWordsWithColors() {
    var criteria = nlp.extract(List.of("Vợt dưới 2 triệu màu xanh"), "bỏ ngân sách, màu nào cũng được");
    assertThat(criteria.maxBudget()).isNull();
    assertThat(criteria.color()).isNull();
    assertThat(nlp.extract(List.of(), "Vợt trong tầm 1 triệu để đi chơi").color()).isNull();
    assertThat(LocalNlp.category("Xin chào")).isNull();
  }
}
