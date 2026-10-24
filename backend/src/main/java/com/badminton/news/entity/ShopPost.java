package com.badminton.news.entity;

import com.badminton.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_posts")
public class ShopPost extends BaseEntity {
  public enum Category { NEWS, COMMUNITY }
  public enum Status { DRAFT, PUBLISHED, ARCHIVED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long postId;
  @Column(nullable = false, length = 180)
  private String title;
  @Column(nullable = false, length = 400)
  private String summary;
  @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
  private String content;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Category category;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Status status = Status.DRAFT;
  @Column(name = "cover_image_url", length = 500)
  private String coverImageUrl;
  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  public Long getPostId() { return postId; }
  public String getTitle() { return title; }
  public void setTitle(String value) { title = value; }
  public String getSummary() { return summary; }
  public void setSummary(String value) { summary = value; }
  public String getContent() { return content; }
  public void setContent(String value) { content = value; }
  public Category getCategory() { return category; }
  public void setCategory(Category value) { category = value; }
  public Status getStatus() { return status; }
  public void setStatus(Status value) { status = value; }
  public String getCoverImageUrl() { return coverImageUrl; }
  public void setCoverImageUrl(String value) { coverImageUrl = value; }
  public LocalDateTime getPublishedAt() { return publishedAt; }
  public void setPublishedAt(LocalDateTime value) { publishedAt = value; }
}
