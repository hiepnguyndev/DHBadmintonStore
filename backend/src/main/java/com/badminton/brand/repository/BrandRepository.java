package com.badminton.brand.repository;

import com.badminton.brand.entity.Brand;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BrandRepository
    extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Brand e where e.brandId=:id")
  Optional<Brand> lock(@Param("id") Long id);
}
