package com.badminton.marketplace.repository;

import com.badminton.marketplace.entity.UsedListing;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UsedListingRepository
    extends JpaRepository<UsedListing, Long>, JpaSpecificationExecutor<UsedListing> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from UsedListing e where e.listingId=:id")
  Optional<UsedListing> lock(@Param("id") Long id);
}
