package com.badminton.category.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.category.dto.request.*;
import com.badminton.category.dto.response.*;
import com.badminton.category.entity.*;
import com.badminton.category.mapper.*;
import com.badminton.category.repository.*;
import com.badminton.common.support.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {
  private final CategoryRepository categories;
  private final Access access;

  public CategoryService(CategoryRepository categories, Access access) {
    this.categories = categories;
    this.access = access;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> list() {
    return categories.findAll().stream()
        .filter(
            e ->
                (access.optional() != null
                        && access.optional().role() == com.badminton.user.entity.Role.ADMIN)
                    || e.getStatus() == CategoryStatus.ACTIVE)
        .map(CategoryMapper::toResponse)
        .toList();
  }

  public CategoryResponse save(Long id, CategoryRequest r) {
    Category c = id == null ? new Category() : found(categories.lock(id), "CATEGORY_NOT_FOUND");
    c.setCategoryName(r.categoryName().trim());
    c.setDescription(r.description());
    if (id == null) c.setStatus(CategoryStatus.ACTIVE);
    else if (r.status() != null) c.setStatus(r.status());
    if (id == null) {
      c.setSlug(slug(r.categoryName()));
    }
    return CategoryMapper.toResponse(categories.saveAndFlush(c));
  }
}
