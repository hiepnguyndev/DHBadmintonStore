package com.badminton.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommerceMySqlIntegrationTest {
  @Autowired com.badminton.marketorder.service.MarketOrderService market;
  @Autowired com.badminton.user.repository.UserRepository users;
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired com.badminton.marketorder.repository.MarketplaceOrderRepository marketOrders;
  String admin, buyer, seller, other, inspector;
  String categoryName = "Vợt IT " + UUID.randomUUID(), brandName = "Test Brand " + UUID.randomUUID();
  long buyerId, sellerId, address, otherAddress, category, brand;

  JsonNode call(String token, String method, String path, Object body, int expected)
      throws Exception {
    var req =
        MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path);
    if (token != null) req.header("Authorization", "Bearer " + token);
    if (body != null) req.contentType("application/json").content(json.writeValueAsString(body));
    var res = mvc.perform(req).andReturn().getResponse();
    assertThat(res.getStatus())
        .as(method + " " + path + " " + res.getContentAsString())
        .isEqualTo(expected);
    return json.readTree(res.getContentAsString()).path("data");
  }

  String account(String role) throws Exception {
    String email = "test-" + UUID.randomUUID() + "@example.com";
    call(
        null,
        "POST",
        "/api/auth/register",
        Map.of(
            "fullName",
            role + " User",
            "email",
            email,
            "password",
            "Password123!",
            "confirmPassword",
            "Password123!"),
        201);
    jdbc.update("update users set role=? where email=?", role, email);
    return call(
            null,
            "POST",
            "/api/auth/login",
            Map.of("email", email, "password", "Password123!"),
            200)
        .path("accessToken")
        .asText();
  }

  long address(String token) throws Exception {
    return call(
            token,
            "POST",
            "/api/addresses",
            Map.of(
                "receiverName",
                "Buyer",
                "receiverPhone",
                "0901234567",
                "province",
                "HN",
                "ward",
                "ML",
                "detailAddress",
                "123 ABC",
                "isDefault",
                true),
            201)
        .path("addressId")
        .asLong();
  }

  @BeforeAll
  void setup() throws Exception {
    admin = account("ADMIN");
    buyer = account("USER");
    seller = account("USER");
    other = account("USER");
    inspector = account("INSPECTOR");
    buyerId = call(buyer, "GET", "/api/users/me", null, 200).path("id").asLong();
    sellerId = call(seller, "GET", "/api/users/me", null, 200).path("id").asLong();
    address = address(buyer);
    otherAddress = address(other);
    category =
        call(
                admin,
                "POST",
                "/api/categories",
                Map.of("categoryName", categoryName, "description", "Racket"),
                201)
            .path("categoryId")
            .asLong();
    brand =
        call(
                admin,
                "POST",
                "/api/brands",
                Map.of("brandName", brandName, "description", "Test"),
                201)
            .path("brandId")
            .asLong();
  }

  long product(int stock) throws Exception {
    return call(
            admin,
            "POST",
            "/api/products",
            Map.of(
                "categoryId",
                category,
                "brandId",
                brand,
                "productName",
                "Racket " + UUID.randomUUID(),
                "description",
                "Integration fixture",
                "price",
                1000000,
                "stock",
                stock,
                "status",
                "ACTIVE",
                "specification",
                Map.of(
                    "weightClass",
                    "4U",
                    "balanceType",
                    "EVEN_BALANCE",
                    "shaftFlexibility",
                    "MEDIUM",
                    "playStyle",
                    "BALANCED",
                    "skillLevel",
                    "INTERMEDIATE")),
            201)
        .path("id")
        .asLong();
  }

  Map<String, Object> listingBody() {
    return new HashMap<>(
        Map.of(
            "categoryId",
            category,
            "brandId",
            brand,
            "productName",
            "Used Racket " + UUID.randomUUID(),
            "model",
            "Test",
            "description",
            "Test condition",
            "originalPrice",
            2000000,
            "sellingPrice",
            1000000,
            "usageMonths",
            6,
            "conditionGrade",
            "A",
            "warrantyStatus",
            "EXPIRED"));
  }

  long listing() throws Exception {
    long id =
        call(seller, "POST", "/api/marketplace/listings", listingBody(), 201)
            .path("listing")
            .path("listingId")
            .asLong();
    var buffer = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", buffer);
    var file = new MockMultipartFile("file", "image.png", "image/png", buffer.toByteArray());
    assertThat(
            mvc.perform(
                    multipart("/api/marketplace/listings/" + id + "/images")
                        .file(file)
                        .header("Authorization", "Bearer " + seller))
                .andReturn()
                .getResponse()
                .getStatus())
        .isEqualTo(200);
    call(seller, "POST", "/api/marketplace/listings/" + id + "/submit", null, 200);
    call(admin, "POST", "/api/admin/marketplace/listings/" + id + "/approve", null, 200);
    return id;
  }

  long purchase(long listingId) throws Exception {
    return call(
            buyer,
            "POST",
            "/api/market-orders",
            Map.of(
                "listingId", listingId, "addressId", address, "paymentMethod", "ESCROW_SIMULATION"),
            200)
        .path("order")
        .path("marketOrderId")
        .asLong();
  }

  long shipped(long order) throws Exception {
    call(buyer, "POST", "/api/escrow/" + order + "/pay", null, 200);
    return call(
            seller,
            "POST",
            "/api/shipments",
            Map.of(
                "marketOrderId",
                order,
                "carrier",
                "TEST",
                "trackingCode",
                UUID.randomUUID().toString()),
            200)
        .path("shipmentId")
        .asLong();
  }

  void delivered(long shipment) throws Exception {
    for (String state : List.of("PICKED_UP", "IN_TRANSIT", "DELIVERED"))
      call(
          admin,
          "PATCH",
          "/api/shipments/" + shipment + "/status",
          Map.of("shippingStatus", state),
          200);
  }

  @Test
  void catalogAndRecommendationRejectForgedAuthority() throws Exception {
    long p = product(3);
    call(null, "GET", "/api/products/" + p, null, 200);
    call(
        buyer,
        "POST",
        "/api/products",
        Map.of(
            "categoryId",
            category,
            "brandId",
            brand,
            "productName",
            "Forbidden",
            "price",
            1,
            "stock",
            1,
            "status",
            "ACTIVE"),
        403);
    call(buyer, "POST", "/api/cart/items", Map.of("productId", p, "quantity", 1, "price", 1), 400);
    var profile =
        Map.of(
            "skillLevel",
            "INTERMEDIATE",
            "playStyle",
            "BALANCED",
            "playType",
            "BOTH",
            "wristStrength",
            "MEDIUM",
            "preferredWeight",
            "4U",
            "minBudget",
            500000,
            "maxBudget",
            2000000);
    call(buyer, "POST", "/api/player-profile", profile, 201);
    var result = call(buyer, "GET", "/api/recommendations", null, 200);
    assertThat(result.isArray()).isTrue();
    assertThat(result.get(0).path("matchScore").asInt()).isEqualTo(100);
    for (JsonNode node : result) assertThat(node.path("matchScore").asInt()).isBetween(0, 100);
  }

  @Test
  void shopCancelRestoresStockOnceAndChecksOwner() throws Exception {
    long p = product(5);
    call(buyer, "POST", "/api/cart/items", Map.of("productId", p, "quantity", 2), 200);
    var order =
        call(
            buyer,
            "POST",
            "/api/orders",
            Map.of("addressId", address, "paymentMethod", "COD"),
            201);
    long id = order.path("order").path("orderId").asLong();
    assertThat(order.path("order").path("totalAmount").decimalValue())
        .isEqualByComparingTo("2000000");
    assertThat(call(null, "GET", "/api/products/" + p, null, 200).path("stock").asInt())
        .isEqualTo(3);
    call(other, "GET", "/api/orders/" + id, null, 403);
    call(buyer, "POST", "/api/orders/" + id + "/cancel", null, 200);
    call(buyer, "POST", "/api/orders/" + id + "/cancel", null, 409);
    assertThat(call(null, "GET", "/api/products/" + p, null, 200).path("stock").asInt())
        .isEqualTo(5);
  }

  @Test
  void marketplaceCompleteAndDoubleActions() throws Exception {
    long l = listing(), o = purchase(l);
    call(other, "GET", "/api/market-orders/" + o, null, 403);
    call(other, "POST", "/api/escrow/" + o + "/pay", null, 403);
    long s = shipped(o);
    call(buyer, "POST", "/api/escrow/" + o + "/pay", null, 409);
    call(buyer, "POST", "/api/market-orders/" + o + "/confirm-received", null, 409);
    delivered(s);
    var end = call(buyer, "POST", "/api/market-orders/" + o + "/confirm-received", null, 200);
    assertThat(end.path("escrow").path("status").asText()).isEqualTo("RELEASED");
    call(buyer, "POST", "/api/market-orders/" + o + "/confirm-received", null, 409);
    assertThat(
            call(null, "GET", "/api/marketplace/listings/" + l, null, 200)
                .path("listing")
                .path("status")
                .asText())
        .isEqualTo("SOLD");
    call(buyer, "POST", "/api/seller-ratings", Map.of("marketOrderId", o, "rating", 5), 200);
    call(buyer, "POST", "/api/seller-ratings", Map.of("marketOrderId", o, "rating", 5), 409);
  }

  @Test
  void disputesFreezeAndResolveEveryMvpOutcome() throws Exception {
    for (String resolution : List.of("FULL_REFUND", "RELEASE_TO_SELLER", "REJECT_DISPUTE")) {
      long l = listing(), o = purchase(l);
      shipped(o);
      long d =
          call(
                  buyer,
                  "POST",
                  "/api/disputes",
                  Map.of(
                      "marketOrderId",
                      o,
                      "reason",
                      "NOT_RECEIVED",
                      "description",
                      "Chưa nhận hàng"),
                  200)
              .path("disputeId")
              .asLong();
      call(buyer, "POST", "/api/market-orders/" + o + "/confirm-received", null, 409);
      call(other, "GET", "/api/disputes/" + d, null, 403);
      call(
          admin,
          "POST",
          "/api/admin/disputes/" + d + "/resolve",
          Map.of("resolution", "PARTIAL_REFUND", "resolutionNote", "Post MVP"),
          400);
      call(
          admin,
          "POST",
          "/api/admin/disputes/" + d + "/resolve",
          Map.of("resolution", resolution, "resolutionNote", "Đã kiểm tra bằng chứng"),
          200);
      call(
          admin,
          "POST",
          "/api/admin/disputes/" + d + "/resolve",
          Map.of("resolution", resolution, "resolutionNote", "Repeat"),
          409);
      var end = call(buyer, "GET", "/api/market-orders/" + o, null, 200);
      assertThat(end.path("order").path("orderStatus").asText())
          .isEqualTo(resolution.equals("FULL_REFUND") ? "REFUNDED" : "COMPLETED");
      assertThat(
              jdbc.queryForObject(
                  "select status from used_listings where listing_id=?", String.class, l))
          .isEqualTo(resolution.equals("FULL_REFUND") ? "HIDDEN" : "SOLD");
    }
  }

  @Test
  void concurrentBuyOnlyOneWins() throws Exception {
    long l = listing();
    var pool = Executors.newFixedThreadPool(2);
    var ready = new CountDownLatch(2);
    var go = new CountDownLatch(1);
    try {
      var futures = new ArrayList<Future<Integer>>();
      for (String t : List.of(buyer, other))
        futures.add(
            pool.submit(
                () -> {
                  ready.countDown();
                  go.await();
                  return mvc.perform(
                          post("/api/market-orders")
                              .header("Authorization", "Bearer " + t)
                              .contentType("application/json")
                              .content(
                                  json.writeValueAsString(
                                      Map.of(
                                          "listingId",
                                          l,
                                          "addressId",
                                          t.equals(buyer) ? address : otherAddress,
                                          "paymentMethod",
                                          "ESCROW_SIMULATION"))))
                      .andReturn()
                      .getResponse()
                      .getStatus();
                }));
      ready.await(10, TimeUnit.SECONDS);
      go.countDown();
      var results = new ArrayList<Integer>();
      for (var f : futures) results.add(f.get(30, TimeUnit.SECONDS));
      assertThat(results).containsExactlyInAnyOrder(200, 409);
      assertThat(
              jdbc.queryForObject(
                  "select count(*) from marketplace_orders where listing_id=?", Long.class, l))
          .isEqualTo(1);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void valuationMatchingFallbackSnapshotsAndOwnership() throws Exception {
    long l = listing();
    var input = Map.of("listingId", l, "accessoryCondition", "FULL", "repairLevel", "NONE");
    call(seller, "POST", "/api/valuations", Map.of("listingId", l), 400);
    call(other, "POST", "/api/valuations", input, 403);
    var first = call(seller, "POST", "/api/valuations", input, 200);
    assertThat(first.path("estimatedPrice").decimalValue()).isEqualByComparingTo("1610920");
    assertThat(first.path("conditionScore").isNull()).isTrue();
    long product = product(4);
    String name =
        jdbc.queryForObject(
            "select product_name from used_listings where listing_id=?", String.class, l);
    jdbc.update(
        "update products set product_name=?,sale_price=800000 where product_id=?",
        "  " + name.toUpperCase(java.util.Locale.ROOT) + "  ",
        product);
    var matched = call(seller, "POST", "/api/valuations", input, 200);
    assertThat(matched.path("referencePrice").decimalValue()).isEqualByComparingTo("800000");
    assertThat(matched.path("estimatedPrice").decimalValue()).isEqualByComparingTo("644368");
    long duplicate = product(2);
    jdbc.update("update products set product_name=? where product_id=?", name, duplicate);
    var ambiguous = call(seller, "POST", "/api/valuations", input, 200);
    assertThat(ambiguous.path("referencePrice").decimalValue()).isEqualByComparingTo("2000000");
    jdbc.update("update products set status='INACTIVE' where product_id=?", duplicate);
    assertThat(
            call(seller, "POST", "/api/valuations", input, 200)
                .path("referencePrice")
                .decimalValue())
        .isEqualByComparingTo("800000");
    assertThat(
            jdbc.queryForObject(
                "select estimated_price from valuations where valuation_id=?",
                java.math.BigDecimal.class,
                first.path("valuationId").asLong()))
        .isEqualByComparingTo("1610920");
    assertThat(call(seller, "GET", "/api/valuations/listing/" + l, null, 200).size()).isEqualTo(4);
  }

  @Test
  void catalogStatusVisibilityAndNoCascade() throws Exception {
    long p = product(3), l = listing();
    call(
        buyer,
        "PUT",
        "/api/categories/" + category,
        Map.of("categoryName", categoryName, "status", "INACTIVE"),
        403);
    call(
        admin,
        "PUT",
        "/api/categories/" + category,
        Map.of("categoryName", categoryName, "status", "INACTIVE"),
        200);
    call(admin, "DELETE", "/api/brands/" + brand, null, 200);
    assertThat(call(null, "GET", "/api/categories", null, 200).toString()).doesNotContain(categoryName);
    assertThat(call(null, "GET", "/api/brands", null, 200).toString()).doesNotContain(brandName);
    assertThat(call(admin, "GET", "/api/categories", null, 200).toString())
        .contains(categoryName)
        .contains("INACTIVE");
    assertThat(call(admin, "GET", "/api/brands", null, 200).toString())
        .contains(brandName)
        .contains("INACTIVE");
    assertThat(call(null, "GET", "/api/products/" + p, null, 200).path("status").asText())
        .isEqualTo("ACTIVE");
    assertThat(
            call(null, "GET", "/api/marketplace/listings/" + l, null, 200)
                .path("listing")
                .path("status")
                .asText())
        .isEqualTo("AVAILABLE");
    call(
        admin,
        "PUT",
        "/api/categories/" + category,
        Map.of("categoryName", categoryName, "status", "ACTIVE"),
        200);
    call(
        admin,
        "PUT",
        "/api/brands/" + brand,
        Map.of("brandName", brandName, "status", "ACTIVE"),
        200);
    call(
        admin,
        "PUT",
        "/api/brands/" + brand,
        Map.of("brandName", brandName, "status", "DELETED"),
        400);
    var created =
        call(
            admin,
            "POST",
            "/api/brands",
            Map.of("brandName", "Brand " + UUID.randomUUID(), "status", "INACTIVE"),
            201);
    assertThat(created.path("status").asText()).isEqualTo("ACTIVE");
  }

  @Test
  void cancelApiAndAdminAuditAuthorization() throws Exception {
    long l = listing(), o = purchase(l);
    call(other, "POST", "/api/market-orders/" + o + "/cancel", null, 403);
    call(seller, "POST", "/api/market-orders/" + o + "/cancel", null, 403);
    var cancelled = call(buyer, "POST", "/api/market-orders/" + o + "/cancel", null, 200);
    assertThat(cancelled.path("order").path("cancelledByType").asText()).isEqualTo("BUYER");
    assertThat(cancelled.path("order").path("cancellationReason").asText())
        .isEqualTo("BUYER_CHANGED_MIND");
    call(buyer, "POST", "/api/market-orders/" + o + "/cancel", null, 409);
    long paid = purchase(l);
    call(buyer, "POST", "/api/escrow/" + paid + "/pay", null, 200);
    call(buyer, "POST", "/api/market-orders/" + paid + "/cancel", null, 409);
    call(buyer, "GET", "/api/admin/audit-logs", null, 403);
    call(null, "GET", "/api/admin/audit-logs", null, 401);
    var logs = call(admin, "GET", "/api/admin/audit-logs?page=0&size=1", null, 200);
    assertThat(logs.path("content").size()).isEqualTo(1);
    assertThat(logs.path("totalElements").asLong()).isPositive();
    call(admin, "GET", "/api/admin/audit-logs?size=101", null, 400);
  }

  @Test
  void pendingCancellationLeavesListingReusable() throws Exception {
    long l = listing(), o = purchase(l);
    var principal =
        new com.badminton.security.CurrentUser(buyerId, com.badminton.user.entity.Role.USER);
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal,
                null,
                java.util.List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_USER"))));
    try {
      market.cancel(o);
      assertThatThrownBy(() -> market.cancel(o))
          .isInstanceOf(com.badminton.exception.BusinessException.class);
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
    long next = purchase(l);
    assertThat(next).isNotEqualTo(o);
    assertThat(
            jdbc.queryForObject(
                "select count(*) from marketplace_orders where listing_id=?", Long.class, l))
        .isEqualTo(2);
  }

  @Test
  void inspectionOwnershipAndBadgeInvalidation() throws Exception {
    long l = listing();
    long id =
        call(seller, "POST", "/api/inspections", Map.of("listingId", l, "note", "Kiểm tra"), 201)
            .path("inspectionId")
            .asLong();
    call(seller, "POST", "/api/inspections", Map.of("listingId", l), 409);
    call(buyer, "POST", "/api/inspector/inspections/" + id + "/accept", null, 403);
    call(inspector, "POST", "/api/inspector/inspections/" + id + "/accept", null, 200);
    call(admin, "POST", "/api/inspector/inspections/" + id + "/start", null, 403);
    call(inspector, "POST", "/api/inspector/inspections/" + id + "/start", null, 200);
    var report = new HashMap<String, Object>();
    report.putAll(
        Map.of(
            "modelVerified",
            true,
            "serialVerified",
            true,
            "frameCondition",
            "GOOD",
            "paintCondition",
            "GOOD",
            "grommetCondition",
            "GOOD",
            "handleCondition",
            "GOOD",
            "crackDetected",
            false,
            "repairDetected",
            false,
            "overallScore",
            9.6));
    report.put("recommendedMinPrice", 900000);
    report.put("recommendedMaxPrice", 1100000);
    call(inspector, "POST", "/api/inspector/inspections/" + id + "/report", report, 200);
    assertThat(
            call(null, "GET", "/api/marketplace/listings/" + l, null, 200)
                .path("verified")
                .asBoolean())
        .isTrue();
    var verifiedValue =
        call(
            seller,
            "POST",
            "/api/valuations",
            Map.of("listingId", l, "accessoryCondition", "PARTIAL", "repairLevel", "NONE"),
            200);
    assertThat(verifiedValue.path("conditionScore").decimalValue()).isEqualByComparingTo("9.6");
    assertThat(verifiedValue.path("conditionFactor").decimalValue()).isEqualByComparingTo("1");
    call(
        seller,
        "POST",
        "/api/valuations",
        Map.of("listingId", l, "accessoryCondition", "PARTIAL", "repairLevel", "MINOR"),
        409);
    call(seller, "PUT", "/api/marketplace/listings/" + l, listingBody(), 200);
    var afterEdit =
        call(
            seller,
            "POST",
            "/api/valuations",
            Map.of("listingId", l, "accessoryCondition", "PARTIAL", "repairLevel", "MINOR"),
            200);
    assertThat(afterEdit.path("conditionScore").isNull()).isTrue();
    assertThat(afterEdit.path("conditionFactor").decimalValue()).isEqualByComparingTo("0.92");

    assertThat(
            call(seller, "GET", "/api/marketplace/listings/" + l, null, 200)
                .path("verified")
                .asBoolean())
        .isFalse();
  }

  @Test
  void adminStatisticsAndImmediateUserLock() throws Exception {
    call(admin, "GET", "/api/admin/statistics/overview", null, 200);
    call(admin, "GET", "/api/admin/statistics/revenue?from=2026-01-01&to=2026-12-31", null, 200);
    call(admin, "GET", "/api/admin/statistics/top-products", null, 200);
    call(admin, "GET", "/api/admin/statistics/top-sellers", null, 200);
    String t = account("USER");
    long id = call(t, "GET", "/api/users/me", null, 200).path("id").asLong();
    call(admin, "POST", "/api/admin/users/" + id + "/lock", null, 200);
    call(t, "GET", "/api/users/me", null, 401);
    call(admin, "POST", "/api/admin/users/" + id + "/unlock", null, 200);
    call(t, "GET", "/api/users/me", null, 200);
  }

  @Test
  void localDemoCleanupIsIdempotentAndPreservesOrderHistory() throws Exception {
    long unused = product(2), ordered = product(2), genuine = product(2);
    String originalSlug = jdbc.queryForObject("select slug from products where product_id=?", String.class, ordered);
    jdbc.update("update products set slug=concat('cleaned-demo-', product_id) where slug='demo-attack'");
    jdbc.update("update products set slug='demo-easy-start' where product_id=?", unused);
    jdbc.update("update products set slug='demo-attack' where product_id=?", ordered);
    String token = account("USER");
    long a = address(token);
    call(token, "POST", "/api/cart/items", Map.of("productId", ordered, "quantity", 1), 200);
    call(token, "POST", "/api/orders", Map.of("addressId", a, "paymentMethod", "COD"), 201);
    call(token, "POST", "/api/cart/items", Map.of("productId", unused, "quantity", 1), 200);
    call(token, "POST", "/api/favorites/" + unused, null, 200);
    try (var connection = jdbc.getDataSource().getConnection()) {
      String sql =
          java.nio.file.Files.readString(java.nio.file.Path.of("../database/01_demo_catalog.sql"))
              .replace("USE badminton_ecommerce;",
                  "USE `" + connection.getCatalog().replace("`", "``") + "`;");
      var resource =
          new org.springframework.core.io.ByteArrayResource(
              sql.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, resource);
      org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, resource);
    }
    assertThat(
            jdbc.queryForObject(
                "select count(*) from products where product_id=?", Long.class, unused))
        .isZero();
    assertThat(jdbc.queryForObject("select status from products where product_id=?", String.class, ordered))
        .isEqualTo("INACTIVE");
    assertThat(jdbc.queryForObject("select stock from products where product_id=?", Integer.class, ordered))
        .isZero();
    assertThat(jdbc.queryForObject("select count(*) from order_items where product_id=?", Long.class, ordered))
        .isEqualTo(1);
    assertThat(jdbc.queryForObject("select status from products where product_id=?", String.class, genuine))
        .isEqualTo("ACTIVE");
    jdbc.update("update products set slug=? where product_id=?", originalSlug, ordered);
  }

  private List<Integer> race(
      java.util.concurrent.Callable<Integer> first, java.util.concurrent.Callable<Integer> second)
      throws Exception {
    var pool = Executors.newFixedThreadPool(2);
    var ready = new CountDownLatch(2);
    var go = new CountDownLatch(1);
    try {
      var results = new ArrayList<Future<Integer>>();
      for (var action : List.of(first, second))
        results.add(
            pool.submit(
                () -> {
                  ready.countDown();
                  go.await();
                  return action.call();
                }));
      assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
      go.countDown();
      return List.of(
          results.get(0).get(30, TimeUnit.SECONDS), results.get(1).get(30, TimeUnit.SECONDS));
    } finally {
      pool.shutdownNow();
    }
  }

  private int actionStatus(String token, String method, String path, Object body) throws Exception {
    var request =
        MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method), path)
            .header("Authorization", "Bearer " + token);
    if (body != null)
      request.contentType("application/json").content(json.writeValueAsString(body));
    return mvc.perform(request).andReturn().getResponse().getStatus();
  }

  @Test
  void reviewRequiresCompletedOwnedItemAndFavoriteIsIdempotent() throws Exception {
    var token = account("USER");
    long p = product(2), a = address(token);
    call(token, "POST", "/api/favorites/" + p, null, 200);
    call(token, "POST", "/api/favorites/" + p, null, 200);
    assertThat(call(token, "GET", "/api/favorites", null, 200).path("totalElements").asInt())
        .isEqualTo(1);
    call(token, "POST", "/api/cart/items", Map.of("productId", p, "quantity", 1), 200);
    var result =
        call(token, "POST", "/api/orders", Map.of("addressId", a, "paymentMethod", "COD"), 201);
    long id = result.path("order").path("orderId").asLong(),
        item = result.path("items").get(0).path("orderItemId").asLong();
    var review = Map.of("orderItemId", item, "rating", 5, "comment", "Đúng mô tả");
    call(token, "POST", "/api/reviews", review, 409);
    call(other, "POST", "/api/reviews", review, 403);
    for (var state : List.of("CONFIRMED", "PREPARING", "SHIPPING", "COMPLETED"))
      call(admin, "PATCH", "/api/admin/orders/" + id + "/status", Map.of("status", state), 200);
    call(token, "POST", "/api/reviews", review, 201);
    call(token, "POST", "/api/reviews", review, 409);
    assertThat(
            call(null, "GET", "/api/products/" + p + "/reviews", null, 200)
                .path("totalElements")
                .asInt())
        .isEqualTo(1);
    call(token, "DELETE", "/api/favorites/" + p, null, 200);
    assertThat(call(token, "GET", "/api/favorites", null, 200).path("totalElements").asInt())
        .isZero();
  }

  @Test
  void notificationOwnershipAndReadAllStayScoped() throws Exception {
    long o = purchase(listing());
    long id =
        jdbc.queryForObject(
            "select notification_id from notifications where user_id=? and reference_id=? and"
                + " reference_type='MarketplaceOrder' order by notification_id desc limit 1",
            Long.class,
            buyerId,
            o);
    call(other, "PATCH", "/api/notifications/" + id + "/read", null, 403);
    call(buyer, "PATCH", "/api/notifications/" + id + "/read", null, 200);
    assertThat(
            jdbc.queryForObject(
                "select is_read from notifications where notification_id=?", Boolean.class, id))
        .isTrue();
    call(buyer, "PATCH", "/api/notifications/read-all", null, 200);
    assertThat(
            jdbc.queryForObject(
                "select count(*) from notifications where user_id=? and is_read=0",
                Long.class,
                buyerId))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "select count(*) from notifications where user_id=? and reference_id=? and"
                    + " is_read=0",
                Long.class,
                sellerId,
                o))
        .isGreaterThan(0);
  }

  @Test
  void disputeEvidenceIsPrivateAndRejectsSpoofedImages() throws Exception {
    long o = purchase(listing());
    shipped(o);
    long d =
        call(
                buyer,
                "POST",
                "/api/disputes",
                Map.of(
                    "marketOrderId", o, "reason", "NOT_RECEIVED", "description", "Chưa nhận hàng"),
                200)
            .path("disputeId")
            .asLong();
    var buffer = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", buffer);
    var file = new MockMultipartFile("file", "proof.png", "image/png", buffer.toByteArray());
    assertThat(
            mvc.perform(
                    multipart("/api/disputes/" + d + "/evidences")
                        .file(file)
                        .header("Authorization", "Bearer " + other))
                .andReturn()
                .getResponse()
                .getStatus())
        .isEqualTo(403);
    var fake = new MockMultipartFile("file", "proof.png", "image/png", "not an image".getBytes());
    assertThat(
            mvc.perform(
                    multipart("/api/disputes/" + d + "/evidences")
                        .file(fake)
                        .header("Authorization", "Bearer " + buyer))
                .andReturn()
                .getResponse()
                .getStatus())
        .isEqualTo(400);
    assertThat(
            mvc.perform(
                    multipart("/api/disputes/" + d + "/evidences")
                        .file(file)
                        .header("Authorization", "Bearer " + buyer))
                .andReturn()
                .getResponse()
                .getStatus())
        .isEqualTo(200);
    assertThat(
            call(buyer, "GET", "/api/disputes/" + d, null, 200)
                .path("evidences")
                .get(0)
                .path("fileUrl")
                .asText())
        .startsWith("data:image/png;base64,");
    String key =
        jdbc.queryForObject(
            "select file_url from dispute_evidences where dispute_id=?", String.class, d);
    call(null, "GET", "/media/" + key, null, 404);
    call(other, "GET", "/api/disputes/" + d, null, 403);
    call(
        admin,
        "POST",
        "/api/admin/disputes/" + d + "/resolve",
        Map.of("resolution", "FULL_REFUND", "resolutionNote", "Đã xem bằng chứng"),
        200);
    assertThat(
            mvc.perform(
                    multipart("/api/disputes/" + d + "/evidences")
                        .file(file)
                        .header("Authorization", "Bearer " + buyer))
                .andReturn()
                .getResponse()
                .getStatus())
        .isEqualTo(409);
  }

  @Test
  void concurrentPayAndConfirmReleaseOnce() throws Exception {
    long o = purchase(listing());
    java.util.concurrent.Callable<Integer> pay =
        () -> actionStatus(buyer, "POST", "/api/escrow/" + o + "/pay", null);
    assertThat(race(pay, pay)).containsExactlyInAnyOrder(200, 409);
    long shipment =
        call(
                seller,
                "POST",
                "/api/shipments",
                Map.of(
                    "marketOrderId",
                    o,
                    "carrier",
                    "TEST",
                    "trackingCode",
                    UUID.randomUUID().toString()),
                200)
            .path("shipmentId")
            .asLong();
    delivered(shipment);
    java.util.concurrent.Callable<Integer> confirm =
        () -> actionStatus(buyer, "POST", "/api/market-orders/" + o + "/confirm-received", null);
    assertThat(race(confirm, confirm)).containsExactlyInAnyOrder(200, 409);
    assertThat(
            jdbc.queryForObject(
                "select count(*) from audit_logs where action='CONFIRM_RECEIVED' and entity_id=?",
                Long.class,
                o))
        .isEqualTo(1);
  }

  @Test
  void utcTimestampsMatchDatabaseAndDoNotExpireFreshOrders() throws Exception {
    long o = purchase(listing());
    var order = call(buyer, "GET", "/api/market-orders/" + o, null, 200).path("order");
    String stored = jdbc.queryForObject(
        "select date_format(created_at, '%Y-%m-%dT%H:%i:%s.%f') from marketplace_orders where market_order_id=?",
        String.class, o);
    assertThat(java.time.LocalDateTime.parse(stored))
        .isEqualTo(java.time.LocalDateTime.parse(order.path("createdAt").asText()));
    assertThat(marketOrders.expiredIds(
        com.badminton.marketorder.entity.MarketOrderStatus.PENDING_PAYMENT,
        java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(30)))
        .doesNotContain(o);
  }

  @Test
  void timeoutCancelsOnlyUnpaidOrders() throws Exception {
    long l = listing(), o = purchase(l);
    jdbc.update(
        "update marketplace_orders set created_at=utc_timestamp()-interval 60 minute where"
            + " market_order_id=?",
        o);
    market.expire(o, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(30));
    market.expire(o, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
    var result = call(buyer, "GET", "/api/market-orders/" + o, null, 200);
    assertThat(result.path("order").path("cancelledByType").asText()).isEqualTo("SYSTEM");
    assertThat(result.path("order").path("cancellationReason").asText())
        .isEqualTo("PAYMENT_TIMEOUT");
    long next = purchase(l);
    call(buyer, "POST", "/api/escrow/" + next + "/pay", null, 200);
    market.expire(next, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(60));
    assertThat(
            call(buyer, "GET", "/api/market-orders/" + next, null, 200)
                .path("order")
                .path("orderStatus")
                .asText())
        .isEqualTo("WAITING_SHIPMENT");
  }

  @Test
  void concurrentShopCheckoutCannotOversell() throws Exception {
    long p = product(1);
    String first = account("USER"), second = account("USER");
    long a = address(first), b = address(second);
    for (String token : List.of(first, second))
      call(token, "POST", "/api/cart/items", Map.of("productId", p, "quantity", 1), 200);
    assertThat(
            race(
                () ->
                    actionStatus(
                        first,
                        "POST",
                        "/api/orders",
                        Map.of("addressId", a, "paymentMethod", "COD")),
                () ->
                    actionStatus(
                        second,
                        "POST",
                        "/api/orders",
                        Map.of("addressId", b, "paymentMethod", "COD"))))
        .containsExactlyInAnyOrder(201, 409);
    assertThat(
            jdbc.queryForObject("select stock from products where product_id=?", Integer.class, p))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "select count(*) from order_items where product_id=?", Long.class, p))
        .isEqualTo(1);
  }
}
