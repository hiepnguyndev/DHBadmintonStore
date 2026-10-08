package com.badminton.category.repository;

import com.badminton.category.entity.Category;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository
    extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Category e where e.categoryId=:id")
  Optional<Category> lock(@Param("id") Long id);
}
