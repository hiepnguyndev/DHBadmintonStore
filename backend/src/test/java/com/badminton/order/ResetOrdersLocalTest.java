package com.badminton.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.badminton.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** Opt-in check against local MySQL. All reset changes are rolled back. */
@EnabledIfEnvironmentVariable(named = "RUN_LOCAL_RESET_TEST", matches = "true")
@SpringBootTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=validate"})
@Transactional
@Rollback
class ResetOrdersLocalTest {
  @Autowired OrderService service;
  @Autowired JdbcTemplate jdbc;

  @Test
  void resetsExistingOrdersAndWritesAuditWithoutConstraintErrors() {
    var ids = jdbc.queryForList("select order_id from orders", Long.class);
    assertThat(ids).as("Local fixture must contain orders").isNotEmpty();
    long users = jdbc.queryForObject("select count(*) from users", Long.class);
    long products = jdbc.queryForObject("select count(*) from products", Long.class);
    service.resetAll();
    assertThat(jdbc.queryForObject("select count(*) from orders", Long.class)).isZero();
    assertThat(jdbc.queryForObject("select count(*) from order_items", Long.class)).isZero();
    for (var id : ids) {
      assertThat(jdbc.queryForObject(
          "select count(*) from audit_logs where action='RESET_SHOP_ORDERS' and entity_id=?",
          Long.class, id)).isPositive();
    }
    assertThat(jdbc.queryForObject("select count(*) from users", Long.class)).isEqualTo(users);
    assertThat(jdbc.queryForObject("select count(*) from products", Long.class)).isEqualTo(products);
  }
}
