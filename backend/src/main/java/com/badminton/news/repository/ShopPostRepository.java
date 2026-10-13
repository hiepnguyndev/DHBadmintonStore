package com.badminton.news.repository;

import com.badminton.news.entity.ShopPost;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ShopPostRepository extends JpaRepository<ShopPost, Long>, JpaSpecificationExecutor<ShopPost> {
  Optional<ShopPost> findByPostIdAndStatus(Long id, ShopPost.Status status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from ShopPost p where p.postId = :id")
  Optional<ShopPost> lock(@Param("id") Long id);
}
