package com.badminton.brand.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.brand.dto.request.*;
import com.badminton.brand.dto.response.*;
import com.badminton.brand.entity.*;
import com.badminton.brand.mapper.*;
import com.badminton.brand.repository.*;
import com.badminton.common.support.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandService {
  private final BrandRepository brands;
  private final Access access;

  public BrandService(BrandRepository brands, Access access) {
    this.brands = brands;
    this.access = access;
  }

  @Transactional(readOnly = true)
  public List<BrandResponse> list() {
    return brands.findAll().stream()
        .filter(
            e ->
                (access.optional() != null
                        && access.optional().role() == com.badminton.user.entity.Role.ADMIN)
                    || e.getStatus() == BrandStatus.ACTIVE)
        .map(BrandMapper::toResponse)
        .toList();
  }

  public BrandResponse save(Long id, BrandRequest r) {
    Brand b = id == null ? new Brand() : found(brands.lock(id), "BRAND_NOT_FOUND");
    b.setBrandName(r.brandName().trim());
    b.setDescription(r.description());
    if (id == null) b.setStatus(BrandStatus.ACTIVE);
    else if (r.status() != null) b.setStatus(r.status());
    b.setLogoUrl(r.logoUrl());
    if (id == null) {
      b.setSlug(slug(r.brandName()));
    }
    return BrandMapper.toResponse(brands.saveAndFlush(b));
  }

  public void disable(Long id) {
    var brand = found(brands.lock(id), "BRAND_NOT_FOUND");
    brand.setStatus(BrandStatus.INACTIVE);
  }
}
