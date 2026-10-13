package com.badminton.integration;

import static org.assertj.core.api.Assertions.*;

import com.badminton.brand.entity.Brand;
import com.badminton.brand.repository.BrandRepository;
import com.badminton.category.entity.Category;
import com.badminton.category.repository.CategoryRepository;
import com.badminton.chatbot.service.*;
import com.badminton.product.entity.*;
import com.badminton.product.repository.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_TESTS", matches = "true")
@Transactional
class ChatbotMySqlIntegrationTest {
  @Autowired ChatbotCatalog catalog;
  @Autowired CategoryRepository categories;
  @Autowired BrandRepository brands;
  @Autowired ProductRepository products;
  @Autowired ProductVariantRepository variants;
  @Autowired RacketSpecificationRepository specs;
  Category category;
  Brand brand;

  @BeforeEach
  void setup() {
    category = new Category();
    category.setCategoryName("Vợt chatbot " + UUID.randomUUID());
    category.setSlug(UUID.randomUUID().toString());
    categories.saveAndFlush(category);
    brand = new Brand();
    brand.setBrandName("Chatbot " + UUID.randomUUID());
    brand.setSlug(UUID.randomUUID().toString());
    brands.saveAndFlush(brand);
  }

  Product product(String name, String price, String salePrice, int stock, ProductStatus status) {
    var p = new Product();
    p.setCategory(category); p.setBrand(brand); p.setProductName(name);
    p.setSlug(UUID.randomUUID().toString()); p.setPrice(new BigDecimal(price));
    p.setSalePrice(salePrice == null ? null : new BigDecimal(salePrice));
    p.setStock(stock); p.setStatus(status); p.setThumbnailUrl("/media/chatbot-test.jpg");
    return products.saveAndFlush(p);
  }

  void variant(Product product, String color, int stock, String status) {
    var v = new ProductVariant();
    v.setProduct(product); v.setColor(color); v.setSize("4U");
    v.setSku(UUID.randomUUID().toString()); v.setStock(stock); v.setStatus(status);
    variants.saveAndFlush(v);
  }

  LocalNlp.Criteria criteria(String color) {
    return new LocalNlp.Criteria("RACKET", BigDecimal.ONE, color, null, null, null);
  }

  @Test
  void filtersAvailabilityAndBudgetUsingEffectivePriceAndLimitsToThree() {
    var sale = product("Sale", "2", "0.10", 3, ProductStatus.ACTIVE);
    var normal = product("Normal", "0.20", null, 1, ProductStatus.ACTIVE);
    var third = product("Third", "0.30", null, 1, ProductStatus.ACTIVE);
    product("Fourth", "0.40", null, 1, ProductStatus.ACTIVE);
    product("Hidden", "0.01", null, 1, ProductStatus.INACTIVE);
    product("Empty", "0.01", null, 0, ProductStatus.OUT_OF_STOCK);
    product("Expensive", "2", null, 1, ProductStatus.ACTIVE);
    var found = catalog.find(criteria(null));
    assertThat(found).extracting(p -> p.id()).containsExactly(sale.getProductId(), normal.getProductId(), third.getProductId());
    assertThat(found.get(0).effectivePrice()).isEqualByComparingTo("0.10");
    assertThat(found.get(0).thumbnailUrl()).isEqualTo("/media/chatbot-test.jpg");
  }

  @Test
  void verifiesVariantAvailabilityAndColorInsteadOfAssumingFromProductName() {
    var valid = product("Valid", "0.10", null, 4, ProductStatus.ACTIVE);
    variant(valid, "Đỏ / White", 4, "ACTIVE");
    var wrongColor = product("Wrong color", "0.20", null, 4, ProductStatus.ACTIVE);
    variant(wrongColor, "Shadow Black", 4, "ACTIVE");
    var inactive = product("Inactive variant", "0.30", null, 4, ProductStatus.ACTIVE);
    variant(inactive, "Red", 4, "INACTIVE");
    var empty = product("Empty variant", "0.40", null, 4, ProductStatus.ACTIVE);
    variant(empty, "Red", 0, "ACTIVE");
    product("Vợt đỏ unverified", "0.50", null, 4, ProductStatus.ACTIVE);
    assertThat(catalog.find(criteria("do"))).extracting(p -> p.id()).containsExactly(valid.getProductId());
    assertThat(catalog.find(criteria(null))).extracting(p -> p.id())
        .doesNotContain(inactive.getProductId(), empty.getProductId());
  }

  @Test
  void usesActualRacketSpecificationsWithoutInventingMatchScores() {
    var matching = product("Beginner 4U", "0.10", null, 2, ProductStatus.ACTIVE);
    product("Unknown specs", "0.20", null, 2, ProductStatus.ACTIVE);
    var spec = new RacketSpecification();
    spec.setProduct(matching); spec.setWeightClass("4U");
    spec.setBalanceType(com.badminton.common.enums.BalanceType.EVEN_BALANCE);
    spec.setShaftFlexibility(com.badminton.common.enums.ShaftFlexibility.MEDIUM);
    spec.setPlayStyle(com.badminton.common.enums.PlayStyle.BALANCED);
    spec.setSkillLevel(com.badminton.common.enums.SkillLevel.BEGINNER);
    specs.saveAndFlush(spec);
    var filter = new LocalNlp.Criteria("RACKET", BigDecimal.ONE, null, "4U", "BALANCED", "BEGINNER");
    assertThat(catalog.find(filter)).extracting(p -> p.id()).containsExactly(matching.getProductId());
  }
}
