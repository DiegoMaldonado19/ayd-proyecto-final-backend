package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.domain.model.rate.RateBranch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class RateDtoMapper {

    public RateBase toDomain(RateBaseHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return RateBase.builder()
                .id(entity.getId())
                .amountPerHour(entity.getAmountPerHour())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public RateBaseResponse toRateBaseResponse(RateBase domain) {
        if (domain == null) {
            return null;
        }
        return RateBaseResponse.builder()
                .id(domain.getId())
                .amountPerHour(domain.getAmountPerHour())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .isActive(domain.getIsActive())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public RateBranch toDomain(BranchEntity entity) {
        if (entity == null) {
            return null;
        }
        return RateBranch.builder()
                .branchId(entity.getId())
                .branchName(entity.getName())
                .ratePerHour(entity.getRatePerHour())
                .isActive(entity.getIsActive())
                .build();
    }

    public RateBranchResponse toRateBranchResponse(RateBranch domain) {
        if (domain == null) {
            return null;
        }
        return RateBranchResponse.builder()
                .branchId(domain.getBranchId())
                .branchName(domain.getBranchName())
                .ratePerHour(domain.getRatePerHour())
                .isActive(domain.getIsActive())
                .build();
    }
}
