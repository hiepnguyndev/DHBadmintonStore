package com.badminton.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badminton.user.dto.request.AddressRequest;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.*;
import com.badminton.user.service.AddressService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
  @Mock UserRepository users;
  @Mock AddressRepository addresses;
  AddressService service;
  User owner;

  @BeforeEach
  void setup() {
    service = new AddressService(users, addresses, new UserMapper());
    owner = User.register("Name", "a@example.com", "hash", null);
  }

  AddressRequest request() {
    return new AddressRequest("Name", "0901234567", "HN", "HD", "ML", "123 ABC", true);
  }

  @Test
  void cannotUpdateSomeoneElsesAddress() {
    when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
    when(addresses.findByAddressIdAndUserUserId(99L, 1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.update(1L, 99L, request()))
        .hasFieldOrPropertyWithValue("code", "ADDRESS_NOT_FOUND");
    verify(addresses, never()).saveAndFlush(any());
  }

  @Test
  void cannotDeleteSomeoneElsesAddress() {
    when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
    when(addresses.findByAddressIdAndUserUserId(99L, 1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.delete(1L, 99L))
        .hasFieldOrPropertyWithValue("code", "ADDRESS_NOT_FOUND");
    verify(addresses, never()).delete(any());
  }

  @Test
  void newDefaultClearsOldDefaultUnderOwnerLock() {
    when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
    Address old = new Address(owner);
    old.update("N", "123", "P", "D", "W", "A", true);
    when(addresses.findAllByUserUserIdOrderByAddressIdDesc(1L)).thenReturn(List.of(old));
    when(addresses.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    var result = service.create(1L, request());
    assertThat(old.isDefaultAddress()).isFalse();
    assertThat(result.isDefault()).isTrue();
    InOrder order = inOrder(users, addresses);
    order.verify(users).findByIdForUpdate(1L);
    order.verify(addresses).findAllByUserUserIdOrderByAddressIdDesc(1L);
    order.verify(addresses).saveAndFlush(any());
  }
}
