package com.badminton.news.dto;

import com.badminton.news.entity.ShopPost;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ShopPostResponse(
    Long id, String title, String summary, String content,
    ShopPost.Category category, ShopPost.Status status, String coverImageUrl,
    OffsetDateTime publishedAt, OffsetDateTime updatedAt) {
  public static ShopPostResponse from(ShopPost post) {
    return new ShopPostResponse(post.getPostId(), post.getTitle(), post.getSummary(), post.getContent(),
        post.getCategory(), post.getStatus(), post.getCoverImageUrl(),
        post.getPublishedAt() == null ? null : post.getPublishedAt().atOffset(ZoneOffset.UTC),
        post.getUpdatedAt().atOffset(ZoneOffset.UTC));
  }
}
