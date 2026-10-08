package com.badminton.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badminton.auth.dto.request.*;
import com.badminton.auth.service.AuthService;
import com.badminton.exception.BusinessException;
import com.badminton.security.JwtService;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock UserRepository users;
  @Mock JwtService jwt;
  final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder(4);
  AuthService service;

  @BeforeEach
  void setup() {
    service = new AuthService(users, passwords, jwt, new UserMapper());
  }

  RegisterRequest request(String email) {
    return new RegisterRequest(
        " Nguyen Van A ", email, null, "Password123!", "Password123!");
  }

  @Test
  void registrationNormalizesEmailHashesPasswordAndForcesUserRole() {
    when(users.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              User u = i.getArgument(0);
              ReflectionTestUtils.setField(u, "userId", 1L);
              return u;
            });
    var response = service.register(request("A@EXAMPLE.COM"));
    var captor = ArgumentCaptor.forClass(User.class);
    verify(users).saveAndFlush(captor.capture());
    User u = captor.getValue();
    assertThat(response.email()).isEqualTo("a@example.com");
    assertThat(u.getFullName()).isEqualTo("Nguyen Van A");
    assertThat(passwords.matches("Password123!", u.getPasswordHash())).isTrue();
    assertThat(u.getPasswordHash()).isNotEqualTo("Password123!");
    assertThat(u.getRole()).isEqualTo(Role.USER);
    assertThat(u.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void duplicateEmailRejected() {
    when(users.existsByEmail("a@example.com")).thenReturn(true);
    assertThatThrownBy(() -> service.register(request("a@example.com")))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("code", "EMAIL_ALREADY_EXISTS");
    verify(users, never()).saveAndFlush(any());
  }

  @Test
  void mismatchedConfirmationRejected() {
    assertThatThrownBy(
            () ->
                service.register(
                    new RegisterRequest("Name", "a@example.com", null, "Password123!", "wrong")))
        .hasFieldOrPropertyWithValue("code", "PASSWORD_MISMATCH");
    verifyNoInteractions(users);
  }

  @Test
  void utf8PasswordOver72BytesRejected() {
    String password = "ắ".repeat(30);
    assertThatThrownBy(
            () ->
                service.register(
                    new RegisterRequest("Name", "a@example.com", null, password, password)))
        .hasFieldOrPropertyWithValue("code", "PASSWORD_LENGTH");
  }

  User user() {
    User u = User.register("Name", "a@example.com", passwords.encode("Password123!"), null);
    ReflectionTestUtils.setField(u, "userId", 1L);
    return u;
  }

  @Test
  void loginReturnsAccessTokenOnlyAndSafeUser() {
    User u = user();
    when(users.findByEmail("a@example.com")).thenReturn(Optional.of(u));
    when(jwt.issue(u)).thenReturn("access-token");
    var result = service.login(new LoginRequest("A@EXAMPLE.COM", "Password123!"));
    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.user().id()).isEqualTo(1L);
  }

  @Test
  void wrongPasswordRejected() {
    when(users.findByEmail("a@example.com")).thenReturn(Optional.of(user()));
    assertThatThrownBy(() -> service.login(new LoginRequest("a@example.com", "wrongpass")))
        .hasFieldOrPropertyWithValue("code", "AUTH_INVALID_CREDENTIALS");
    verifyNoInteractions(jwt);
  }

  @Test
  void missingUserUsesSameCredentialsError() {
    when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "wrongpass")))
        .hasFieldOrPropertyWithValue("code", "AUTH_INVALID_CREDENTIALS");
  }

  @Test
  void lockedSuspendedDeletedUsersCannotLogin() {
    for (UserStatus status :
        new UserStatus[] {UserStatus.LOCKED, UserStatus.SUSPENDED, UserStatus.DELETED}) {
      User u = user();
      ReflectionTestUtils.setField(u, "status", status);
      when(users.findByEmail("a@example.com")).thenReturn(Optional.of(u));
      assertThatThrownBy(() -> service.login(new LoginRequest("a@example.com", "Password123!")))
          .hasFieldOrPropertyWithValue("code", "USER_LOCKED");
    }
    verifyNoInteractions(jwt);
  }
}
