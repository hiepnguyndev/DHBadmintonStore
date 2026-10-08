package com.badminton.user.dto.response;

public record AddressResponse(
    Long addressId,
    String receiverName,
    String receiverPhone,
    String province,
    String district,
    String ward,
    String detailAddress,
    boolean isDefault) {}
