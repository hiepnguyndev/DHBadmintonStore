package com.badminton.product.repository;

import com.badminton.product.entity.ProductImage;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository
    extends JpaRepository<ProductImage, Long>, JpaSpecificationExecutor<ProductImage> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from ProductImage e where e.imageId=:id")
  Optional<ProductImage> lock(@Param("id") Long id);

  List<ProductImage> findByProductProductIdOrderBySortOrderAsc(Long id);
}
