package com.badminton.review.repository;

import com.badminton.review.entity.Review;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository
    extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Review e where e.reviewId=:id")
  Optional<Review> lock(@Param("id") Long id);

  boolean existsByUserUserIdAndOrderItemOrderItemId(Long userId, Long itemId);

  void deleteByOrderItemOrderOrderIdIn(Collection<Long> orderIds);
}
