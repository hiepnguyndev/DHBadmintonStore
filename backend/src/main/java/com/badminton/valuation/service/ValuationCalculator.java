package com.badminton.valuation.service;

import static com.badminton.common.support.Checks.bad;

import com.badminton.marketplace.entity.ConditionGrade;
import java.math.*;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Pure D04 calculations; request classifications approved in D15. */
@Component
public class ValuationCalculator {
  public enum Accessories {
    FULL,
    PARTIAL,
    MANY_MISSING
  }

  public enum Repair {
    NONE,
    MINOR,
    STRUCTURAL
  }

  public BigDecimal ageFactor(int months) {
    bad(months >= 0, "Tuổi dụng cụ không âm");
    return new BigDecimal(
        months < 3
            ? "0.95"
            : months < 6
                ? "0.90"
                : months < 12 ? "0.85" : months < 24 ? "0.75" : months <= 36 ? "0.65" : "0.55");
  }

  public BigDecimal conditionFactor(ConditionGrade grade) {
    bad(grade != null, "Cần mức tình trạng");
    return new BigDecimal(
        switch (grade) {
          case S -> "1.00";
          case A -> "0.92";
          case B -> "0.82";
          case C -> "0.68";
          case D -> "0.50";
        });
  }

  public BigDecimal warrantyFactor(LocalDate expiry, boolean knownExpired, LocalDate today) {
    bad(today != null, "Cần ngày tính định giá");
    if (expiry == null) return new BigDecimal(knownExpired ? "1.00" : "0.97");
    if (!expiry.isAfter(today)) return BigDecimal.ONE;
    return new BigDecimal(expiry.isAfter(today.plusMonths(6)) ? "1.05" : "1.02");
  }

  public BigDecimal accessoryFactor(Accessories state) {
    bad(state != null, "Thiếu tình trạng phụ kiện; không thể mặc định hệ số");
    return new BigDecimal(
        switch (state) {
          case FULL -> "1.03";
          case PARTIAL -> "1.00";
          case MANY_MISSING -> "0.97";
        });
  }

  public BigDecimal repairFactor(Repair state) {
    bad(state != null, "Thiếu mức sửa chữa; không thể mặc định hệ số");
    return new BigDecimal(
        switch (state) {
          case NONE -> "1.00";
          case MINOR -> "0.90";
          case STRUCTURAL -> "0.70";
        });
  }

  public BigDecimal demandFactor() {
    return BigDecimal.ONE;
  }

  public BigDecimal estimate(BigDecimal reference, BigDecimal... factors) {
    bad(reference != null && reference.signum() > 0, "Giá tham chiếu phải lớn hơn 0");
    bad(factors != null && factors.length == 6, "Cần đủ sáu hệ số định giá");
    BigDecimal value = reference;
    for (BigDecimal factor : factors) {
      bad(factor != null && factor.signum() > 0, "Hệ số định giá phải dương");
      value = value.multiply(factor);
    }
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  public record PriceRange(
      BigDecimal estimatedPrice, BigDecimal suggestedMinPrice, BigDecimal suggestedMaxPrice) {}

  public PriceRange range(BigDecimal reference, BigDecimal... factors) {
    BigDecimal value = estimate(reference, factors);
    return new PriceRange(
        value,
        value.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP),
        value.multiply(new BigDecimal("1.08")).setScale(2, RoundingMode.HALF_UP));
  }
}
