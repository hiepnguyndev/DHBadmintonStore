package com.badminton.recommendation.service;

import com.badminton.common.enums.*;
import com.badminton.product.entity.RacketSpecification;
import com.badminton.recommendation.entity.PlayerProfile;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class MatchScoreCalculator {
  private static final int[][] STYLE = {
    {25, 8, 18, 14, 12},
    {8, 25, 18, 20, 16},
    {18, 18, 25, 18, 18},
    {14, 20, 18, 25, 18},
    {12, 16, 18, 18, 25}
  };
  private static final int[][] WRIST = {{15, 13, 6, 2}, {12, 15, 13, 7}, {8, 12, 15, 15}};

  public record Result(
      int matchScore,
      Map<String, Integer> breakdown,
      List<String> reasons,
      List<String> cautions) {}

  public int skillScore(SkillLevel a, SkillLevel b) {
    return new int[] {20, 12, 5}[Math.abs(a.ordinal() - b.ordinal())];
  }

  public int flexScore(SkillLevel skill, ShaftFlexibility flex) {
    int distance = Math.abs(skill.ordinal() - flex.ordinal());
    return distance == 0 ? 10 : distance == 1 ? 6 : 2;
  }

  public int budgetScore(BigDecimal price, BigDecimal min, BigDecimal max) {
    if (price.compareTo(min) < 0) return 12;
    if (price.compareTo(max) <= 0) return 15;
    if (price.compareTo(max.multiply(new BigDecimal("1.10"))) <= 0) return 10;
    if (price.compareTo(max.multiply(new BigDecimal("1.20"))) <= 0) return 5;
    return 0;
  }

  public Result calculate(PlayerProfile p, RacketSpecification r, BigDecimal price) {
    Map<String, Integer> scores = new LinkedHashMap<>();
    scores.put("skill", skillScore(p.getSkillLevel(), r.getSkillLevel()));
    scores.put("playStyle", STYLE[p.getPlayStyle().ordinal()][r.getPlayStyle().ordinal()]);
    scores.put("wrist", WRIST[p.getWristStrength().ordinal()][r.getShaftFlexibility().ordinal()]);
    int distance = Math.abs(p.getPreferredWeight().charAt(0) - r.getWeightClass().charAt(0));
    scores.put("weight", distance == 0 ? 15 : distance == 1 ? 10 : 5);
    scores.put("flex", flexScore(p.getSkillLevel(), r.getShaftFlexibility()));
    scores.put("budget", budgetScore(price, p.getMinBudget(), p.getMaxBudget()));
    List<String> reasons = new ArrayList<>(), cautions = new ArrayList<>();
    if (scores.get("skill") == 20) reasons.add("Phù hợp với trình độ của bạn");
    else cautions.add("Trình độ khuyến nghị của vợt khác hồ sơ của bạn");
    if (scores.get("playStyle") == 25) reasons.add("Phù hợp với lối chơi " + p.getPlayStyle());
    else if (scores.get("playStyle") < 14) cautions.add("Lối chơi của vợt chưa gần với nhu cầu");
    if (distance == 0) reasons.add("Trọng lượng " + r.getWeightClass() + " đúng sở thích");
    else cautions.add("Trọng lượng khác lựa chọn ưu tiên");
    if (scores.get("wrist") >= 12) reasons.add("Độ cứng tương thích với lực cổ tay");
    else cautions.add("Cần cân nhắc độ cứng so với lực cổ tay");
    if (scores.get("budget") == 15) reasons.add("Giá nằm trong ngân sách");
    else if (price.compareTo(p.getMaxBudget()) > 0) cautions.add("Giá vượt ngân sách tối đa");
    else reasons.add("Giá thấp hơn ngân sách tối thiểu");
    return new Result(
        Math.min(100, Math.max(0, scores.values().stream().mapToInt(Integer::intValue).sum())),
        Collections.unmodifiableMap(scores),
        List.copyOf(reasons),
        List.copyOf(cautions));
  }
}
