package com.badminton.integration;

import static org.assertj.core.api.Assertions.*;

import com.badminton.exception.BusinessException;
import com.badminton.news.dto.*;
import com.badminton.news.entity.ShopPost;
import com.badminton.news.repository.ShopPostRepository;
import com.badminton.news.service.ShopPostService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_TESTS", matches = "true")
@Transactional
class ShopPostsMySqlIntegrationTest {
  @Autowired ShopPostService service;
  @Autowired ShopPostRepository posts;

  ShopPostRequest request(String title, ShopPost.Status status, ShopPost.Category category) {
    return new ShopPostRequest(title, "Tin từ cửa hàng DH", "Nội dung tiếng Việt\nDòng tiếp theo <script>alert(1)</script>", category, status);
  }

  @Test
  void draftPublishEditUnpublishArchiveAndRestoreArePersisted() {
    var title = "Shop workflow " + UUID.randomUUID();
    var draft = service.save(null, request(title, ShopPost.Status.DRAFT, ShopPost.Category.NEWS), null);
    assertThat(draft.publishedAt()).isNull();
    assertThatThrownBy(() -> service.get(draft.id())).isInstanceOf(BusinessException.class);
    var published = service.save(draft.id(), request(title, ShopPost.Status.PUBLISHED, ShopPost.Category.NEWS), null);
    assertThat(published.publishedAt()).isNotNull();
    assertThat(service.get(draft.id()).content()).contains("tiếng Việt", "<script>");
    assertThat(service.list(false, title, null, null, 0, 3).totalElements()).isEqualTo(1);
    var edited = service.save(draft.id(), request(title + " edited", ShopPost.Status.PUBLISHED, ShopPost.Category.COMMUNITY), null);
    assertThat(edited.publishedAt()).isEqualTo(published.publishedAt());
    service.save(draft.id(), request(title, ShopPost.Status.DRAFT, ShopPost.Category.NEWS), null);
    assertThatThrownBy(() -> service.get(draft.id())).isInstanceOf(BusinessException.class);
    assertThat(service.archive(draft.id()).status()).isEqualTo(ShopPost.Status.ARCHIVED);
    assertThat(service.list(true, title, null, null, 0, 12).content()).isEmpty();
    assertThat(service.list(true, title, null, ShopPost.Status.ARCHIVED, 0, 12).totalElements()).isEqualTo(1);
    var restored = service.save(draft.id(), request(title, ShopPost.Status.PUBLISHED, ShopPost.Category.NEWS), null);
    assertThat(service.get(restored.id()).status()).isEqualTo(ShopPost.Status.PUBLISHED);
  }

  @Test
  void filtersPublishedAndCategoryBeforePaginationAndIgnoresDraftFilterOnPublicList() {
    var prefix = "Posts filter " + UUID.randomUUID();
    service.save(null, request(prefix + " draft", ShopPost.Status.DRAFT, ShopPost.Category.NEWS), null);
    var first = service.save(null, request(prefix + " news", ShopPost.Status.PUBLISHED, ShopPost.Category.NEWS), null);
    var community = service.save(null, request(prefix + " community", ShopPost.Status.PUBLISHED, ShopPost.Category.COMMUNITY), null);
    var publicPage = service.list(false, prefix, null, ShopPost.Status.DRAFT, 0, 1);
    assertThat(publicPage.totalElements()).isEqualTo(2);
    assertThat(publicPage.totalPages()).isEqualTo(2);
    assertThat(publicPage.content()).extracting(ShopPostResponse::id).containsExactly(community.id());
    assertThat(service.list(false, prefix, null, null, 1, 1).content()).extracting(ShopPostResponse::id).containsExactly(first.id());
    assertThat(service.list(false, prefix, ShopPost.Category.NEWS, null, 0, 12).totalElements()).isEqualTo(1);
    assertThat(service.list(true, prefix, null, null, 0, 12).totalElements()).isEqualTo(3);
  }

  @Test
  void invalidCoverDoesNotPublishPartialChangesAndLargeVietnameseContentFits() {
    var draft = service.save(null, request("Invalid cover " + UUID.randomUUID(), ShopPost.Status.DRAFT, ShopPost.Category.NEWS), null);
    var cover = new MockMultipartFile("cover", "bad.svg", "image/svg+xml", "<svg/>".getBytes());
    assertThatThrownBy(() -> service.save(draft.id(), request("Attempt", ShopPost.Status.PUBLISHED, ShopPost.Category.NEWS), cover))
        .isInstanceOf(BusinessException.class);
    assertThat(posts.findById(draft.id()).orElseThrow().getStatus()).isEqualTo(ShopPost.Status.DRAFT);
    var large = new ShopPostRequest("Large post", "Summary", "ệ".repeat(30000), ShopPost.Category.NEWS, ShopPost.Status.DRAFT);
    var saved = service.save(null, large, null);
    assertThat(posts.findById(saved.id()).orElseThrow().getContent()).hasSize(30000);
    assertThatThrownBy(() -> service.save(null, request("Archived direct", ShopPost.Status.ARCHIVED, ShopPost.Category.NEWS), null))
        .isInstanceOf(BusinessException.class);
  }
}
