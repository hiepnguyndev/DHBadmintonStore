package com.badminton.product.repository;
import com.badminton.product.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
 List<ProductVariant> findByProductProductIdOrderByColorAscSizeAsc(Long productId);
 List<ProductVariant> findByProductProductIdIn(Collection<Long> productIds);
 List<ProductVariant> findByProductProductIdAndStatusOrderByColorAscSizeAsc(Long productId,String status);
 boolean existsByProductProductId(Long productId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select v from ProductVariant v where v.variantId=:id") Optional<ProductVariant> lock(@Param("id") Long id);
 Optional<ProductVariant> findByVariantIdAndProductProductId(Long id,Long productId);
}
