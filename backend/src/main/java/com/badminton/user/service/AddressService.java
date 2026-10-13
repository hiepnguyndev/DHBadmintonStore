package com.badminton.user.service;

import com.badminton.exception.BusinessException;
import com.badminton.user.dto.request.AddressRequest;
import com.badminton.user.dto.response.AddressResponse;
import com.badminton.user.entity.*;
import com.badminton.user.mapper.UserMapper;
import com.badminton.user.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
  private final UserRepository users;
  private final AddressRepository addresses;
  private final UserMapper mapper;

  public AddressService(UserRepository users, AddressRepository addresses, UserMapper mapper) {
    this.users = users;
    this.addresses = addresses;
    this.mapper = mapper;
  }

  private User lockOwner(Long userId) {
    User u =
        users
            .findByIdForUpdate(userId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));
    if (u.getStatus() != UserStatus.ACTIVE)
      throw new BusinessException(HttpStatus.FORBIDDEN, "USER_LOCKED", "Tài khoản không khả dụng");
    return u;
  }

  private Address owned(Long userId, Long id) {
    return addresses
        .findByAddressIdAndUserUserId(id, userId)
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ"));
  }

  @Transactional(readOnly = true)
  public List<AddressResponse> list(Long userId) {
    return addresses.findAllByUserUserIdOrderByAddressIdDesc(userId).stream()
        .map(mapper::toResponse)
        .toList();
  }

  private void apply(Long userId, Address a, AddressRequest r) {
    // The user row serializes writes even when no address exists yet.
    if (r.isDefault())
      addresses.findAllByUserUserIdOrderByAddressIdDesc(userId).forEach(Address::clearDefault);
    a.update(
        r.receiverName().trim(),
        r.receiverPhone().trim(),
        r.province().trim(),
        "",
        r.ward().trim(),
        r.detailAddress().trim(),
        r.isDefault());
  }

  @Transactional
  public AddressResponse create(Long userId, AddressRequest r) {
    Address a = new Address(lockOwner(userId));
    apply(userId, a, r);
    return mapper.toResponse(addresses.saveAndFlush(a));
  }

  @Transactional
  public AddressResponse update(Long userId, Long id, AddressRequest r) {
    lockOwner(userId);
    Address a = owned(userId, id);
    apply(userId, a, r);
    return mapper.toResponse(a);
  }

  @Transactional
  public void delete(Long userId, Long id) {
    lockOwner(userId);
    addresses.delete(owned(userId, id));
  }
}
