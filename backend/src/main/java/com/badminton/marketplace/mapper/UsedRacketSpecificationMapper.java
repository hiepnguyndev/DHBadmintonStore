package com.badminton.marketplace.mapper;

import com.badminton.marketplace.dto.response.UsedRacketSpecificationResponse;
import com.badminton.marketplace.entity.UsedRacketSpecification;

public final class UsedRacketSpecificationMapper {
  private UsedRacketSpecificationMapper() {}

  public static UsedRacketSpecificationResponse toResponse(UsedRacketSpecification e) {
    return new UsedRacketSpecificationResponse(
        e.getUsedSpecId(),
        e.getListing() == null ? null : e.getListing().getListingId(),
        e.getWeightClass(),
        e.getGripSize(),
        e.getStringModel(),
        e.getStringTensionLbs(),
        e.getFrameCondition(),
        e.getPaintCondition(),
        e.getGrommetCondition(),
        e.getHandleCondition(),
        e.getRepairHistory(),
        e.getSerialNumber(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
