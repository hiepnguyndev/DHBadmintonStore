package com.badminton.recommendation.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.brand.entity.BrandStatus;
import com.badminton.category.entity.CategoryStatus;
import com.badminton.common.enums.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.ProductMapper;
import com.badminton.product.repository.*;
import com.badminton.product.service.ProductService;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.recommendation.dto.request.*;
import com.badminton.recommendation.dto.response.*;
import com.badminton.recommendation.entity.*;
import com.badminton.recommendation.mapper.*;
import com.badminton.recommendation.repository.*;
import java.math.*;
import java.time.*;
import java.text.Normalizer;
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
  private final ProductVariantRepository variants;

  public RecommendationService(
      PlayerProfileRepository profiles,
      ProductRepository products,
      RacketSpecificationRepository specs,
      RecommendationLogRepository logs,
      ProductService catalog,
      MatchScoreCalculator calculator,
      Access access,
      com.fasterxml.jackson.databind.ObjectMapper json,
      ProductVariantRepository variants) {
    this.profiles = profiles;
    this.products = products;
    this.specs = specs;
    this.logs = logs;
    this.catalog = catalog;
    this.calculator = calculator;
    this.access = access;
    this.json = json;
    this.variants = variants;
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
    if (results.isEmpty()) return fallbackCatalogRecommendations(p, limit);
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
      if (r.matchScore() == null) continue;
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

  /**
   * Keep profile suggestions useful when imported products have stock but lack
   * normalized racket specifications. Such items are explicitly marked unscored.
   */
  private List<RecommendationResult> fallbackCatalogRecommendations(PlayerProfile profile, int limit) {
    record Candidate(Product product, BigDecimal price, Map<String, Integer> evidence,
                     List<String> reasons, List<String> cautions, int matched, boolean inBudget) {}
    var candidates = new ArrayList<Candidate>();
    for (Product product : products.findCatalogCandidates(ProductStatus.ACTIVE,
        BrandStatus.ACTIVE, CategoryStatus.ACTIVE)) {
      if (product.getStock() == null || product.getStock() <= 0 || product.getCategory() == null
          || !matchesCategory(product.getCategory().getCategoryName(), "racket")) continue;
      BigDecimal price = effectivePrice(product);
      if (price == null) continue;
      RacketSpecification spec = specificationFromDescription(product);
      var evidence = new LinkedHashMap<String, Integer>();
      var reasons = new ArrayList<String>();
      var cautions = new ArrayList<String>();
      int matched = 0;
      if (spec != null && profile.getPreferredWeight() != null && spec.getWeightClass() != null) {
        boolean match = profile.getPreferredWeight().equalsIgnoreCase(spec.getWeightClass());
        evidence.put("weight", match ? 10 : 0);
        if (match) { matched++; reasons.add("Trọng lượng ghi trong mô tả trùng ưu tiên hồ sơ"); }
        else cautions.add("Trọng lượng trong mô tả khác ưu tiên hồ sơ");
      }
      if (spec != null && profile.getBalanceType() != null && spec.getBalanceType() != null) {
        boolean match = profile.getBalanceType().equalsIgnoreCase(spec.getBalanceType().name());
        evidence.put("balance", match ? 10 : 0);
        if (match) { matched++; reasons.add("Cân bằng vợt ghi trong mô tả trùng ưu tiên hồ sơ"); }
        else cautions.add("Cân bằng vợt trong mô tả khác ưu tiên hồ sơ");
      }
      if (spec != null && profile.getShaftFlexibility() != null && spec.getShaftFlexibility() != null) {
        boolean match = profile.getShaftFlexibility().equalsIgnoreCase(spec.getShaftFlexibility().name());
        evidence.put("shaftFlex", match ? 10 : 0);
        if (match) { matched++; reasons.add("Độ cứng thân ghi trong mô tả trùng ưu tiên hồ sơ"); }
        else cautions.add("Độ cứng thân trong mô tả khác ưu tiên hồ sơ");
      }
      boolean inBudget = price.compareTo(profile.getMinBudget()) >= 0
          && price.compareTo(profile.getMaxBudget()) <= 0;
      evidence.put("budget", inBudget ? 10 : 0);
      if (inBudget) { matched++; reasons.add("Giá nằm trong ngân sách của hồ sơ"); }
      else if (price.compareTo(profile.getMaxBudget()) > 0) cautions.add("Giá vượt ngân sách tối đa trong hồ sơ");
      else reasons.add("Giá thấp hơn ngân sách tối thiểu trong hồ sơ");
      cautions.add("Thiếu thông số kỹ thuật đã xác minh; sản phẩm được xếp theo dữ liệu hồ sơ có thể đối chiếu, chưa có điểm phù hợp tổng thể");
      candidates.add(new Candidate(product, price, Map.copyOf(evidence), List.copyOf(reasons),
          List.copyOf(cautions), matched, inBudget));
    }
    if (candidates.isEmpty()) return List.of();
    boolean hasInBudget = candidates.stream().anyMatch(Candidate::inBudget);
    var top = candidates.stream()
        .filter(candidate -> !hasInBudget || candidate.inBudget())
        .sorted(Comparator.comparingInt(Candidate::matched).reversed()
            .thenComparing(Candidate::price)
            .thenComparing(candidate -> candidate.product().getProductId()))
        .limit(limit).toList();
    var views = catalog.views(top.stream().map(Candidate::product).toList());
    var response = new ArrayList<RecommendationResult>();
    for (int i = 0; i < top.size(); i++) {
      Candidate candidate = top.get(i);
      var cautions = new ArrayList<>(candidate.cautions());
      if (!hasInBudget) cautions.add("Chưa có vợt còn hàng nằm trong khoảng ngân sách; đây là lựa chọn giá gần nhất");
      response.add(new RecommendationResult(candidate.product().getProductId(), candidate.product().getProductName(),
          null, candidate.reasons(), List.copyOf(cautions), candidate.evidence(), views.get(i)));
    }
    return List.copyOf(response);
  }

  /**
   * Dialogue Manager bridge. Uses the catalog and match-score component, but
   * intentionally does not require a profile, write recommendation logs, or
   * perform any authorization-sensitive action. Not exposed as an HTTP route.
   */
  @Transactional(readOnly = true)
  public DialogueRecommendationResponse recommendForDialogue(DialogueRecommendationRequest request) {
    var warnings = new LinkedHashSet<String>();
    if (request == null || !("RECOMMEND_PRODUCTS".equals(request.action())
        || "SEARCH_PRODUCTS".equals(request.action()) || "LOOKUP_PRODUCTS".equals(request.action()))) {
      return new DialogueRecommendationResponse("SAFE_RESPONSE", List.of(),
          List.of("Unsupported dialogue action; no catalog query was performed"));
    }
    int limit = request.limit() == null ? 3 : Math.max(1, Math.min(3, request.limit()));
    var filters = normalizeSlots(request.filters(), "filters", warnings);
    var preferences = normalizeSlots(request.preferences(), "preferences", warnings);
    var exclusions = normalizeSlots(request.exclusions(), "exclusions", warnings);
    var query = new DialogueQuery(filters, preferences, exclusions, warnings);
    boolean lookup = "LOOKUP_PRODUCTS".equals(request.action())
        || filters.getOrDefault("availability", List.of()).contains("OUT_OF_STOCK");
    if (filters.isEmpty() && preferences.isEmpty()) {
      warnings.add("No explicit matching criteria or preferences were supplied");
      return new DialogueRecommendationResponse(request.action(), List.of(), List.copyOf(warnings));
    }
    if (!query.prepare()) {
      return new DialogueRecommendationResponse(request.action(), List.of(), List.copyOf(warnings));
    }

    var candidates = new ArrayList<>(products.findCatalogCandidates(ProductStatus.ACTIVE,
        BrandStatus.ACTIVE, CategoryStatus.ACTIVE));
    if (lookup) candidates.addAll(products.findCatalogCandidates(ProductStatus.OUT_OF_STOCK,
        BrandStatus.ACTIVE, CategoryStatus.ACTIVE));
    if (candidates.isEmpty())
      return new DialogueRecommendationResponse(request.action(), List.of(), List.copyOf(warnings));
    var ids = candidates.stream().map(Product::getProductId).toList();
    var specByProduct = new HashMap<Long, RacketSpecification>();
    specs.findByProductProductIdIn(ids).forEach(s -> specByProduct.put(s.getProduct().getProductId(), s));
    var variantsByProduct = new HashMap<Long, List<ProductVariant>>();
    var productsWithVariants = new HashSet<Long>();
    variants.findByProductProductIdIn(ids).forEach(v -> {
      Long productId = v.getProduct().getProductId();
      productsWithVariants.add(productId);
      variantsByProduct.computeIfAbsent(productId, ignored -> new ArrayList<>()).add(v);
    });

    boolean shoeQuery = query.has("shoeSize", filters) || query.has("shoeSize", preferences);
    String requestedCategory = filters.getOrDefault("category", List.of()).stream()
        .filter(String.class::isInstance).map(String.class::cast).findFirst().orElse(null);
    boolean racketQuery = requestedCategory == null ? !shoeQuery : requestedCategory.equalsIgnoreCase("racket");
    record Scored(Product product, BigDecimal price, Integer score, Map<String, Integer> breakdown,
                  List<String> reasons, List<ProductVariant> availableVariants) {}
    var scored = new ArrayList<Scored>();
    for (Product product : candidates) {
      Long id = product.getProductId();
      if (request.excludedProductIds() != null && request.excludedProductIds().contains(id)) continue;
      if (request.productIds() != null && !request.productIds().isEmpty() && !request.productIds().contains(id)) continue;
      if (!(product.getStatus() == ProductStatus.ACTIVE || lookup && product.getStatus() == ProductStatus.OUT_OF_STOCK)
          || effectivePrice(product) == null) continue;
      if (!lookup && (product.getStock() == null || product.getStock() <= 0)) continue;
      if (requestedCategory != null && !matchesCategory(product.getCategory().getCategoryName(), requestedCategory)) continue;
      RacketSpecification spec = specByProduct.get(id);
      if (spec == null && racketQuery) spec = specificationFromDescription(product);
      boolean needsRacketSpec = List.of("racketWeight", "balance", "balancePoint", "shaftFlex", "playStyle", "skillLevel", "stringTension")
          .stream().anyMatch(k -> query.has(k, filters));
      if (racketQuery && needsRacketSpec && spec == null) {
        if (needsRacketSpec)
          warnings.add("Racket specification data is missing; required specification filters were not inferred");
        continue;
      }
      var available = variantsByProduct.getOrDefault(id, List.of()).stream()
          .filter(v -> ("ACTIVE".equalsIgnoreCase(v.getStatus()) || lookup && "OUT_OF_STOCK".equalsIgnoreCase(v.getStatus()))
              && v.getStock() != null && (lookup || v.getStock() > 0))
          .filter(v -> !query.variantExcluded(v, exclusions))
          .toList();
      if (!lookup && productsWithVariants.contains(id) && available.isEmpty()) continue;
      if (!productsWithVariants.contains(id) && List.of("color", "shoeSize", "apparelSize")
          .stream().anyMatch(k -> query.has(k, exclusions))) continue;
      if (query.matchesHard(product, spec, available, filters)) continue;
      if (query.matchesExclusion(product, spec, exclusions)) continue;

      var preferenceMatches = query.preferenceMatches(product, spec, available, preferences, warnings);
      var suitability = ProfileSuitability.evaluate(matchesCategory(product.getCategory().getCategoryName(), "racket") ? preferences : Map.of(), spec);
      preferenceMatches.putAll(suitability.matches());
      var preferenceScore = calculator.scoreExplicitPreferences(preferenceMatches);
      List<String> reasons = new ArrayList<>();
      reasons.addAll(suitability.reasons());
      if (!suitability.missing().isEmpty()) reasons.add("Chưa có thông tin để chấm: " + String.join(", ", suitability.missing()));
      List<String> active = query.activeFilterNames(filters);
      if (!active.isEmpty()) reasons.add("Khớp điều kiện đã kiểm tra: " + String.join(", ", active.stream().map(RecommendationService::slotLabel).toList()));
      if (active.isEmpty() && preferenceScore.matchScore() == null) continue;
      if (preferenceScore.matchScore() != null) {
        if (preferenceScore.matchScore() == 0) reasons.add("Không khớp các sở thích mềm có thể xác minh trong dữ liệu sản phẩm");
        preferenceScore.breakdown().forEach((dimension, points) -> {
          if (points > 0 && !dimension.contains(".")) reasons.add("Phù hợp ưu tiên về " + slotLabel(dimension));
        });
      }
      BigDecimal price = effectivePrice(product);
      scored.add(new Scored(product, price, preferenceScore.matchScore(), preferenceScore.breakdown(),
          List.copyOf(reasons), available));
    }

    // Explicit soft-preference evidence ranks first. For a maximum budget,
    // prefer products closest to the user's stated ceiling instead of always
    // returning the three cheapest catalog items. Other unscored searches keep
    // deterministic ascending price order.
    Comparator<BigDecimal> priceTieBreaker = Comparator.naturalOrder();
    if (query.hardBudget != null && Set.of("lt", "lte").contains(query.hardBudget.operator()))
      priceTieBreaker = Comparator.reverseOrder();
    Comparator<BigDecimal> finalPriceTieBreaker = priceTieBreaker;
    var top = scored.stream().sorted(
        Comparator.comparing((Scored x) -> x.score() == null ? -1 : x.score(), Comparator.reverseOrder())
            .thenComparing(Scored::price, finalPriceTieBreaker).thenComparing(x -> x.product().getProductId()))
        .limit(limit).toList();
    var views = catalog.views(top.stream().map(Scored::product).toList());
    var result = new ArrayList<DialogueRecommendationResponse.Item>();
    for (int i = 0; i < top.size(); i++) {
      Scored row = top.get(i);
      ProductResponse original = views.get(i);
      // Keep chat's effective price consistent with a currently published promotion.
      ProductResponse corrected = new ProductResponse(original.id(), original.productName(), original.slug(),
          original.description(), original.price(), original.salePrice(), row.price(),
          original.promotionPublished(), productsWithVariants.contains(row.product().getProductId())
              ? row.availableVariants().stream().filter(v -> query.variantMatchesHard(v, filters)).mapToInt(ProductVariant::getStock).sum()
              : original.stock(), original.thumbnailUrl(), original.status(),
          original.brand(), original.category(), original.specification(), original.images());
      var variantOptions = row.availableVariants().stream()
          .filter(v -> query.variantMatchesHard(v, filters))
          .sorted(Comparator.comparing(ProductVariant::getColor, Comparator.nullsLast(String::compareTo)).thenComparing(ProductVariant::getSize, Comparator.nullsLast(String::compareTo)))
          .limit(20)
          .map(v -> new DialogueRecommendationResponse.VariantOption(v.getColor(), v.getSize(), v.getStock()))
          .toList();
      result.add(new DialogueRecommendationResponse.Item(row.product().getProductId(),
          row.product().getProductName(), row.price(), row.score(), row.breakdown(), row.reasons(),
          variantOptions, corrected));
    }
    return new DialogueRecommendationResponse(request.action(), List.copyOf(result), List.copyOf(warnings));
  }

  private static BigDecimal effectivePrice(Product p) {
    BigDecimal sale = p.getSalePrice();
    if (p.isPromotionPublished() && sale != null && sale.signum() >= 0
        && p.getPrice() != null && sale.compareTo(p.getPrice()) < 0) return sale;
    return p.getPrice();
  }

  /** Use only explicit racket facts in imported product descriptions when the structured spec row is absent. */
  private static RacketSpecification specificationFromDescription(Product product) {
    String description = Objects.toString(product.getProductName(), "") + " "
        + Objects.toString(product.getDescription(), "");
    if (description == null || description.isBlank()) return null;
    String folded = Normalizer.normalize(description, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
    var spec = new RacketSpecification();
    var weight = java.util.regex.Pattern.compile("(?<![a-z0-9])([2-6]u)(?![a-z0-9])").matcher(folded);
    if (weight.find()) spec.setWeightClass(weight.group(1).toUpperCase(Locale.ROOT));
    if (java.util.regex.Pattern.compile("diem can bang|balance point").matcher(folded).find()) {
      var point = java.util.regex.Pattern.compile("(?:diem can bang|balance point)\\s*:?\\s*(\\d{3})").matcher(folded);
      if (point.find()) spec.setBalancePointMm(Integer.parseInt(point.group(1)));
    }
    if (folded.matches("(?s).*(?:nang dau|dau nang|head heavy).*")) spec.setBalanceType(BalanceType.HEAD_HEAVY);
    else if (folded.matches("(?s).*(?:nhe dau|dau nhe|head light).*")) spec.setBalanceType(BalanceType.HEAD_LIGHT);
    else if (folded.matches("(?s).*(?:can bang|even balance).*")) spec.setBalanceType(BalanceType.EVEN_BALANCE);
    if (folded.contains("cung trung binh") || folded.matches("(?s).*do cung\\s*:?\\s*trung binh.*")
        || folded.contains("medium flex"))
      spec.setShaftFlexibility(ShaftFlexibility.MEDIUM);
    else if (folded.contains("kha cung") || folded.matches("(?s).*do cung\\s*:?\\s*cung.*")
        || folded.contains("extra stiff") || folded.matches("(?s).*do cung\\s*:?\\s*stiff.*"))
      spec.setShaftFlexibility(folded.contains("extra stiff") ? ShaftFlexibility.EXTRA_STIFF : ShaftFlexibility.STIFF);
    else if (folded.contains("do deo") || folded.contains("do mem") || folded.contains("flexible"))
      spec.setShaftFlexibility(ShaftFlexibility.FLEXIBLE);
    var tension = java.util.regex.Pattern.compile("(?:suc cang|maximum tension|max tension)\\s*:?\\s*(\\d+(?:[.,]\\d+)?)\\s*(?:lbs?|pounds?)?", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(folded);
    if (tension.find()) spec.setMaxTensionLbs(new BigDecimal(tension.group(1).replace(',', '.')));
    return spec.getWeightClass() == null && spec.getBalanceType() == null && spec.getBalancePointMm() == null
        && spec.getShaftFlexibility() == null && spec.getMaxTensionLbs() == null ? null : spec;
  }

  private static String slotLabel(String key) {
    return switch (key) {
      case "brand" -> "thương hiệu"; case "category" -> "loại sản phẩm";
      case "productName" -> "tên mẫu"; case "budget" -> "ngân sách";
      case "racketWeight" -> "trọng lượng"; case "balance" -> "cân bằng vợt";
      case "shaftFlex" -> "độ cứng thân"; case "skillLevel" -> "trình độ chơi";
      case "playStyle" -> "lối chơi"; case "shoeSize", "apparelSize" -> "kích cỡ";
      case "color" -> "màu sắc"; case "features" -> "tính năng trong mô tả";
      case "capacity" -> "sức chứa"; case "diameter" -> "đường kính dây";
      case "material" -> "chất liệu"; case "gender" -> "kiểu dáng";
      case "stringTension" -> "giới hạn căng dây"; case "footWidth" -> "phom giày";
      default -> "đặc tính đã cung cấp";
    };
  }

  private static boolean matchesCategory(String actual, String requested) {
    String a = normalizeKey(actual), r = normalizeKey(requested);
    return switch (r) {
      case "racket" -> a.contains("vot") || a.contains("racket");
      case "shoes" -> a.contains("giay") || a.contains("shoe");
      case "bags" -> a.contains("tui") || a.contains("bag") || a.contains("baovot") || a.contains("balo");
      case "apparel" -> a.contains("ao") || a.contains("quan") || a.contains("clothing");
      case "strings" -> a.contains("cuoc") || a.contains("string");
      case "shuttlecocks" -> a.contains("quacau") || a.contains("shuttle") || a.contains("ongcau");
      case "accessories" -> a.contains("phukien") || a.contains("quancan") || a.contains("bang") || a.contains("accessor") || a.contains("tat");
      default -> false;
    };
  }

  private static Map<String, List<Object>> normalizeSlots(
      Map<String, List<Object>> supplied, String kind, Set<String> warnings) {
    var result = new LinkedHashMap<String, List<Object>>();
    if (supplied == null) return result;
    if (supplied.size() > 40) {
      warnings.add("Too many " + kind + " fields; ignored");
      return result;
    }
    for (var entry : supplied.entrySet()) {
      String key = entry.getKey();
      if (key == null || !Set.of("brand", "productName", "budget", "category", "skillLevel", "playStyle",
          "racketWeight", "balance", "shaftFlex", "stringTension", "shoeSize", "color", "quantity",
          "apparelSize", "footLength", "footWidth", "wristStrength", "playType", "features", "capacity",
          "diameter", "material", "gender", "frequency", "ownedCategories", "productRef", "priorityCategory",
          "mainPreference", "balancePoint", "availability").contains(key)) {
        warnings.add("Unsupported slot in " + kind + " was ignored");
        continue;
      }
      List<Object> values = entry.getValue();
      if (values == null || values.isEmpty()) continue;
      if (values.size() > 12) {
        warnings.add("Too many values for " + key + " in " + kind);
        continue;
      }
      var safe = new ArrayList<Object>();
      for (Object value : values) {
        if (value instanceof String s && !s.isBlank() && s.length() <= 120) safe.add(s.trim());
        else if (value instanceof Number n && Double.isFinite(n.doubleValue())) safe.add(n);
        else if (value instanceof Map<?, ?> m && m.size() <= 6) safe.add(m);
        else warnings.add("Invalid value for " + key + " in " + kind + " was ignored");
      }
      if (!safe.isEmpty()) result.put(key, List.copyOf(safe));
    }
    return result;
  }

  private static final class DialogueQuery {
    private final Map<String, List<Object>> filters, preferences, exclusions;
    private final Set<String> warnings;
    private Budget hardBudget;
    private Budget excludedBudget;
    private boolean invalidHardQuery;

    DialogueQuery(Map<String, List<Object>> filters, Map<String, List<Object>> preferences,
                  Map<String, List<Object>> exclusions, Set<String> warnings) {
      this.filters = filters; this.preferences = preferences; this.exclusions = exclusions; this.warnings = warnings;
    }

    boolean prepare() {
      hardBudget = parseSingleBudget(filters.get("budget"), true);
      excludedBudget = parseSingleBudget(exclusions.get("budget"), false);
      if (filters.containsKey("budget") && hardBudget == null) invalidHardQuery = true;
      if (has("availability", filters) && filters.get("availability").stream().anyMatch(v ->
          !Set.of("IN_STOCK", "OUT_OF_STOCK").contains(v))) invalidHardQuery = true;
      if (has("balancePoint", filters) && filters.get("balancePoint").stream().anyMatch(v ->
          !(v instanceof Number n) || n.doubleValue() != n.intValue() || n.intValue() < 1)) invalidHardQuery = true;
      // Measurements and user attributes cannot be equated with catalog specs.
      for (String slot : List.of("footLength")) {
        if (has(slot, filters)) {
          warnings.add("Chưa có dữ liệu thuộc tính được xác minh: " + slot + "; không tự suy đoán sản phẩm phù hợp.");
          invalidHardQuery = true;
        }
      }
      if (conflicts(filters, exclusions)) {
        warnings.add("A required filter conflicts with an exclusion; no products returned");
        invalidHardQuery = true;
      }
      // Verify all enum-like filter values before querying to prevent fail-open behavior.
      if (!validateEnums(filters)) invalidHardQuery = true;
      return !invalidHardQuery;
    }

    private boolean validateEnums(Map<String, List<Object>> slots) {
      boolean ok = true;
      for (var e : slots.entrySet()) {
        if (List.of("skillLevel", "playStyle", "racketWeight", "balance", "shaftFlex").contains(e.getKey())) {
          for (Object raw : e.getValue()) {
            if (!(raw instanceof String)) { warnings.add("Invalid enum filter for " + e.getKey()); ok = false; continue; }
            String value = ((String) raw).toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
            boolean valid = switch (e.getKey()) {
              case "skillLevel" -> Set.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "PROFESSIONAL").contains(value);
              case "playStyle" -> Set.of("ATTACK", "DEFENSE", "CONTROL", "ALL_ROUND", "BALANCED", "SPEED").contains(value);
              case "racketWeight" -> value.matches("[2-6]U");
              case "balance" -> Set.of("HEAD_HEAVY", "HEAD_LIGHT", "EVEN_BALANCE").contains(value);
              case "shaftFlex" -> Set.of("FLEXIBLE", "MEDIUM", "STIFF", "EXTRA_STIFF").contains(value);
              default -> false;
            };
            if (!valid) { warnings.add("Unsupported value for hard filter " + e.getKey()); ok = false; }
            if ("skillLevel".equals(e.getKey()) && "PROFESSIONAL".equals(value)) {
              warnings.add("PROFESSIONAL has no matching database enum value"); ok = false;
            }
          }
        }
      }
      return ok;
    }

    boolean matchesHard(Product p, RacketSpecification s, List<ProductVariant> available,
                        Map<String, List<Object>> hard) {
      if (has("category", hard) && !hard.get("category").stream().filter(String.class::isInstance)
          .map(String.class::cast).anyMatch(v -> matchesCategory(p.getCategory().getCategoryName(), v))) return true;
      if (hardBudget != null && !budgetMatches(effectivePrice(p), hardBudget)) return true;
      if (has("availability", hard)) {
        boolean inStock = p.getStatus() == ProductStatus.ACTIVE && p.getStock() != null && p.getStock() > 0;
        if (hard.get("availability").contains("IN_STOCK") != inStock) return true;
      }
      if (has("balancePoint", hard) && (s == null || s.getBalancePointMm() == null
          || hard.get("balancePoint").stream().noneMatch(v -> v instanceof Number n && n.intValue() == s.getBalancePointMm()))) return true;
      if (!stringMatches(p.getBrand().getBrandName(), hard.get("brand"), true)) return true;
      if (!productNameMatches(p.getProductName(), hard.get("productName"))) return true;
      if (has("quantity", hard) && hard.get("quantity").stream().anyMatch(v -> !(v instanceof Number n) || n.intValue() < 1
          || (available.isEmpty() ? p.getStock() == null || p.getStock() < n.intValue()
              : available.stream().noneMatch(row -> variantMatchesHard(row, hard) && row.getStock() >= n.intValue())))) return true;
      if (!descriptionMatches(p, hard)) return true;
      if (has("racketWeight", hard) && (s == null || !enumStringMatches(s.getWeightClass(), hard.get("racketWeight")))) return true;
      if (has("balance", hard) && (s == null || !enumStringMatches(Objects.toString(s.getBalanceType(), null), hard.get("balance")))) return true;
      if (has("shaftFlex", hard) && (s == null || !enumStringMatches(Objects.toString(s.getShaftFlexibility(), null), hard.get("shaftFlex")))) return true;
      if (has("playStyle", hard) && (s == null || !enumStringMatches(Objects.toString(s.getPlayStyle(), null), hard.get("playStyle")))) return true;
      if (has("skillLevel", hard) && (s == null || !enumStringMatches(Objects.toString(s.getSkillLevel(), null), hard.get("skillLevel")))) return true;
      if (has("stringTension", hard)) {
        if (s == null || s.getMaxTensionLbs() == null) {
          warnings.add("Maximum supported string tension is missing in catalog data; candidate excluded");
          return true;
        }
        boolean tension = hard.get("stringTension").stream().anyMatch(v -> tensionSupported(s, v));
        if (!tension) return true;
      }
      if (has("color", hard) || has("shoeSize", hard) || has("apparelSize", hard)) {
        if (available.isEmpty() || available.stream().noneMatch(v -> variantMatchesHard(v, hard))) return true;
      }
      return false;
    }

    boolean matchesExclusion(Product p, RacketSpecification s, Map<String, List<Object>> excluded) {
      if (has("availability", excluded)) {
        boolean inStock = p.getStatus() == ProductStatus.ACTIVE && p.getStock() != null && p.getStock() > 0;
        if (excluded.get("availability").contains(inStock ? "IN_STOCK" : "OUT_OF_STOCK")) return true;
      }
      if (has("category", excluded) && excluded.get("category").stream().anyMatch(v -> matchesCategory(p.getCategory().getCategoryName(), v.toString()))) return true;
      // A missing technical fact cannot prove compliance with a technical exclusion.
      if (s == null && List.of("racketWeight", "balance", "shaftFlex", "balancePoint", "skillLevel", "playStyle")
          .stream().anyMatch(k -> has(k, excluded))) return true;
      if (s != null && (has("racketWeight", excluded) && s.getWeightClass() == null
          || has("balance", excluded) && s.getBalanceType() == null
          || has("shaftFlex", excluded) && s.getShaftFlexibility() == null
          || has("skillLevel", excluded) && s.getSkillLevel() == null
          || has("playStyle", excluded) && s.getPlayStyle() == null)) return true;
      if (has("balancePoint", excluded) && (s == null || s.getBalancePointMm() == null
          || excluded.get("balancePoint").stream().anyMatch(v -> v instanceof Number n && n.intValue() == s.getBalancePointMm()))) return true;
      for (String key : List.of("features", "material", "gender"))
        if (has(key, excluded) && excluded.get(key).stream().anyMatch(v -> descriptionContains(p, v.toString()))) return true;
      if (matchesAnyString(p.getBrand().getBrandName(), excluded.get("brand"), true)) return true;
      if (matchesAnyProductName(p.getProductName(), excluded.get("productName"))) return true;
      if (excludedBudget != null && budgetMatches(effectivePrice(p), excludedBudget)) return true;
      if (s != null && (enumStringMatches(s.getWeightClass(), excluded.get("racketWeight"))
          || enumStringMatches(Objects.toString(s.getBalanceType(), null), excluded.get("balance"))
          || enumStringMatches(Objects.toString(s.getShaftFlexibility(), null), excluded.get("shaftFlex"))
          || enumStringMatches(Objects.toString(s.getPlayStyle(), null), excluded.get("playStyle"))
          || enumStringMatches(Objects.toString(s.getSkillLevel(), null), excluded.get("skillLevel")))) return true;
      return false;
    }

    boolean variantExcluded(ProductVariant variant, Map<String, List<Object>> excluded) {
      return (has("color", excluded) && variantColorMatches(variant.getColor(), excluded.get("color")))
          || (has("apparelSize", excluded) && enumStringMatches(variant.getSize(), excluded.get("apparelSize")))
          || (has("shoeSize", excluded) && variantSizeMatches(variant.getSize(), excluded.get("shoeSize")));
    }

    boolean variantMatchesHard(ProductVariant v, Map<String, List<Object>> hard) {
      if (has("apparelSize", hard) && !enumStringMatches(v.getSize(), hard.get("apparelSize"))) return false;
      if (has("color", hard) && !variantColorMatches(v.getColor(), hard.get("color"))) return false;
      if (has("shoeSize", hard) && !variantSizeMatches(v.getSize(), hard.get("shoeSize"))) return false;
      return true;
    }

    private boolean descriptionMatches(Product p, Map<String, List<Object>> hard) {
      if (has("footWidth", hard) && !hard.get("footWidth").stream().allMatch(v ->
          "WIDE".equals(v) ? descriptionContains(p, "chân bè") || descriptionContains(p, "wide")
              : descriptionContains(p, "chân hẹp") || descriptionContains(p, "narrow"))) {
        warnings.add("Chưa xác minh phom giày theo độ rộng chân trong dữ liệu cửa hàng."); return false;
      }
      for (String key : List.of("capacity", "diameter")) {
        if (!has(key, hard)) continue;
        String source = Normalizer.normalize(Objects.toString(p.getDescription(), ""), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
        var matcher = java.util.regex.Pattern.compile(key.equals("capacity")
            ? "(?:chua|dung)\\s+(?:duoc\\s+)?(\\d+)\\s*(?:cay\\s*)?vot"
            : "(?:duong kinh|diameter)\\s*:?\\s*(\\d+(?:[.,]\\d+)?)\\s*mm").matcher(source);
        boolean matched = false;
        while (matcher.find()) {
          double actual = Double.parseDouble(matcher.group(1).replace(',', '.'));
          if (hard.get(key).stream().allMatch(v -> v instanceof Number n && (key.equals("capacity")
              ? actual >= n.doubleValue() : Math.abs(actual - n.doubleValue()) < 0.001))) matched = true;
        }
        if (!matched) { warnings.add("Chưa xác minh đủ sức chứa hoặc đường kính yêu cầu trong dữ liệu cửa hàng."); return false; }
      }
      for (String key : List.of("features", "material", "gender")) {
        if (has(key, hard) && !hard.get(key).stream().allMatch(v -> descriptionContains(p, v.toString()))) {
          warnings.add("Chưa xác minh đủ đặc tính yêu cầu trong mô tả sản phẩm của cửa hàng.");
          return false;
        }
      }
      return true;
    }

    private boolean descriptionContains(Product p, String value) {
      String source = Normalizer.normalize(Objects.toString(p.getDescription(), "") + " " + p.getProductName(), Normalizer.Form.NFD)
          .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
      String needle = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('đ', 'd');
      return java.util.regex.Pattern.compile("(?<![a-z0-9])" + java.util.regex.Pattern.quote(needle) + "(?![a-z0-9])").matcher(source).find()
          && !source.contains("khong " + needle) && !source.contains("khong co " + needle);
    }

    Map<String, Boolean> preferenceMatches(Product p, RacketSpecification s, List<ProductVariant> available,
                                           Map<String, List<Object>> soft, Set<String> warnings) {
      var match = new LinkedHashMap<String, Boolean>();
      for (String key : List.of("features", "material", "gender"))
        if (has(key, soft)) match.put(key, soft.get(key).stream().allMatch(v -> descriptionContains(p, v.toString())));
      if (s != null && has("wristStrength", soft)) {
        List<Object> wristValues = soft.get("wristStrength");
        if (wristValues.contains("WEAK")) {
          if (s.getWeightClass() != null) match.put("racketWeight", Set.of("4U", "5U", "6U").contains(s.getWeightClass()));
          if (s.getShaftFlexibility() != null) match.put("shaftFlex", s.getShaftFlexibility() == ShaftFlexibility.FLEXIBLE
              || s.getShaftFlexibility() == ShaftFlexibility.MEDIUM);
        }
        if (wristValues.contains("STRONG") && s.getShaftFlexibility() != null)
          match.put("shaftFlex", s.getShaftFlexibility() == ShaftFlexibility.STIFF
              || s.getShaftFlexibility() == ShaftFlexibility.EXTRA_STIFF);
        if (wristValues.stream().anyMatch(v -> !(v instanceof String value)
            || !Set.of("WEAK", "MEDIUM", "STRONG").contains(value.toUpperCase(Locale.ROOT))))
          warnings.add("Unsupported preference value for wristStrength was not scored");
      }
      if (has("brand", soft)) match.put("brand", matchesAnyString(p.getBrand().getBrandName(), soft.get("brand"), true));
      if (has("productName", soft)) match.put("productName", matchesAnyProductName(p.getProductName(), soft.get("productName")));
      if (has("budget", soft)) {
        Budget b = parseSingleBudget(soft.get("budget"), false);
        if (b != null && Set.of("lte", "range").contains(b.operator()))
          match.put("budget", budgetMatches(effectivePrice(p), b));
        else warnings.add("Unsupported soft budget operator was not scored");
      }
      if (s != null) {
        addPreferenceMatch(match, "skillLevel", Objects.toString(s.getSkillLevel(), null), soft, warnings);
        addPreferenceMatch(match, "playStyle", Objects.toString(s.getPlayStyle(), null), soft, warnings);
        addPreferenceMatch(match, "racketWeight", s.getWeightClass(), soft, warnings);
        addPreferenceMatch(match, "balance", Objects.toString(s.getBalanceType(), null), soft, warnings);
        addPreferenceMatch(match, "shaftFlex", Objects.toString(s.getShaftFlexibility(), null), soft, warnings);
      } else if (List.of("skillLevel", "playStyle", "racketWeight", "balance", "shaftFlex", "wristStrength")
          .stream().anyMatch(k -> has(k, soft))) {
        warnings.add("Some racket preference attributes are absent in catalog data; they were not scored");
      }
      if (has("color", soft) && !available.isEmpty()) match.put("color", available.stream().anyMatch(v -> variantColorMatches(v.getColor(), soft.get("color"))));
      if (has("shoeSize", soft) && !available.isEmpty()) match.put("shoeSize", available.stream().anyMatch(v -> variantSizeMatches(v.getSize(), soft.get("shoeSize"))));
      if (has("stringTension", soft) || has("quantity", soft)) warnings.add("String tension or quantity preference has no verified product-match score");
      return match;
    }

    private void addPreferenceMatch(Map<String, Boolean> matches, String slot, String actual,
                                    Map<String, List<Object>> soft, Set<String> warnings) {
      if (!has(slot, soft)) return;
      boolean supported = true;
      for (Object raw : soft.get(slot)) {
        if (!(raw instanceof String value)) { supported = false; break; }
        String v = value.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        boolean valid = switch (slot) {
          case "skillLevel" -> Set.of("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(v);
          case "playStyle" -> Set.of("ATTACK", "DEFENSE", "CONTROL", "ALL_ROUND", "BALANCED", "SPEED").contains(v);
          case "racketWeight" -> v.matches("[2-6]U");
          case "balance" -> Set.of("HEAD_HEAVY", "HEAD_LIGHT", "EVEN_BALANCE").contains(v);
          case "shaftFlex" -> Set.of("FLEXIBLE", "MEDIUM", "STIFF", "EXTRA_STIFF").contains(v);
          default -> false;
        };
        if (!valid) { supported = false; break; }
      }
      if (supported && actual != null) matches.put(slot, enumStringMatches(actual, soft.get(slot)));
      else if (actual == null) warnings.add("Chưa có thông tin: " + slot + "; không cộng điểm.");
      else warnings.add("Unsupported preference value for " + slot + " was not scored");
    }

    List<String> activeFilterNames(Map<String, List<Object>> values) {
      return values.keySet().stream().filter(k -> !Set.of("quantity").contains(k)).toList();
    }

    boolean has(String key, Map<String, List<Object>> map) { return map.containsKey(key) && !map.get(key).isEmpty(); }

    private boolean conflicts(Map<String, List<Object>> required, Map<String, List<Object>> excluded) {
      for (String key : required.keySet()) {
        List<Object> a = required.get(key), b = excluded.get(key);
        if (b != null && a.stream().anyMatch(x -> b.stream().anyMatch(y -> sameValue(key, x, y)))) return true;
      }
      return false;
    }

    private boolean sameValue(String slot, Object a, Object b) {
      if (slot.equals("brand")) return a instanceof String x && b instanceof String y
          && normalizeKey(x).equals(normalizeKey(y));
      if (slot.equals("productName")) return a instanceof String x && b instanceof String y
          && (normalizeKey(x).contains(normalizeKey(y)) || normalizeKey(y).contains(normalizeKey(x)));
      if (slot.equals("color")) return a instanceof String x && b instanceof String y
          && colorAliases(x).stream().map(RecommendationService::normalizeKey)
              .anyMatch(k -> colorAliases(y).stream().map(RecommendationService::normalizeKey).anyMatch(k::equals));
      return Objects.equals(a, b);
    }

    private Budget parseSingleBudget(List<Object> values, boolean hard) {
      if (values == null || values.isEmpty()) return null;
      if (values.size() != 1) { warnings.add("Multiple budget values are ambiguous"); return null; }
      Object raw = values.get(0);
      Object amount = raw; String operator = "eq";
      if (raw instanceof Map<?, ?> m) { amount = m.get("amountVnd"); operator = Objects.toString(m.get("operator"), "eq"); }
      if (!(amount instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() < 0 || n.doubleValue() > 1_000_000_000_000d) {
        warnings.add("Invalid budget value was not applied"); return null;
      }
      if (!Set.of("lt", "lte", "gt", "gte", "eq", "approx", "range").contains(operator)) {
        warnings.add("Unsupported budget operator was not applied"); return null;
      }
      BigDecimal minimum = null;
      if (operator.equals("range")) {
        if (!(raw instanceof Map<?, ?> m) || !(m.get("minVnd") instanceof Number lower)
            || !Double.isFinite(lower.doubleValue()) || lower.doubleValue() < 0 || lower.doubleValue() > n.doubleValue()) {
          warnings.add("Invalid budget range"); return null;
        }
        minimum = new BigDecimal(lower.toString());
      }
      return new Budget(new BigDecimal(n.toString()), operator, minimum);
    }

    private boolean budgetMatches(BigDecimal price, Budget budget) {
      if (price == null) return false;
      int c = price.compareTo(budget.amount());
      // Contract: gt/gte and lt/lte preserve strictness; eq is exact;
      // approx is a one-sided ceiling; range is inclusive at both ends.
      return switch (budget.operator()) {
        case "lt" -> c < 0; case "lte" -> c <= 0; case "gt" -> c > 0;
        case "gte" -> c >= 0; case "eq" -> c == 0; case "approx" -> c <= 0;
        case "range" -> c <= 0 && price.compareTo(budget.minimum()) >= 0; default -> false;
      };
    }

    private boolean tensionSupported(RacketSpecification spec, Object raw) {
      if (!(raw instanceof Map<?, ?> m) || !(m.get("value") instanceof Number n)) return false;
      String unit = Objects.toString(m.get("unit"), "kg").toLowerCase(Locale.ROOT);
      double lbf = n.doubleValue() * (unit.equals("lb") || unit.equals("lbs") ? 1d : 2.2046226218d);
      return spec.getMaxTensionLbs().doubleValue() >= lbf;
    }
  }

  private record Budget(BigDecimal amount, String operator, BigDecimal minimum) {}

  private static boolean stringMatches(String actual, List<Object> values, boolean brand) {
    if (values == null || values.isEmpty()) return true;
    return values.stream().filter(String.class::isInstance).map(String.class::cast)
        .anyMatch(v -> brand ? normalizeKey(actual).equals(normalizeKey(v)) : normalizeKey(actual).contains(normalizeKey(v)));
  }

  private static boolean matchesAnyString(String actual, List<Object> values, boolean brand) {
    return values != null && values.stream().filter(String.class::isInstance).map(String.class::cast)
        .anyMatch(v -> brand ? normalizeKey(actual).equals(normalizeKey(v)) : normalizeKey(actual).contains(normalizeKey(v)));
  }

  private static boolean productNameMatches(String actual, List<Object> values) {
    if (values == null || values.isEmpty()) return true;
    return matchesAnyProductName(actual, values);
  }

  private static boolean matchesAnyProductName(String actual, List<Object> values) {
    if (values == null || values.isEmpty()) return false;
    String product = normalizeKey(actual);
    return values.stream().filter(String.class::isInstance).map(String.class::cast)
        .map(RecommendationService::normalizeKey).filter(v -> !v.isBlank()).anyMatch(product::contains);
  }

  private static boolean enumStringMatches(String actual, List<Object> values) {
    if (actual == null) return false;
    if (values == null || values.isEmpty()) return false;
    String normalizedActual = actual.toUpperCase(Locale.ROOT);
    return values.stream().filter(String.class::isInstance).map(String.class::cast)
        .map(v -> v.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'))
        .map(v -> "ALL_ROUND".equals(v) ? "BALANCED" : v)
        .anyMatch(normalizedActual::equals);
  }

  private static boolean variantColorMatches(String actual, List<Object> values) {
    if (values == null || values.isEmpty()) return false;
    String key = normalizeKey(actual);
    return values.stream().filter(String.class::isInstance).map(String.class::cast)
        .map(v -> colorAliases(v).stream().map(RecommendationService::normalizeKey).toList())
        .anyMatch(aliases -> aliases.contains(key));
  }

  private static Set<String> colorAliases(String value) {
    String v = value.toUpperCase(Locale.ROOT);
    return switch (v) {
      case "BLACK" -> Set.of("black", "đen", "den"); case "WHITE" -> Set.of("white", "trắng", "trang");
      case "RED" -> Set.of("red", "đỏ", "do"); case "BLUE" -> Set.of("blue", "xanh dương", "xanh");
      case "GREEN" -> Set.of("green", "xanh lá"); case "YELLOW" -> Set.of("yellow", "vàng", "vang");
      case "PINK" -> Set.of("pink", "hồng", "hong"); case "PURPLE" -> Set.of("purple", "tím", "tim");
      case "ORANGE" -> Set.of("orange", "cam"); case "NAVY" -> Set.of("navy", "xanh navy");
      default -> Set.of(value);
    };
  }

  private static boolean variantSizeMatches(String actual, List<Object> values) {
    if (values == null || values.isEmpty()) return false;
    Size actualSize = parseSize(actual);
    return values.stream().filter(Map.class::isInstance).map(Map.class::cast).anyMatch(raw -> {
      Object size = raw.get("size"), system = raw.get("system");
      if (!(size instanceof Number n)) return false;
      Size requested = new Size(n.doubleValue(), Objects.toString(system, "UNSPECIFIED").toUpperCase(Locale.ROOT));
      return Math.abs(actualSize.value() - requested.value()) < 0.001
          && ("UNSPECIFIED".equals(requested.system()) || requested.system().equals(actualSize.system()));
    });
  }

  private static Size parseSize(String value) {
    if (value == null) return new Size(Double.NaN, "UNKNOWN");
    var m = java.util.regex.Pattern.compile("(?i)(EU|US|JP|CM)?\\s*(\\d+(?:[.,]\\d+)?)").matcher(value.trim());
    if (!m.matches()) return new Size(Double.NaN, "UNKNOWN");
    String system = m.group(1) == null ? "UNSPECIFIED" : m.group(1).toUpperCase(Locale.ROOT);
    return new Size(Double.parseDouble(m.group(2).replace(',', '.')), system);
  }

  private static String normalizeKey(String value) {
    if (value == null) return "";
    String folded = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return folded.toLowerCase(Locale.ROOT).replace('đ', 'd').replaceAll("[^a-z0-9]", "");
  }

  private record Size(double value, String system) {}
}
