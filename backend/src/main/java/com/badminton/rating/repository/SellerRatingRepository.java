package com.badminton.rating.repository;

import com.badminton.rating.entity.SellerRating;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SellerRatingRepository
    extends JpaRepository<SellerRating, Long>, JpaSpecificationExecutor<SellerRating> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from SellerRating e where e.ratingId=:id")
  Optional<SellerRating> lock(@Param("id") Long id);

  Optional<SellerRating> findByMarketOrderMarketOrderId(Long id);

  boolean existsByMarketOrderMarketOrderId(Long id);
}
