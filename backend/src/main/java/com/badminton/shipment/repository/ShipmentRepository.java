package com.badminton.shipment.repository;

import com.badminton.shipment.entity.Shipment;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository
    extends JpaRepository<Shipment, Long>, JpaSpecificationExecutor<Shipment> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Shipment e where e.shipmentId=:id")
  Optional<Shipment> lock(@Param("id") Long id);

  Optional<Shipment> findByMarketOrderMarketOrderId(Long id);

  boolean existsByMarketOrderMarketOrderId(Long id);

  @Query("select e.marketOrder.marketOrderId from Shipment e where e.shipmentId=:id")
  Optional<Long> orderId(@Param("id") Long id);
}
