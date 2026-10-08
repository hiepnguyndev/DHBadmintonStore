package com.badminton.product.repository;

import com.badminton.product.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Product e where e.productId=:id")
  Optional<Product> lock(@Param("id") Long id);
}
