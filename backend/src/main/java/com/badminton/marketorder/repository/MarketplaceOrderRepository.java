package com.badminton.marketorder.repository;

import com.badminton.marketorder.entity.MarketplaceOrder;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MarketplaceOrderRepository
    extends JpaRepository<MarketplaceOrder, Long>, JpaSpecificationExecutor<MarketplaceOrder> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from MarketplaceOrder e where e.marketOrderId=:id")
  Optional<MarketplaceOrder> lock(@Param("id") Long id);

  @Query("select o.listing.listingId from MarketplaceOrder o where o.marketOrderId=:id")
  Optional<Long> listingId(@Param("id") Long id);

  @Query(
      "select o.marketOrderId from MarketplaceOrder o where o.orderStatus=:status and"
          + " o.createdAt<:cutoff")
  List<Long> expiredIds(
      @Param("status") com.badminton.marketorder.entity.MarketOrderStatus status,
      @Param("cutoff") java.time.LocalDateTime cutoff);
}
