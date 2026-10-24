package com.badminton.product;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.product.controller.AdminPromotionController;
import com.badminton.product.service.PromotionService;
import com.badminton.security.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminPromotionController.class,
    properties = "app.cors.allowed-origins=http://localhost:5173")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class,
    GlobalExceptionHandler.class})
class AdminPromotionApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean PromotionService service;
  @MockitoBean JwtService jwt;
  @MockitoBean CustomUserDetailsService details;
  static final String BODY = "{\"discountType\":\"PERCENT\",\"value\":20}";

  @Test
  void anonymousCannotListPublishOrEnd() throws Exception {
    mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
    mvc.perform(put("/api/admin/products/1/promotion").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isUnauthorized());
    mvc.perform(delete("/api/admin/products/1/promotion")).andExpect(status().isUnauthorized());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "USER")
  void customerCannotListPublishOrEnd() throws Exception {
    mvc.perform(get("/api/admin/products")).andExpect(status().isForbidden());
    mvc.perform(put("/api/admin/products/1/promotion").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/admin/products/1/promotion")).andExpect(status().isForbidden());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "INSPECTOR")
  void inspectorCannotPublish() throws Exception {
    mvc.perform(put("/api/admin/products/1/promotion").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isForbidden());
    verifyNoInteractions(service);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanListSearchAndFilter() throws Exception {
    when(service.list("Vợt", true, 1, 12)).thenReturn(new PageResponse<>(List.of(), 1, 12, 0, 0));
    mvc.perform(get("/api/admin/products").param("keyword", "Vợt").param("published", "true").param("page", "1"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
    verify(service).list("Vợt", true, 1, 12);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanPublishAndEnd() throws Exception {
    mvc.perform(put("/api/admin/products/1/promotion").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isOk());
    verify(service).publish(eq(1L), argThat(r -> r.value().intValueExact() == 20));
    mvc.perform(delete("/api/admin/products/1/promotion")).andExpect(status().isOk());
    verify(service).end(1L);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void validatesMissingNegativeOverprecisionAndUnknownMode() throws Exception {
    for (String body : List.of("{}", "{\"discountType\":\"PERCENT\",\"value\":-1}",
        "{\"discountType\":\"SALE_PRICE\",\"value\":1.001}", "{\"discountType\":\"UNKNOWN\",\"value\":20}")) {
      mvc.perform(put("/api/admin/products/1/promotion").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }
    verifyNoInteractions(service);
  }
}
