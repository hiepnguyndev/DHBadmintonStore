package com.badminton.recommendation.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.ProductMapper;
import com.badminton.product.repository.*;
import com.badminton.product.service.ProductService;
import com.badminton.recommendation.dto.request.*;
import com.badminton.recommendation.dto.response.*;
import com.badminton.recommendation.entity.*;
import com.badminton.recommendation.mapper.*;
import com.badminton.recommendation.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecommendationService {
  private final PlayerProfileRepository profiles;
  private final ProductRepository products;
  private final RacketSpecificationRepository specs;
  private final RecommendationLogRepository logs;
  private final ProductService catalog;
  private final MatchScoreCalculator calculator;
  private final Access access;
  private final com.fasterxml.jackson.databind.ObjectMapper json;

  public RecommendationService(
      PlayerProfileRepository profiles,
      ProductRepository products,
      RacketSpecificationRepository specs,
      RecommendationLogRepository logs,
      ProductService catalog,
      MatchScoreCalculator calculator,
      Access access,
      com.fasterxml.jackson.databind.ObjectMapper json) {
    this.profiles = profiles;
    this.products = products;
    this.specs = specs;
    this.logs = logs;
    this.catalog = catalog;
    this.calculator = calculator;
    this.access = access;
    this.json = json;
  }

  @Transactional(readOnly = true)
  public PlayerProfileResponse profile() {
    return PlayerProfileMapper.toResponse(
        found(profiles.findByUserUserId(access.current().id()), "PROFILE_REQUIRED"));
  }

  public PlayerProfileResponse save(PlayerProfileRequest r, boolean create) {
    var u = access.lockUser();
    var existing = profiles.findByUserUserId(u.getUserId());
    require(
        create ? existing.isEmpty() : existing.isPresent(),
        create ? "PROFILE_EXISTS" : "PROFILE_REQUIRED",
        create ? "Hồ sơ đã tồn tại" : "Chưa có hồ sơ người chơi");
    bad(r.minBudget().compareTo(r.maxBudget()) <= 0, "Ngân sách tối thiểu phải <= tối đa");
    PlayerProfile p = existing.orElseGet(PlayerProfile::new);
    p.setUser(u);
    p.setSkillLevel(r.skillLevel());
    p.setPlayStyle(r.playStyle());
    p.setPlayType(r.playType());
    p.setWristStrength(r.wristStrength());
    p.setPreferredWeight(r.preferredWeight());
    p.setMinBudget(r.minBudget());
    p.setMaxBudget(r.maxBudget());
    p.setDominantHand(r.dominantHand());
    p.setBalanceType(r.balanceType());
    p.setShaftFlexibility(r.shaftFlexibility());
    p.setShirtSize(r.shirtSize());
    p.setPantsSize(r.pantsSize());
    p.setShoeSize(r.shoeSize());
    p.setFavoriteColors(r.favoriteColors());
    p.setFavoriteBrands(r.favoriteBrands());
    return PlayerProfileMapper.toResponse(profiles.saveAndFlush(p));
  }

  public List<RecommendationResult> recommend(int limit) {
    bad(limit >= 1 && limit <= 100, "limit trong 1–100");
    PlayerProfile p = found(profiles.findByUserUserId(access.current().id()), "PROFILE_REQUIRED");
    record Scored(Product product, MatchScoreCalculator.Result score) {}
    var results = new ArrayList<Scored>();
    for (var r : specs.findAll()) {
      var product = r.getProduct();
      if (product.getStatus() != ProductStatus.ACTIVE || product.getStock() <= 0) continue;
      var score = calculator.calculate(p, r, ProductMapper.effectivePrice(product));
      results.add(new Scored(product, score));
    }
    var top = results.stream()
        .sorted(Comparator.comparingInt((Scored x) -> x.score().matchScore()).reversed()
            .thenComparing(x -> x.product().getProductId()))
        .limit(limit).toList();
    var productViews = catalog.views(top.stream().map(Scored::product).toList());
    var response = new ArrayList<RecommendationResult>(top.size());
    for (int i = 0; i < top.size(); i++) {
      var x = top.get(i);
      var score = x.score();
      response.add(new RecommendationResult(x.product().getProductId(), x.product().getProductName(),
          score.matchScore(), score.reasons(), score.cautions(), score.breakdown(), productViews.get(i)));
    }
    for (var r : response) {
      RecommendationLog log = new RecommendationLog();
      log.setUser(p.getUser());
      log.setProduct(products.getReferenceById(r.productId()));
      log.setMatchScore(BigDecimal.valueOf(r.matchScore()));
      try {
        log.setReasonJson(json.writeValueAsString(r.breakdown()));
      } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
        throw new IllegalStateException(e);
      }
      logs.save(log);
    }
    return response;
  }
}
