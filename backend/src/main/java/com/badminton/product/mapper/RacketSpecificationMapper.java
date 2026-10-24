package com.badminton.product.mapper;

import com.badminton.product.dto.response.RacketSpecificationResponse;
import com.badminton.product.entity.RacketSpecification;

public final class RacketSpecificationMapper {
  private RacketSpecificationMapper() {}

  public static RacketSpecificationResponse toResponse(RacketSpecification e) {
    return new RacketSpecificationResponse(
        e.getSpecificationId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getWeightClass(),
        e.getBalanceType(),
        e.getBalancePointMm(),
        e.getShaftFlexibility(),
        e.getMaxTensionLbs(),
        e.getPlayStyle(),
        e.getSkillLevel(),
        e.getGripSize(),
        e.getRacketLengthMm(),
        e.getMaterial(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
