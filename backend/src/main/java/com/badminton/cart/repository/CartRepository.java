package com.badminton.cart.repository;

import com.badminton.cart.entity.Cart;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Cart e where e.cartId=:id")
  Optional<Cart> lock(@Param("id") Long id);

  Optional<Cart> findByUserUserId(Long id);
}
