package com.badminton.product;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.product.controller.ProductController;
import com.badminton.product.service.ProductService;
import com.badminton.security.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductController.class,
    properties = "app.cors.allowed-origins=http://localhost:5173")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class,
    GlobalExceptionHandler.class})
class ProductOffersApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean ProductService service;
  @MockitoBean JwtService jwt;
  @MockitoBean CustomUserDetailsService details;

  void emptyCatalog() {
    when(service.list(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
        anyBoolean(), anyInt(), anyInt(), any()))
        .thenReturn(new PageResponse<>(List.of(), 0, 4, 0, 0));
  }

  @Test
  void anonymousOfferFilterUsesTheExistingCatalogEnvelope() throws Exception {
    emptyCatalog();
    mvc.perform(get("/api/products").param("onSale", "true").param("size", "4"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").isEmpty());
    verify(service).list(null, null, null, null, null, null, null, null, null, null,
        true, 0, 4, null);
  }

  @Test
  void ordinaryCatalogDefaultsToNoSaleFilter() throws Exception {
    emptyCatalog();
    mvc.perform(get("/api/products")).andExpect(status().isOk());
    verify(service).list(null, null, null, null, null, null, null, null, null, null,
        false, 0, 20, null);
  }

  @Test
  void rejectsInvalidBooleanFlags() throws Exception {
    mvc.perform(get("/api/products").param("onSale", "invalid"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }
}
