package com.badminton.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badminton.brand.entity.*;
import com.badminton.category.entity.*;
import com.badminton.common.enums.*;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.entity.*;
import com.badminton.product.repository.*;
import com.badminton.product.service.ProductService;
import com.badminton.recommendation.dto.request.DialogueRecommendationRequest;
import com.badminton.recommendation.dto.response.DialogueRecommendationResponse;
import com.badminton.recommendation.service.*;
import com.badminton.recommendation.entity.PlayerProfile;
import com.badminton.recommendation.repository.*;
import com.badminton.common.support.Access;
import com.badminton.security.CurrentUser;
import com.badminton.user.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DialogueRecommendationServiceTest {
  private ProductRepository products;
  private RacketSpecificationRepository specs;
  private RecommendationLogRepository logs;
  private ProductService catalog;
  private ProductVariantRepository variants;
  private PlayerProfileRepository profiles;
  private Access access;
  private RecommendationService service;
  private Brand yonex;
  private Category rackets;
  private final List<Product> fixtureProducts = new ArrayList<>();
  private final List<RacketSpecification> fixtureSpecs = new ArrayList<>();
  private final List<ProductVariant> fixtureVariants = new ArrayList<>();

  @BeforeEach
  void setup() {
    profiles = mock(PlayerProfileRepository.class);
    products = mock(ProductRepository.class);
    specs = mock(RacketSpecificationRepository.class);
    logs = mock(RecommendationLogRepository.class);
    catalog = mock(ProductService.class);
    variants = mock(ProductVariantRepository.class);
    access = mock(Access.class);
    yonex = new Brand(); yonex.setBrandId(1L); yonex.setBrandName("Yonex"); yonex.setStatus(BrandStatus.ACTIVE);
    rackets = new Category(); rackets.setCategoryId(1L); rackets.setCategoryName("Vợt"); rackets.setStatus(CategoryStatus.ACTIVE);
    service = new RecommendationService(profiles, products, specs, logs, catalog,
        new MatchScoreCalculator(), access, new ObjectMapper(), variants);
    when(products.findCatalogCandidates(any(), eq(BrandStatus.ACTIVE), eq(CategoryStatus.ACTIVE)))
        .thenAnswer(inv -> fixtureProducts.stream().filter(p -> p.getStatus() == inv.getArgument(0)
            && p.getBrand().getStatus() == BrandStatus.ACTIVE && p.getCategory().getStatus() == CategoryStatus.ACTIVE).toList());
    when(specs.findByProductProductIdIn(anyCollection())).thenAnswer(inv -> fixtureSpecs);
    when(variants.findByProductProductIdIn(anyCollection())).thenAnswer(inv -> fixtureVariants);
    when(catalog.views(anyCollection())).thenAnswer(inv -> {
      var batch = (Collection<Product>) inv.getArgument(0);
      return batch.stream().map(this::view).toList();
    });
  }

  private Product product(long id, String name, String price, ProductStatus status, int stock) {
    Product p = new Product(); p.setProductId(id); p.setProductName(name); p.setSlug("p-" + id);
    p.setBrand(yonex); p.setCategory(rackets); p.setPrice(new BigDecimal(price));
    p.setStock(stock); p.setStatus(status); fixtureProducts.add(p); return p;
  }

  private RacketSpecification spec(Product p, String weight, BalanceType balance,
      ShaftFlexibility flex, PlayStyle style, SkillLevel skill, String tension) {
    var s = new RacketSpecification(); s.setProduct(p); s.setWeightClass(weight); s.setBalanceType(balance);
    s.setShaftFlexibility(flex); s.setPlayStyle(style); s.setSkillLevel(skill);
    s.setMaxTensionLbs(tension == null ? null : new BigDecimal(tension)); fixtureSpecs.add(s); return s;
  }

  private ProductVariant variant(Product p, String color, String size, int stock, String status) {
    var v = new ProductVariant(); v.setProduct(p); v.setColor(color); v.setSize(size); v.setStock(stock); v.setStatus(status);
    fixtureVariants.add(v); return v;
  }

  private ProductResponse view(Product p) {
    var brand = new ProductResponse.NamedReference(yonex.getBrandId(), yonex.getBrandName());
    var category = new ProductResponse.NamedReference(rackets.getCategoryId(), rackets.getCategoryName());
    return new ProductResponse(p.getProductId(), p.getProductName(), p.getSlug(), null, p.getPrice(),
        p.getSalePrice(), p.getPrice(), p.isPromotionPublished(), p.getStock(), null, p.getStatus(),
        brand, category, null, List.of());
  }

  private DialogueRecommendationRequest request(String action, Map<String, List<Object>> filters,
      Map<String, List<Object>> prefs, Map<String, List<Object>> excluded, Integer limit) {
    return new DialogueRecommendationRequest(action, filters, prefs, excluded, limit);
  }

  private Map<String, List<Object>> slots(Object... pairs) {
    var result = new LinkedHashMap<String, List<Object>>();
    for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], (List<Object>) pairs[i + 1]);
    return result;
  }

  @Test
  void profileRecommendationsFallBackToInStockRacketsWhenCatalogSpecsAreMissing() {
    var product = product(601, "Vợt cầu lông không có thông số chuẩn hóa", "700000", ProductStatus.ACTIVE, 5);
    var profile = new PlayerProfile();
    profile.setPreferredWeight("4U");
    profile.setMinBudget(new BigDecimal("500000"));
    profile.setMaxBudget(new BigDecimal("1200000"));
    when(access.current()).thenReturn(new CurrentUser(77L, Role.USER));
    when(profiles.findByUserUserId(77L)).thenReturn(Optional.of(profile));
    when(specs.findAll()).thenReturn(List.of());

    var results = service.recommend(3);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).productId()).isEqualTo(product.getProductId());
    assertThat(results.get(0).matchScore()).isNull();
    assertThat(results.get(0).reasons()).anyMatch(reason -> reason.contains("ngân sách"));
    assertThat(results.get(0).cautions()).anyMatch(caution -> caution.contains("Thiếu thông số kỹ thuật"));
    verifyNoInteractions(logs);
  }

  @Test void filtersByBrandAndEffectiveBudget() {
    var p = product(1, "Astrox 99", "2000000", ProductStatus.ACTIVE, 5); spec(p, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, "30");
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("brand", List.of("YONEX"), "budget", List.of(Map.of("amountVnd", 1_500_000, "operator", "lte"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void profileScoreNeverOverridesHardBudgetAndWeight() {
    var over = product(101, "Fixture over budget", "4000000", ProductStatus.ACTIVE, 5);
    spec(over, "4U", BalanceType.HEAD_LIGHT, ShaftFlexibility.FLEXIBLE, PlayStyle.SPEED, SkillLevel.BEGINNER, "24");
    var valid = product(102, "Fixture valid", "2000000", ProductStatus.ACTIVE, 5);
    spec(valid, "3U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, "30");
    var result = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("category", List.of("racket"),
        "racketWeight", List.of("3U"), "budget", List.of(Map.of("amountVnd", 3000000, "operator", "lt"))),
        slots("playType", List.of("DOUBLES"), "mainPreference", List.of("EASY_HANDLING")), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::productId).containsExactly(102L);
    assertThat(result.results().get(0).matchScore()).isZero();
  }

  @Test void profileRanksVerifiedDoublesAttributesAndReportsMissingData() {
    var heavy = product(101, "Fixture heavy", "2000000", ProductStatus.ACTIVE, 5);
    spec(heavy, "3U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, "30");
    var light = product(102, "Fixture light", "2000000", ProductStatus.ACTIVE, 5);
    spec(light, "4U", BalanceType.HEAD_LIGHT, ShaftFlexibility.MEDIUM, PlayStyle.SPEED, SkillLevel.INTERMEDIATE, "24");
    product(103, "Fixture unknown", "1000000", ProductStatus.ACTIVE, 5);
    var result = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("category", List.of("racket")),
        slots("playType", List.of("DOUBLES")), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::productId).containsExactly(102L, 101L, 103L);
    assertThat(result.results().get(0).reasons()).anyMatch(r -> r.contains("weight=4U"));
    assertThat(result.results().get(2).matchScore()).isNull();
    assertThat(result.results().get(2).reasons()).anyMatch(r -> r.contains("Chưa có thông tin"));
  }

  @Test void balancePointUsesCatalogAndExcludesMissingPoint() {
    var known = product(101, "Fixture known", "2000000", ProductStatus.ACTIVE, 5);
    spec(known, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.MEDIUM, PlayStyle.ATTACK, SkillLevel.INTERMEDIATE, "28").setBalancePointMm(305);
    product(102, "Fixture unknown", "1000000", ProductStatus.ACTIVE, 5);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("balancePoint", List.of(305)), Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::productId).containsExactly(101L);
  }

  @Test void moreExcludesPreviouslyShownIdsAndRefreshesPrices() {
    for (int i = 1; i <= 6; i++) product(i, "Racket " + i, "1000000", ProductStatus.ACTIVE, 4);
    var r = service.recommendForDialogue(new DialogueRecommendationRequest("SEARCH_PRODUCTS",
        slots("brand", List.of("YONEX")), Map.of(), Map.of(), 3, List.of(1L, 2L, 3L), List.of()));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productId).containsExactly(4L, 5L, 6L);
    fixtureProducts.get(3).setPrice(new BigDecimal("500000"));
    fixtureProducts.get(4).setStock(0);
    var fresh = service.recommendForDialogue(new DialogueRecommendationRequest("SEARCH_PRODUCTS",
        slots("brand", List.of("YONEX")), Map.of(), Map.of(), 3, List.of(1L, 2L, 3L), List.of()));
    assertThat(fresh.results()).extracting(DialogueRecommendationResponse.Item::productId).containsExactly(4L, 6L);
    assertThat(fresh.results().get(0).effectivePrice()).isEqualByComparingTo("500000");
  }

  @Test void stockLookupIncludesSoldOutProductsAndVariants() {
    var p = product(1, "Yonex Shoe", "900000", ProductStatus.OUT_OF_STOCK, 0);
    variant(p, "White", "42", 0, "OUT_OF_STOCK");
    var result = service.recommendForDialogue(request("LOOKUP_PRODUCTS", slots("productName", List.of("Yonex Shoe"),
        "shoeSize", List.of(Map.of("size", 42, "system", "UNSPECIFIED"))), Map.of(), Map.of(), 3));
    assertThat(result.results()).hasSize(1);
    assertThat(result.results().get(0).product().stock()).isZero();
    assertThat(result.results().get(0).availableVariants().get(0).stock()).isZero();
  }

  @Test void quantityRequiresEnoughStockInOneMatchingVariant() {
    var p = product(1, "Shoe", "900000", ProductStatus.ACTIVE, 20);
    variant(p, "White", "42", 1, "ACTIVE"); variant(p, "White", "43", 10, "ACTIVE");
    var filters = slots("productName", List.of("Shoe"), "quantity", List.of(3),
        "shoeSize", List.of(Map.of("size", 42, "system", "UNSPECIFIED")));
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", filters, Map.of(), Map.of(), 3)).results()).isEmpty();
    fixtureVariants.get(0).setStock(3);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS", filters, Map.of(), Map.of(), 3));
    assertThat(result.results()).hasSize(1);
    assertThat(result.results().get(0).product().stock()).isEqualTo(3);
  }

  @Test void apparelSizeMustMatchTheSameColorVariant() {
    var p = product(1, "Áo", "200000", ProductStatus.ACTIVE, 6);
    variant(p, "Black", "M", 3, "ACTIVE"); variant(p, "White", "L", 3, "ACTIVE");
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("apparelSize", List.of("L"),
        "color", List.of("BLACK")), Map.of(), Map.of(), 3)).results()).isEmpty();
  }

  @Test void missingFootSizeChartDoesNotInventConversion() {
    product(1, "Shoe", "900000", ProductStatus.ACTIVE, 20);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("footLength", List.of(26)), Map.of(), Map.of(), 3));
    assertThat(result.results()).isEmpty();
    assertThat(result.warnings()).anyMatch(w -> w.contains("footLength"));
  }

  @Test void bagCapacityAndFeaturesNeedCatalogEvidence() {
    var p = product(1, "Balo", "500000", ProductStatus.ACTIVE, 3);
    var filters = slots("capacity", List.of(6), "features", List.of("ngăn giày riêng"));
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", filters, Map.of(), Map.of(), 3)).results()).isEmpty();
    p.setDescription("Chứa được 6 cây vợt, có ngăn giày riêng.");
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", filters, Map.of(), Map.of(), 3)).results()).hasSize(1);
    p.setDescription("Chứa được 6 cây vợt, không có ngăn giày riêng.");
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", filters, Map.of(), Map.of(), 3)).results()).isEmpty();
  }

  @Test void filtersByModelNameCaseAndPunctuationInsensitively() {
    var p = product(1, "Yonex Astrox-99", "2000000", ProductStatus.ACTIVE, 5); spec(p, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, null);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("productName", List.of("ASTROX 99")), Map.of(), Map.of(), 3));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productName).containsExactly("Yonex Astrox-99");
  }

  @Test void filtersByRacketWeightBalanceFlexStyleAndSkill() {
    var p = racket(1, "Racket 4U", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("racketWeight", List.of("4U"),
        "balance", List.of("HEAD_HEAVY"), "shaftFlex", List.of("STIFF"), "playStyle", List.of("ATTACK"), "skillLevel", List.of("ADVANCED")), Map.of(), Map.of(), 3));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productName).containsExactly("Racket 4U");
  }

  @Test void mapsAllRoundToDatabaseBalancedEnum() {
    racket(1, "Balanced racket", "4U", BalanceType.EVEN_BALANCE, ShaftFlexibility.MEDIUM, PlayStyle.BALANCED, SkillLevel.BEGINNER, 4);
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("playStyle", List.of("ALL_ROUND")), Map.of(), Map.of(), 3)).results()).hasSize(1);
  }

  @Test void preferencesRankExactMatchesWithBreakdown() {
    var a = racket(1, "Attack 4U", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    var b = racket(2, "Defense 5U", "5U", BalanceType.HEAD_LIGHT, ShaftFlexibility.FLEXIBLE, PlayStyle.DEFENSE, SkillLevel.BEGINNER, 4);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", Map.of(),
        slots("racketWeight", List.of("4U"), "playStyle", List.of("ATTACK")), Map.of(), 3));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productName).containsExactly("Attack 4U", "Defense 5U");
    assertThat(r.results().get(0).matchScore()).isEqualTo(30);
    assertThat(r.results().get(0).scoreBreakdown()).containsEntry("racketWeight", 10).containsEntry("playStyle", 10)
        .containsEntry("playStyle.ATTACK.balance", 10);
  }

  @Test void maximumBudgetRanksClosestAffordableProductsFirst() {
    product(1, "Entry racket", "499000", ProductStatus.ACTIVE, 4);
    product(2, "Midrange racket", "2500000", ProductStatus.ACTIVE, 4);
    product(3, "Near budget racket", "4900000", ProductStatus.ACTIVE, 4);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS",
        slots("category", List.of("racket"), "budget", List.of(Map.of("amountVnd", 5_000_000, "operator", "lte"))),
        Map.of(), Map.of(), 3));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("Near budget racket", "Midrange racket", "Entry racket");
  }

  @Test void strongWristPreferenceRanksStifferRacketsFirst() {
    racket(1, "Flexible racket", "4U", BalanceType.HEAD_LIGHT, ShaftFlexibility.FLEXIBLE,
        PlayStyle.DEFENSE, SkillLevel.BEGINNER, 4);
    racket(2, "Stiff racket", "3U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF,
        PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("category", List.of("racket")),
        slots("wristStrength", List.of("STRONG")), Map.of(), 3));
    assertThat(r.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("Stiff racket", "Flexible racket");
    assertThat(r.results().get(0).scoreBreakdown()).containsEntry("shaftFlex", 10);
    assertThat(r.results().get(1).scoreBreakdown()).containsEntry("shaftFlex", 0);
  }

  @Test void missingRacketSpecsWarnWhenWristPreferenceCannotBeApplied() {
    product(1, "Racket without specification", "1000000", ProductStatus.ACTIVE, 4);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("category", List.of("racket")),
        slots("wristStrength", List.of("STRONG")), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.warnings()).anyMatch(w -> w.contains("preference attributes are absent"));
  }

  @Test void noPreferenceMeansNoMatchScore() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("brand", List.of("YONEX")), Map.of(), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.results().get(0).matchScore()).isNull();
    assertThat(r.results().get(0).scoreBreakdown()).isEmpty();
  }

  @Test void exclusionsRemoveMatchingBrand() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", Map.of(), Map.of(), slots("brand", List.of("Yonex")), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void exclusionKeepsProductWhenAnotherColorVariantIsAvailable() {
    var p = racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    variant(p, "Red", "G5", 3, "ACTIVE"); variant(p, "Blue", "G5", 2, "ACTIVE");
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("brand", List.of("YONEX")), Map.of(), slots("color", List.of("RED")), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.results().get(0).availableVariants()).extracting(DialogueRecommendationResponse.VariantOption::color).containsExactly("Blue");
  }

  @Test void hardColorFilterRequiresAvailableVariant() {
    var p = racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    variant(p, "Xanh dương", "G5", 1, "ACTIVE"); variant(p, "Red", "G5", 0, "OUT_OF_STOCK");
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("color", List.of("BLUE")), Map.of(), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.results().get(0).availableVariants()).extracting(DialogueRecommendationResponse.VariantOption::color).containsExactly("Xanh dương");
  }

  @Test void shoeSizeRequiresAnInStockVariantWithSameSizingSystem() {
    var p = product(1, "Shoe", "1500000", ProductStatus.ACTIVE, 5); variant(p, "Black", "EU 42", 2, "ACTIVE");
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("shoeSize", List.of(Map.of("size", 42, "system", "EU"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
  }

  @Test void excludesSoldOutProducts() {
    var p = racket(1, "Sold out", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 0);
    p.setStock(0);
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", Map.of(), Map.of(), Map.of(), 3)).results()).isEmpty();
  }

  @Test void excludesProductWithOnlyInactiveOrEmptyVariants() {
    var p = racket(1, "No active variants", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 3);
    variant(p, "Black", "G5", 0, "OUT_OF_STOCK");
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", Map.of(), Map.of(), Map.of(), 3)).results()).isEmpty();
  }

  @Test void unpublishedSalePriceDoesNotBecomeEffectivePrice() {
    var p = racket(1, "Racket", "2000000", "500000", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("budget", List.of(Map.of("amountVnd", 1_000_000, "operator", "lte"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void publishedValidSalePriceIsUsed() {
    var p = racket(1, "Racket", "2000000", "900000", true, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("budget", List.of(Map.of("amountVnd", 1_000_000, "operator", "lte"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.results().get(0).effectivePrice()).isEqualByComparingTo("900000");
    assertThat(r.results().get(0).product().effectivePrice()).isEqualByComparingTo("900000");
  }

  @Test void inactiveProductNeverAppearsEvenIfRepositoryReturnsIt() {
    var p = racket(1, "Inactive", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4);
    p.setStatus(ProductStatus.INACTIVE);
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", Map.of(), Map.of(), Map.of(), 3)).results()).isEmpty();
  }

  @Test void missingRacketSpecDoesNotSatisfyHardRacketFilter() {
    product(1, "No specification", "1000000", ProductStatus.ACTIVE, 2);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("racketWeight", List.of("4U")), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void explicitRacketFactsInImportedDescriptionCanSatisfyHardFilters() {
    var p = product(1, "Imported racket", "1500000", ProductStatus.ACTIVE, 2);
    p.setDescription("Trọng lượng: 4U (82+-2gr)\nĐiểm cân bằng: 305 ± 3mm - Nặng đầu\nĐộ cứng: Cứng");
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots(
        "category", List.of("racket"), "racketWeight", List.of("4U"),
        "balance", List.of("HEAD_HEAVY"), "shaftFlex", List.of("STIFF")), Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("Imported racket");
  }

  @Test void missingDescriptionFactsStillCannotSatisfyHardRacketFilters() {
    var p = product(1, "Unknown racket", "1500000", ProductStatus.ACTIVE, 2);
    p.setDescription("Graphite frame");
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots(
        "category", List.of("racket"), "racketWeight", List.of("4U")), Map.of(), Map.of(), 3));
    assertThat(result.results()).isEmpty();
  }

  @Test void missingSoftAttributeDoesNotGetAScore() {
    var p = product(1, "Shoe", "1000000", ProductStatus.ACTIVE, 2); variant(p, "Black", "EU 42", 2, "ACTIVE");
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", Map.of(),
        slots("skillLevel", List.of("ADVANCED"), "shoeSize", List.of(Map.of("size", 42, "system", "EU"))), Map.of(), 3));
    assertThat(r.results()).hasSize(1);
    assertThat(r.results().get(0).matchScore()).isEqualTo(10);
    assertThat(r.results().get(0).scoreBreakdown()).containsOnlyKeys("shoeSize");
  }

  @Test void conflictingRequiredAndExcludedBrandFailsClosed() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("brand", List.of("Yonex")), Map.of(), slots("brand", List.of("YONEX")), 3));
    assertThat(r.results()).isEmpty();
    assertThat(r.warnings()).anyMatch(w -> w.contains("conflicts"));
  }

  @Test void approximateBudgetIsACeilingWithoutUnrequestedOverspend() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("budget", List.of(Map.of("amountVnd", 1000000, "operator", "approx"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
    var affordable = product(2, "Affordable", "900000", ProductStatus.ACTIVE, 5);
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("budget", List.of(Map.of("amountVnd", 1000000, "operator", "approx"))), Map.of(), Map.of(), 3)).results())
        .extracting(DialogueRecommendationResponse.Item::productName).containsExactly("Affordable");
  }

  @Test void strictGreaterThanBudgetFiltersBeforeTheThreeResultLimit() {
    racket(1, "Below threshold", "499000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(2, "At threshold", "5000000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(3, "Above threshold", "5000001", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("category", List.of("racket"), "budget", List.of(Map.of("amountVnd", 5_000_000, "operator", "gt"))),
        Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::effectivePrice)
        .containsExactly(new BigDecimal("5000001"));
  }

  @Test void equalityBudgetMeansExactEffectivePrice() {
    racket(1, "Under exact price", "499000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(2, "Exact price", "5000000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(3, "Over exact price", "5000001", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("budget", List.of(Map.of("amountVnd", 5_000_000, "operator", "eq"))), Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::effectivePrice)
        .containsExactly(new BigDecimal("5000000"));
  }

  @Test void budgetRangeIncludesBothEndpoints() {
    racket(1, "Below range", "1999999", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(2, "Lower endpoint", "2000000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(3, "Upper endpoint", "5000000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(4, "Above range", "5000001", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("budget", List.of(Map.of("amountVnd", 5_000_000, "operator", "range", "minVnd", 2_000_000))),
        Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::effectivePrice)
        .containsExactly(new BigDecimal("2000000"), new BigDecimal("5000000"));
  }

  @Test void greaterThanOrEqualAndLessThanKeepTheirBoundarySemantics() {
    racket(1, "Below", "1999999", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(2, "At", "2000000", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    racket(3, "Above", "2000001", "0", false, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var atLeast = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("budget", List.of(Map.of("amountVnd", 2_000_000, "operator", "gte"))), Map.of(), Map.of(), 3));
    assertThat(atLeast.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("At", "Above");
    var strictlyBelow = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("budget", List.of(Map.of("amountVnd", 2_000_000, "operator", "lt"))), Map.of(), Map.of(), 3));
    assertThat(strictlyBelow.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("Below");
  }

  @Test void racketCategoryExcludesShoesAndAccessories() {
    racket(1, "Racket result", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var shoe = product(2, "Shoe result", "1000000", ProductStatus.ACTIVE, 2);
    var shoes = new Category(); shoes.setCategoryId(2L); shoes.setCategoryName("Giày cầu lông"); shoes.setStatus(CategoryStatus.ACTIVE);
    shoe.setCategory(shoes);
    var accessory = product(3, "Accessory result", "500000", ProductStatus.ACTIVE, 2);
    var accessories = new Category(); accessories.setCategoryId(3L); accessories.setCategoryName("Phụ kiện"); accessories.setStatus(CategoryStatus.ACTIVE);
    accessory.setCategory(accessories);

    var result = service.recommendForDialogue(request("SEARCH_PRODUCTS",
        slots("category", List.of("racket")), Map.of(), Map.of(), 3));
    assertThat(result.results()).extracting(DialogueRecommendationResponse.Item::productName)
        .containsExactly("Racket result");
  }

  @Test void professionalFilterUnsupportedByDatabaseFailsClosed() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("skillLevel", List.of("PROFESSIONAL")), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
    assertThat(r.warnings()).anyMatch(w -> w.contains("PROFESSIONAL"));
  }

  @Test void missingTensionAttributeDoesNotSatisfyHardFilter() {
    var p = product(1, "Racket", "2000000", ProductStatus.ACTIVE, 2);
    spec(p, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, null);
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("stringTension", List.of(Map.of("value", 11, "unit", "kg"))), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
    assertThat(r.warnings()).anyMatch(w -> w.contains("missing in catalog data"));
  }

  @Test void supportedStringTensionUsesMaximumSupportedTension() {
    racket(1, "High tension racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 4, "28");
    assertThat(service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("stringTension", List.of(Map.of("value", 11, "unit", "kg"))), Map.of(), Map.of(), 3)).results()).hasSize(1);
  }

  @Test void noProductsFoundReturnsEmptyResults() {
    var r = service.recommendForDialogue(request("SEARCH_PRODUCTS", Map.of(), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void emptyCriteriaDoesNotReturnArbitraryProducts() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 2);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", Map.of(), Map.of(), Map.of(), 3));
    assertThat(r.results()).isEmpty();
    assertThat(r.warnings()).anyMatch(w -> w.contains("No explicit"));
  }

  @Test void preferenceWithoutComparableProductDataDoesNotReturnUnexplainedItem() {
    var p = product(1, "Shoe without sizes", "1000000", ProductStatus.ACTIVE, 2);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", Map.of(),
        slots("shoeSize", List.of(Map.of("size", 42, "system", "EU"))), Map.of(), 3));
    assertThat(r.results()).isEmpty();
  }

  @Test void neverReturnsMoreThanThreeProducts() {
    for (int i = 1; i <= 6; i++) racket(i, "Racket " + i, "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, i);
    var r = service.recommendForDialogue(request("RECOMMEND_PRODUCTS", slots("brand", List.of("YONEX")), Map.of(), Map.of(), 50));
    assertThat(r.results()).hasSize(3);
  }

  @Test void unsafeActionDoesNotQueryCatalogOrPersistLogs() {
    var r = service.recommendForDialogue(request("DELETE_PRODUCTS", Map.of(), Map.of(), Map.of(), 3));
    assertThat(r.action()).isEqualTo("SAFE_RESPONSE");
    verifyNoInteractions(products, specs, variants, logs);
  }

  @Test void doesNotWriteRecommendationLogForDialogue() {
    racket(1, "Racket", "4U", BalanceType.HEAD_HEAVY, ShaftFlexibility.STIFF, PlayStyle.ATTACK, SkillLevel.ADVANCED, 1);
    service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("brand", List.of("YONEX")), Map.of(), Map.of(), 3));
    verifyNoInteractions(logs);
  }

  @Test void candidateQueryRequiresActiveProductBrandAndCategory() {
    service.recommendForDialogue(request("SEARCH_PRODUCTS", slots("brand", List.of("YONEX")), Map.of(), Map.of(), 3));
    verify(products).findCatalogCandidates(ProductStatus.ACTIVE, BrandStatus.ACTIVE, CategoryStatus.ACTIVE);
  }

  private Product racket(long id, String name, String weight, BalanceType balance, ShaftFlexibility flex,
                         PlayStyle style, SkillLevel skill, int stock) {
    Product p = product(id, name, "2000000", ProductStatus.ACTIVE, stock);
    spec(p, weight, balance, flex, style, skill, "28"); return p;
  }

  private Product racket(long id, String name, String weight, BalanceType balance, ShaftFlexibility flex,
                         PlayStyle style, SkillLevel skill, int stock, String tension) {
    Product p = product(id, name, "2000000", ProductStatus.ACTIVE, stock);
    spec(p, weight, balance, flex, style, skill, tension); return p;
  }

  private Product racket(long id, String name, String price, String sale, boolean published,
      String weight, BalanceType balance, ShaftFlexibility flex, PlayStyle style, SkillLevel skill, int stock) {
    Product p = product(id, name, price, ProductStatus.ACTIVE, stock);
    p.setSalePrice(new BigDecimal(sale)); p.setPromotionPublished(published);
    spec(p, weight, balance, flex, style, skill, "28"); return p;
  }
}
