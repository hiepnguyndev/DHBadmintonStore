package com.badminton.news.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.storage.ImageStorage;
import com.badminton.news.dto.*;
import com.badminton.news.entity.ShopPost;
import com.badminton.news.repository.ShopPostRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ShopPostService {
  private final ShopPostRepository posts;
  private final ImageStorage images;

  public ShopPostService(ShopPostRepository posts, ImageStorage images) {
    this.posts = posts;
    this.images = images;
  }

  @Transactional(readOnly = true)
  public PageResponse<ShopPostResponse> list(boolean admin, String keyword, ShopPost.Category category,
      ShopPost.Status status, int page, int size) {
    Specification<ShopPost> filter = all();
    if (!admin) filter = filter.and(eq("status", ShopPost.Status.PUBLISHED));
    else if (status != null) filter = filter.and(eq("status", status));
    else filter = filter.and((r, q, b) -> r.get("status").in(ShopPost.Status.DRAFT, ShopPost.Status.PUBLISHED));
    if (category != null) filter = filter.and(eq("category", category));
    if (keyword != null && !keyword.isBlank()) filter = filter.and(contains("title", keyword));
    var requested = page(page, size, null, admin ? "updatedAt" : "publishedAt", Set.of("updatedAt", "publishedAt"));
    var pageable = PageRequest.of(requested.getPageNumber(), requested.getPageSize(),
        requested.getSort().and(Sort.by(Sort.Direction.DESC, "postId")));
    var result = posts.findAll(filter, pageable);
    return new PageResponse<>(result.getContent().stream().map(ShopPostResponse::from).toList(),
        result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
  }

  @Transactional(readOnly = true)
  public ShopPostResponse get(Long id) {
    return ShopPostResponse.from(found(posts.findByPostIdAndStatus(id, ShopPost.Status.PUBLISHED), "POST_NOT_FOUND"));
  }

  public ShopPostResponse save(Long id, ShopPostRequest request, MultipartFile cover) {
    bad(request.status() != ShopPost.Status.ARCHIVED, "Dùng thao tác lưu trữ để gỡ bài viết");
    ShopPost post = id == null ? new ShopPost() : found(posts.lock(id), "POST_NOT_FOUND");
    var stored = cover == null ? null : images.store(cover, false);
    post.setTitle(request.title().trim());
    post.setSummary(request.summary().trim());
    post.setContent(request.content().trim());
    post.setCategory(request.category());
    post.setStatus(request.status());
    if (request.status() == ShopPost.Status.PUBLISHED && post.getPublishedAt() == null)
      post.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
    if (stored != null) post.setCoverImageUrl("/media/" + stored.key());
    return ShopPostResponse.from(posts.saveAndFlush(post));
  }

  public ShopPostResponse archive(Long id) {
    ShopPost post = found(posts.lock(id), "POST_NOT_FOUND");
    post.setStatus(ShopPost.Status.ARCHIVED);
    return ShopPostResponse.from(posts.saveAndFlush(post));
  }
}
