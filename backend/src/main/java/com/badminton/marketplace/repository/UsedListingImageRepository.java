package com.badminton.marketplace.repository;

import com.badminton.marketplace.entity.UsedListingImage;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsedListingImageRepository
    extends JpaRepository<UsedListingImage, Long>, JpaSpecificationExecutor<UsedListingImage> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from UsedListingImage e where e.imageId=:id")
  Optional<UsedListingImage> lock(@Param("id") Long id);

  List<UsedListingImage> findByListingListingIdOrderBySortOrderAsc(Long id);
}
