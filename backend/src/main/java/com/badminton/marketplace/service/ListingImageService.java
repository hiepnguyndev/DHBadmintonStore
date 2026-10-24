package com.badminton.marketplace.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.support.*;
import com.badminton.marketplace.entity.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ListingImageService {
  private final com.badminton.marketplace.repository.UsedListingRepository listings;
  private final com.badminton.marketplace.repository.UsedListingImageRepository images;
  private final com.badminton.common.storage.ImageStorage storage;
  private final com.badminton.audit.service.AuditService audit;
  private final Access access;

  public ListingImageService(
      com.badminton.marketplace.repository.UsedListingRepository listings,
      com.badminton.marketplace.repository.UsedListingImageRepository images,
      com.badminton.common.storage.ImageStorage storage,
      com.badminton.audit.service.AuditService audit,
      Access access) {
    this.listings = listings;
    this.images = images;
    this.storage = storage;
    this.audit = audit;
    this.access = access;
  }

  public Object upload(
      Long id, org.springframework.web.multipart.MultipartFile file, ImageType imageType) {
    var l = found(listings.lock(id), "LISTING_NOT_FOUND");
    access.owner(l.getSeller().getUserId());
    require(
        Set.of(ListingStatus.DRAFT, ListingStatus.REJECTED, ListingStatus.AVAILABLE)
            .contains(l.getStatus()),
        "LISTING_INVALID_STATUS",
        "Không thể sửa ảnh ở trạng thái hiện tại");
    long count = images.count(eq("listing.listingId", id));
    bad(count < 10, "Tối đa 10 ảnh mỗi tin");
    var saved = storage.store(file, false);
    var i = new UsedListingImage();
    i.setListing(l);
    i.setImageUrl("/media/" + saved.key());
    i.setImageType(imageType);
    i.setSortOrder((int) count);
    images.saveAndFlush(i);
    if (l.getStatus() == ListingStatus.AVAILABLE) l.setStatus(ListingStatus.PENDING_REVIEW);
    l.setConditionScore(null);
    audit.record(
        "INVALIDATE_INSPECTION", "UsedListing", id, null, Map.of("imageId", i.getImageId()));
    return com.badminton.marketplace.mapper.UsedListingImageMapper.toResponse(i);
  }
}
