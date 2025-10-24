package com.ayd.parkcontrol.infrastructure.persistence.mapper;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchEntity toEntity(Branch branch) {
        if (branch == null) {
            return null;
        }

        return BranchEntity.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .openingTime(branch.getOpeningTime())
                .closingTime(branch.getClosingTime())
                .capacity2r(branch.getCapacity2r())
                .capacity4r(branch.getCapacity4r())
                .ratePerHour(branch.getRatePerHour())
                .isActive(branch.getIsActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }

    public Branch toDomain(BranchEntity entity) {
        if (entity == null) {
            return null;
        }

        return Branch.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .openingTime(entity.getOpeningTime())
                .closingTime(entity.getClosingTime())
                .capacity2r(entity.getCapacity2r())
                .capacity4r(entity.getCapacity4r())
                .ratePerHour(entity.getRatePerHour())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
