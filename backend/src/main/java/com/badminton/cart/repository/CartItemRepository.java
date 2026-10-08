package com.badminton.cart.repository;

import com.badminton.cart.entity.CartItem;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository
    extends JpaRepository<CartItem, Long>, JpaSpecificationExecutor<CartItem> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from CartItem e where e.cartItemId=:id")
  Optional<CartItem> lock(@Param("id") Long id);

  List<CartItem> findByCartCartIdOrderByProductProductIdAsc(Long id);

  Optional<CartItem> findByCartCartIdAndProductProductId(Long cartId, Long productId);
}
