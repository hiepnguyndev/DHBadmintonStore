package com.badminton.statistics.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StatisticsService {
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;

  public StatisticsService(org.springframework.jdbc.core.JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> overview() {
    var r = new LinkedHashMap<String, Object>();
    r.put(
        "shopRevenue",
        jdbc.queryForObject(
            "select coalesce(sum(total_amount),0) from orders where order_status='COMPLETED' and"
                + " payment_status='PAID'",
            BigDecimal.class));
    for (var q :
        List.of(
            new String[] {"shopOrders", "orders"},
            new String[] {"marketplaceOrders", "marketplace_orders"},
            new String[] {"users", "users"},
            new String[] {"products", "products"})) {
      r.put(q[0], jdbc.queryForObject("select count(*) from " + q[1], Long.class));
    }
    r.put(
        "activeListings",
        jdbc.queryForObject(
            "select count(*) from used_listings where status='AVAILABLE'", Long.class));
    r.put(
        "pendingInspections",
        jdbc.queryForObject(
            "select count(*) from inspection_requests where status in"
                + " ('REQUESTED','ACCEPTED','INSPECTING')",
            Long.class));
    r.put(
        "openDisputes",
        jdbc.queryForObject(
            "select count(*) from disputes where status not in ('RESOLVED','REJECTED')",
            Long.class));
    return r;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> revenue(LocalDate from, LocalDate to, String groupBy) {
    bad(
        from != null
            && to != null
            && !to.isBefore(from)
            && java.time.temporal.ChronoUnit.DAYS.between(from, to) <= 3660,
        "Khoảng ngày không hợp lệ (tối đa 10 năm)");
    bad(Set.of("DAY", "MONTH", "YEAR").contains(groupBy), "groupBy phải là DAY, MONTH hoặc YEAR");
    String format =
        switch (groupBy) {
          case "DAY" -> "%Y-%m-%d";
          case "YEAR" -> "%Y";
          default -> "%Y-%m";
        };
    return jdbc.queryForList(
        "select date_format(created_at, '"
            + format
            + "') as period, sum(total_amount) as revenue, count(*) as orderCount from orders where"
            + " order_status='COMPLETED' and payment_status='PAID' and created_at>=? and"
            + " created_at<? group by period order by period",
        from.atStartOfDay(),
        to.plusDays(1).atStartOfDay());
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> topProducts() {
    return jdbc.queryForList(
        "select i.product_id as productId, p.product_name as productName, sum(i.quantity) as"
            + " quantity, sum(i.total_price) as revenue from order_items i join orders o on"
            + " o.order_id=i.order_id join products p on p.product_id=i.product_id where"
            + " o.order_status='COMPLETED' and o.payment_status='PAID' group by"
            + " i.product_id,p.product_name order by quantity desc,i.product_id limit 10");
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> topSellers() {
    return jdbc.queryForList(
        "select o.seller_id as sellerId, u.full_name as fullName, count(*) as"
            + " completedTransactions, sum(o.item_price) as itemRevenue from marketplace_orders o"
            + " join users u on u.user_id=o.seller_id where o.order_status='COMPLETED' group by"
            + " o.seller_id,u.full_name order by completedTransactions desc,o.seller_id limit 10");
  }
}
