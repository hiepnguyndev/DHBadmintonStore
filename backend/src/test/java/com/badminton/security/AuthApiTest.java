package com.badminton.security;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.badminton.auth.controller.AuthController;
import com.badminton.auth.dto.response.*;
import com.badminton.auth.service.AuthService;
import com.badminton.config.SecurityConfig;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.user.controller.*;
import com.badminton.user.dto.response.UserResponse;
import com.badminton.user.entity.Role;
import com.badminton.user.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {AuthController.class, UserController.class, AddressController.class},
    properties = "app.cors.allowed-origins=http://localhost:5173,http://127.0.0.1:5173,https://ngdhiep-fithau.id.vn")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  SecurityErrorWriter.class,
  GlobalExceptionHandler.class
})
class AuthApiTest {
  @Autowired MockMvc mvc;
  @MockitoBean AuthService auth;
  @MockitoBean UserService users;
  @MockitoBean AddressService addresses;
  @MockitoBean JwtService jwt;
  @MockitoBean CustomUserDetailsService details;

  @Test
  void registerIsPublicAndReturns201() throws Exception {
    when(auth.register(any())).thenReturn(new RegisterResponse(1L, "a@example.com"));
    mvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content(
                    """
{"fullName":"Name","email":"a@example.com","password":"Password123!","confirmPassword":"Password123!"}
"""))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value(1))
        .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
  }

  @Test
  void rejectsMassAssignmentRole() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content(
                    """
{"fullName":"Name","email":"a@example.com","password":"Password123!","confirmPassword":"Password123!","role":"ADMIN"}
"""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    verifyNoInteractions(auth);
  }

  @Test
  void validationUsesErrorEnvelope() throws Exception {
    mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errors.fullName").exists());
  }

  @Test
  void unauthenticatedProfileReturns401() throws Exception {
    mvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
  }

  @Test
  void invalidJwtReturns401() throws Exception {
    when(jwt.userId("invalid")).thenThrow(new BadJwtException("invalid"));
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_INVALID_TOKEN"));
  }

  @Test
  void lockedUserWithExistingTokenReturns401() throws Exception {
    when(jwt.userId("valid")).thenReturn(1L);
    when(details.loadActiveUser(1L)).thenThrow(new BadCredentialsException("locked"));
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer valid"))
        .andExpect(status().isUnauthorized());
  }

  void asUser() {
    when(jwt.userId("valid")).thenReturn(7L);
    when(details.loadActiveUser(7L)).thenReturn(new CurrentUser(7L, Role.USER));
  }

  @Test
  void profileUsesServerResolvedUser() throws Exception {
    asUser();
    when(users.me(7L))
        .thenReturn(new UserResponse(7L, "Name", "a@example.com", null, null, Role.USER));
    mvc.perform(get("/api/users/me").header("Authorization", "Bearer valid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(7));
    verify(users).me(7L);
  }

  @Test
  void userCannotAccessAdminOrInspectorRoutes() throws Exception {
    asUser();
    mvc.perform(get("/api/admin/users").header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/inspector/inspections").header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
  }

  @Test
  void logoutDoesNotClaimToRevokeToken() throws Exception {
    asUser();
    mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer valid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
    verify(jwt, never()).issue(any());
  }

  @Test
  void allowedCorsPreflightPasses() throws Exception {
    mvc.perform(
            options("/api/users/me")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }

  @Test
  void publicFrontendCorsPreflightPasses() throws Exception {
    mvc.perform(
            options("/api/auth/login")
                .header("Origin", "https://ngdhiep-fithau.id.vn")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://ngdhiep-fithau.id.vn"));
  }

  @Test
  void unlistedCorsOriginRejected() throws Exception {
    mvc.perform(
            options("/api/users/me")
                .header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isForbidden());
  }
}
