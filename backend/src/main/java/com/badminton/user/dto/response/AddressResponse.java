package com.badminton.user.dto.response;

public record AddressResponse(
    Long addressId,
    String receiverName,
    String receiverPhone,
    String province,
    String ward,
    String detailAddress,
    boolean isDefault) {}
