package com.badminton.integration;

import static org.assertj.core.api.Assertions.*;

import com.badminton.brand.entity.Brand;
import com.badminton.brand.repository.BrandRepository;
import com.badminton.category.entity.Category;
import com.badminton.category.repository.CategoryRepository;
import com.badminton.common.dto.PageResponse;
import com.badminton.product.dto.response.ProductResponse;
import com.badminton.product.dto.request.ProductRequest;
import com.badminton.product.dto.request.PromotionRequest;
import com.badminton.product.dto.request.PromotionRequest.DiscountType;
import com.badminton.exception.BusinessException;
import com.badminton.product.entity.*;
import com.badminton.product.repository.ProductRepository;
import com.badminton.product.service.ProductService;
import com.badminton.product.service.PromotionService;
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
class ProductOffersMySqlIntegrationTest {
  @Autowired ProductService service;
  @Autowired PromotionService promotions;
  @Autowired ProductRepository products;
  @Autowired BrandRepository brands;
  @Autowired CategoryRepository categories;
  Brand brand;
  Category category;

  @BeforeEach
  void setup() {
    brand = new Brand();
    brand.setBrandName("Offers " + UUID.randomUUID()); brand.setSlug(UUID.randomUUID().toString());
    brands.saveAndFlush(brand);
    category = new Category();
    category.setCategoryName("Offers " + UUID.randomUUID()); category.setSlug(UUID.randomUUID().toString());
    categories.saveAndFlush(category);
  }

  Product product(String name, String sale, ProductStatus status) {
    var p = new Product();
    p.setProductName(name); p.setSlug(UUID.randomUUID().toString());
    p.setBrand(brand); p.setCategory(category); p.setPrice(new BigDecimal("1000000"));
    p.setSalePrice(sale == null ? null : new BigDecimal(sale));
    p.setPromotionPublished(sale != null);
    p.setStock(status == ProductStatus.OUT_OF_STOCK ? 0 : 3); p.setStatus(status);
    return products.saveAndFlush(p);
  }

  PageResponse<ProductResponse> list(boolean onSale, int page, int size) {
    return service.list(null, null, brand.getBrandId(), null, null, null, null, null,
        null, null, onSale, page, size, "productId,asc");
  }

  @Test
  void filtersActualDiscountsBeforePaginationAndKeepsPublicVisibilityRules() {
    product("Regular", null, ProductStatus.ACTIVE);
    product("Same price", "1000000", ProductStatus.ACTIVE);
    var discounted = product("Discount", "800000", ProductStatus.ACTIVE);
    var zero = product("Zero sale", "0", ProductStatus.ACTIVE);
    var soldOut = product("Sold out", "900000", ProductStatus.OUT_OF_STOCK);
    product("Hidden", "100000", ProductStatus.INACTIVE);
    var draft = product("Unpublished discount", "700000", ProductStatus.ACTIVE);
    draft.setPromotionPublished(false);
    products.flush();
    var first = list(true, 0, 2);
    assertThat(first.totalElements()).isEqualTo(3);
    assertThat(first.totalPages()).isEqualTo(2);
    assertThat(first.content()).extracting(ProductResponse::id)
        .containsExactly(discounted.getProductId(), zero.getProductId());
    assertThat(first.content().get(0).effectivePrice()).isEqualByComparingTo("800000");
    assertThat(list(true, 1, 2).content()).extracting(ProductResponse::id)
        .containsExactly(soldOut.getProductId());
    assertThat(list(false, 0, 100).totalElements()).isEqualTo(6);
  }

  @Test
  void adminPublishesUpdatesAndEndsAnOfferUsingTheAuthoritativePrice() {
    var product = product("Admin offer " + UUID.randomUUID(), null, ProductStatus.ACTIVE);
    assertThat(list(true, 0, 12).content()).isEmpty();
    var published = promotions.publish(product.getProductId(), new PromotionRequest(DiscountType.PERCENT, new BigDecimal("20")));
    assertThat(published.promotionPublished()).isTrue();
    assertThat(published.effectivePrice()).isEqualByComparingTo("800000");
    assertThat(list(true, 0, 12).content()).extracting(ProductResponse::id).containsExactly(product.getProductId());
    var updated = promotions.publish(product.getProductId(), new PromotionRequest(DiscountType.SALE_PRICE, new BigDecimal("750000.50")));
    assertThat(updated.effectivePrice()).isEqualByComparingTo("750000.50");
    var ended = promotions.end(product.getProductId());
    assertThat(ended.promotionPublished()).isFalse();
    assertThat(ended.salePrice()).isNull();
    assertThat(ended.effectivePrice()).isEqualByComparingTo("1000000");
    assertThat(list(true, 0, 12).content()).isEmpty();
    assertThat(promotions.end(product.getProductId()).promotionPublished()).isFalse();
  }

  @Test
  void validatesDiscountAndVisibilityAndRoundsPercentageToCents() {
    var p = product("Validation", null, ProductStatus.ACTIVE);
    for (String value : new String[] {"0", "100.01", "-1"}) {
      assertThatThrownBy(() -> promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.PERCENT, new BigDecimal(value))))
          .isInstanceOf(BusinessException.class);
    }
    for (String value : new String[] {"1000000", "1000001", "-1"}) {
      assertThatThrownBy(() -> promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.SALE_PRICE, new BigDecimal(value))))
          .isInstanceOf(BusinessException.class);
    }
    assertThat(p.isPromotionPublished()).isFalse();
    p.setPrice(new BigDecimal("999.99"));
    assertThat(promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.PERCENT, new BigDecimal("12.34"))).salePrice())
        .isEqualByComparingTo("876.59");
    assertThat(promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.PERCENT, new BigDecimal("100"))).salePrice())
        .isEqualByComparingTo("0");
    p.setStatus(ProductStatus.INACTIVE);
    assertThatThrownBy(() -> promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.PERCENT, new BigDecimal("20"))))
        .isInstanceOf(BusinessException.class);
    p.setStatus(ProductStatus.ACTIVE); p.setPrice(BigDecimal.ZERO);
    assertThatThrownBy(() -> promotions.publish(p.getProductId(), new PromotionRequest(DiscountType.SALE_PRICE, BigDecimal.ZERO)))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void adminCanFindUnpublishedAndInactiveProductsAndFilterPublishedOnes() {
    String keyword = "Admin search " + UUID.randomUUID();
    var draft = product(keyword + " draft", "800000", ProductStatus.ACTIVE);
    draft.setPromotionPublished(false);
    var hidden = product(keyword + " inactive", null, ProductStatus.INACTIVE);
    var live = product(keyword + " live", "700000", ProductStatus.ACTIVE);
    products.flush();
    assertThat(promotions.list(keyword, null, 0, 12).content()).extracting(ProductResponse::id)
        .containsExactly(live.getProductId(), hidden.getProductId(), draft.getProductId());
    assertThat(promotions.list(keyword, true, 0, 12).content()).extracting(ProductResponse::id).containsExactly(live.getProductId());
    assertThat(promotions.list(keyword, false, 0, 12).totalElements()).isEqualTo(2);
  }

  @Test
  void disablingOrRemovingADiscountCannotLeaveAPublishedOffer() {
    var p = product("Disable offer", "800000", ProductStatus.ACTIVE);
    service.disable(p.getProductId());
    assertThat(p.isPromotionPublished()).isFalse();
    assertThat(list(true, 0, 12).content()).isEmpty();
    p.setStatus(ProductStatus.ACTIVE); p.setPromotionPublished(true);
    var saved = service.save(p.getProductId(), new ProductRequest(category.getCategoryId(), brand.getBrandId(),
        p.getProductName(), null, p.getPrice(), null, p.getStock(), ProductStatus.ACTIVE, null));
    assertThat(saved.promotionPublished()).isFalse();
  }
}
