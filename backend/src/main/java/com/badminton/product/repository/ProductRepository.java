package com.badminton.product.repository;

import com.badminton.product.entity.Product;
import com.badminton.product.entity.ProductStatus;
import com.badminton.brand.entity.BrandStatus;
import com.badminton.category.entity.CategoryStatus;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query("select p from Product p join fetch p.brand b join fetch p.category c "
      + "where p.status = :status and b.status = :brandStatus and c.status = :categoryStatus")
  List<Product> findCatalogCandidates(
      @Param("status") ProductStatus status,
      @Param("brandStatus") BrandStatus brandStatus,
      @Param("categoryStatus") CategoryStatus categoryStatus);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Product e where e.productId=:id")
  Optional<Product> lock(@Param("id") Long id);
}
