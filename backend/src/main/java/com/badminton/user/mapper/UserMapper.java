package com.badminton.user.mapper;

import com.badminton.user.dto.response.*;
import com.badminton.user.entity.*;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserResponse toResponse(User u) {
    return new UserResponse(
        u.getUserId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAvatarUrl(), u.getRole(),
        u.getDateOfBirth(), u.getGender(), u.getHeightCm(), u.getWeightKg());
  }

  public AddressResponse toResponse(Address a) {
    return new AddressResponse(
        a.getAddressId(),
        a.getReceiverName(),
        a.getReceiverPhone(),
        a.getProvince(),
        a.getWard(),
        a.getDetailAddress(),
        a.isDefaultAddress());
  }
}
