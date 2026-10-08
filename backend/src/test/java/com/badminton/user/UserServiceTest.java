package com.badminton.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badminton.user.dto.request.*;
import com.badminton.user.entity.User;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.UserRepository;
import com.badminton.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock UserRepository users;
  final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
  UserService service;
  User user;

  @BeforeEach
  void setup() {
    service = new UserService(users, new UserMapper(), encoder);
    user = User.register("Name", "a@example.com", encoder.encode("old-pass-123"), null);
    when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
  }

  @Test
  void wrongCurrentPasswordLeavesHashUnchanged() {
    String before = user.getPasswordHash();
    assertThatThrownBy(
            () ->
                service.changePassword(
                    1L, new ChangePasswordRequest("incorrect", "new-pass-123", "new-pass-123")))
        .hasFieldOrPropertyWithValue("code", "CURRENT_PASSWORD_INVALID");
    assertThat(user.getPasswordHash()).isEqualTo(before);
  }

  @Test
  void changesPasswordUsingHash() {
    service.changePassword(
        1L, new ChangePasswordRequest("old-pass-123", "new-pass-123", "new-pass-123"));
    assertThat(encoder.matches("new-pass-123", user.getPasswordHash())).isTrue();
    assertThat(encoder.matches("old-pass-123", user.getPasswordHash())).isFalse();
  }

  @Test
  void updatingProfilePreservesRoleEmailAndStatus() {
    var beforeRole = user.getRole();
    var beforeStatus = user.getStatus();
    service.update(1L, new UpdateUserRequest("New Name", "0901234567", null));
    assertThat(user.getFullName()).isEqualTo("New Name");
    assertThat(user.getEmail()).isEqualTo("a@example.com");
    assertThat(user.getRole()).isEqualTo(beforeRole);
    assertThat(user.getStatus()).isEqualTo(beforeStatus);
  }
}
