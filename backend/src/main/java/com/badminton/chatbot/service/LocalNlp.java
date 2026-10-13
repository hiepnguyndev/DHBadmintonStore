package com.badminton.chatbot.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Bounded, local Vietnamese/English rules adapted from the standalone chatbot MVP. */
@Component
public class LocalNlp {
  public record Criteria(
      String category, BigDecimal maxBudget, String color, String weight,
      String playStyle, String skillLevel) {}

  private static final Pattern PRICE = Pattern.compile(
      "(?:duoi|khong qua|toi da|tam|khoang|ngan sach|under|budget|up to)\\s*"
          + "([0-9]+(?:[.,][0-9]+)*)\\s*(trieu|million|tr|nghin|ngan|k|dong|vnd|d)?\\b");
  private static final Pattern WEIGHT = Pattern.compile("\\b([2-6]u)\\b");

  public static String normalize(String value) {
    return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "").replace('\u0111', 'd').trim();
  }

  private static boolean has(String text, String words) {
    return Pattern.compile("\\b(?:" + words + ")\\b").matcher(text).find();
  }

  public static String category(String value) {
    String s = normalize(value);
    if (has(s, "cuoc|day vot|strings?")) return "STRING";
    if (has(s, "vot|rackets?")) return "RACKET";
    if (has(s, "giay|shoes?")) return "SHOES";
    if (has(s, "ao|quan|trang phuc|shirts?|shorts?|apparel|clothing")) return "APPAREL";
    if (has(s, "tui|balo|ba lo|bags?|backpacks?")) return "BAG";
    if (has(s, "qua cau|ong cau|shuttlecocks?")) return "SHUTTLE";
    if (has(s, "phu kien|accessories")) return "ACCESSORY";
    return null;
  }

  public Criteria extract(List<String> history, String message) {
    Criteria criteria = new Criteria(null, null, null, null, null, null);
    for (String previous : history == null ? List.<String>of() : history) {
      criteria = apply(criteria, previous);
    }
    return apply(criteria, message);
  }

  private Criteria apply(Criteria old, String message) {
    String s = normalize(message);
    if (has(s, "bat dau lai|xoa dieu kien|start over|reset")) {
      old = new Criteria(null, null, null, null, null, null);
    }
    String category = category(s);
    boolean switched = category != null && !category.equals(old.category());
    String currentCategory = category == null ? old.category() : category;
    BigDecimal budget = old.maxBudget();
    var price = PRICE.matcher(s);
    if (price.find()) {
      String number = price.group(1), unit = price.group(2);
      boolean million = unit != null && Set.of("trieu", "million", "tr").contains(unit);
      boolean thousand = unit != null && Set.of("nghin", "ngan", "k").contains(unit);
      // Grouped VND amounts differ from decimal amounts followed by trieu/k.
      if (!million && !thousand && number.matches("[0-9]{1,3}(?:[.,][0-9]{3})+")) {
        number = number.replace(".", "").replace(",", "");
      } else {
        number = number.replace(',', '.');
      }
      try {
        budget = new BigDecimal(number).multiply(BigDecimal.valueOf(
            million ? 1_000_000 : thousand ? 1000 : 1));
        if (budget.compareTo(new BigDecimal("9999999999999")) > 0) budget = null;
      } catch (NumberFormatException ignored) {
        budget = null;
      }
    }
    if (has(s, "khong gioi han|bo ngan sach|any budget|no budget")) budget = null;
    if (has(s, "re hon|gia thap hon|cheaper") && budget != null) {
      budget = budget.multiply(new BigDecimal("0.8")).setScale(0, java.math.RoundingMode.DOWN);
    }
    String color = old.color();
    if (has(s, "mau nao cung duoc|bo mau|any colou?r")) color = null;
    else {
      for (String[] entry : new String[][] {
          {"trang", "white"}, {"den", "black"}, {"xanh", "blue|green"},
          {"do", "red"}, {"vang", "yellow"}, {"hong", "pink"}, {"tim", "purple"}}) {
        if (has(s, "mau " + entry[0] + "|" + entry[1])) { color = entry[0]; break; }
      }
    }
    String weight = switched ? null : old.weight();
    String style = switched ? null : old.playStyle();
    String skill = switched ? null : old.skillLevel();
    if ("RACKET".equals(currentCategory)) {
      var weightMatch = WEIGHT.matcher(s);
      if (weightMatch.find()) weight = weightMatch.group(1).toUpperCase(Locale.ROOT);
      if (has(s, "tan cong|attack")) style = "ATTACK";
      else if (has(s, "phong thu|defen[cs]e")) style = "DEFENSE";
      else if (has(s, "can bang|toan dien|balanced")) style = "BALANCED";
      else if (has(s, "toc do|speed")) style = "SPEED";
      else if (has(s, "kiem soat|control")) style = "CONTROL";
      if (has(s, "moi choi|moi bat dau|beginner")) skill = "BEGINNER";
      else if (has(s, "trung cap|intermediate")) skill = "INTERMEDIATE";
      else if (has(s, "nang cao|advanced")) skill = "ADVANCED";
    }
    return new Criteria(currentCategory, budget, color, weight, style, skill);
  }
}
