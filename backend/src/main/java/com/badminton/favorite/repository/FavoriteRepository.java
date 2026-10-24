package com.badminton.favorite.repository;

import com.badminton.favorite.entity.Favorite;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository
    extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Favorite e where e.favoriteId=:id")
  Optional<Favorite> lock(@Param("id") Long id);

  Optional<Favorite> findByUserUserIdAndProductProductId(Long userId, Long productId);
}
