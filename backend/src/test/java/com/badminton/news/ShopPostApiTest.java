package com.badminton.news;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.news.controller.*;
import com.badminton.news.entity.ShopPost;
import com.badminton.news.service.ShopPostService;
import com.badminton.security.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {ShopPostController.class, AdminShopPostController.class},
    properties = "app.cors.allowed-origins=http://localhost:5173")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class, GlobalExceptionHandler.class})
class ShopPostApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean ShopPostService service;
  @MockitoBean JwtService jwt;
  @MockitoBean CustomUserDetailsService details;
  static final String BODY = "{\"title\":\"Shop news\",\"summary\":\"Summary\",\"content\":\"Content\",\"category\":\"NEWS\",\"status\":\"DRAFT\"}";
  MockMultipartFile post(String body) {
    return new MockMultipartFile("post", "post.json", "application/json", body.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void publicReadDoesNotExposeAnAdminStatusFilter() throws Exception {
    when(service.list(false, null, ShopPost.Category.NEWS, null, 0, 3))
        .thenReturn(new PageResponse<>(List.of(), 0, 3, 0, 0));
    mvc.perform(get("/api/posts").param("category", "NEWS").param("size", "3").param("status", "DRAFT"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
    verify(service).list(false, null, ShopPost.Category.NEWS, null, 0, 3);
    mvc.perform(get("/api/posts/1")).andExpect(status().isOk());
    verify(service).get(1L);
  }

  @Test
  void anonymousCannotWriteOrReadDrafts() throws Exception {
    mvc.perform(get("/api/admin/posts")).andExpect(status().isUnauthorized());
    mvc.perform(multipart("/api/admin/posts").file(post(BODY))).andExpect(status().isUnauthorized());
    mvc.perform(multipart(HttpMethod.PUT, "/api/admin/posts/1").file(post(BODY))).andExpect(status().isUnauthorized());
    mvc.perform(delete("/api/admin/posts/1")).andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "USER")
  void customerCannotManageShopPosts() throws Exception {
    mvc.perform(get("/api/admin/posts")).andExpect(status().isForbidden());
    mvc.perform(multipart("/api/admin/posts").file(post(BODY))).andExpect(status().isForbidden());
    mvc.perform(multipart(HttpMethod.PUT, "/api/admin/posts/1").file(post(BODY))).andExpect(status().isForbidden());
    mvc.perform(delete("/api/admin/posts/1")).andExpect(status().isForbidden());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "INSPECTOR")
  void inspectorCannotPublish() throws Exception {
    mvc.perform(multipart("/api/admin/posts").file(post(BODY))).andExpect(status().isForbidden());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanSaveMultipartContentAndArchive() throws Exception {
    mvc.perform(multipart("/api/admin/posts").file(post(BODY))).andExpect(status().isCreated());
    verify(service).save(isNull(), argThat(r -> r.status() == ShopPost.Status.DRAFT), isNull());
    var cover = new MockMultipartFile("cover", "cover.png", "image/png", new byte[] {1});
    mvc.perform(multipart(HttpMethod.PUT, "/api/admin/posts/1").file(post(BODY.replace("DRAFT", "PUBLISHED"))).file(cover))
        .andExpect(status().isOk());
    verify(service).save(eq(1L), argThat(r -> r.status() == ShopPost.Status.PUBLISHED), any());
    mvc.perform(delete("/api/admin/posts/1")).andExpect(status().isOk());
    verify(service).archive(1L);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void validatesRequiredLengthAndEnumFields() throws Exception {
    for (String body : List.of("{}", BODY.replace("Shop news", " "), BODY.replace("NEWS", "OTHER"),
        BODY.replace("Summary", "x".repeat(401)), BODY.replace("Content", "x".repeat(30001)))) {
      mvc.perform(multipart("/api/admin/posts").file(post(body))).andExpect(status().isBadRequest());
    }
    mvc.perform(get("/api/posts").param("category", "OTHER")).andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }
}
