package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.domain.model.rate.RateBase;
import com.ayd.parkcontrol.domain.model.rate.RateBranch;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.RateBaseHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RateDtoMapperTest {

    private RateDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RateDtoMapper();
    }

    @Test
    void toDomain_fromRateBaseHistoryEntity_shouldMapCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        LocalDateTime createdAt = LocalDateTime.now();

        UserEntity createdByUser = new UserEntity();
        createdByUser.setId(100L);

        RateBaseHistoryEntity entity = new RateBaseHistoryEntity();
        entity.setId(1L);
        entity.setAmountPerHour(new BigDecimal("12.50"));
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setIsActive(true);
        entity.setCreatedBy(createdByUser);
        entity.setCreatedAt(createdAt);

        RateBase result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmountPerHour()).isEqualTo(new BigDecimal("12.50"));
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(100L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toDomain_withNullRateBaseHistoryEntity_shouldReturnNull() {
        RateBase result = mapper.toDomain((RateBaseHistoryEntity) null);
        assertThat(result).isNull();
    }

    @Test
    void toDomain_withNullCreatedBy_shouldMapWithoutCreatedBy() {
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);

        RateBaseHistoryEntity entity = new RateBaseHistoryEntity();
        entity.setId(1L);
        entity.setAmountPerHour(new BigDecimal("12.50"));
        entity.setStartDate(startDate);
        entity.setCreatedBy(null);

        RateBase result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCreatedBy()).isNull();
    }

    @Test
    void toRateBaseResponse_shouldMapCorrectly() {
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        LocalDateTime createdAt = LocalDateTime.now();

        RateBase domain = RateBase.builder()
                .id(1L)
                .amountPerHour(new BigDecimal("15.00"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .createdBy(200L)
                .createdAt(createdAt)
                .build();

        RateBaseResponse result = mapper.toRateBaseResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAmountPerHour()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(200L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toRateBaseResponse_withNullInput_shouldReturnNull() {
        RateBaseResponse result = mapper.toRateBaseResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toDomain_fromBranchEntity_shouldMapCorrectly() {
        BranchEntity entity = new BranchEntity();
        entity.setId(5L);
        entity.setName("Sucursal Sur");
        entity.setRatePerHour(new BigDecimal("18.00"));
        entity.setIsActive(true);

        RateBranch result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(5L);
        assertThat(result.getBranchName()).isEqualTo("Sucursal Sur");
        assertThat(result.getRatePerHour()).isEqualTo(new BigDecimal("18.00"));
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void toDomain_withNullBranchEntity_shouldReturnNull() {
        RateBranch result = mapper.toDomain((BranchEntity) null);
        assertThat(result).isNull();
    }

    @Test
    void toRateBranchResponse_shouldMapCorrectly() {
        RateBranch domain = RateBranch.builder()
                .branchId(10L)
                .branchName("Sucursal Centro")
                .ratePerHour(new BigDecimal("20.00"))
                .isActive(true)
                .build();

        RateBranchResponse result = mapper.toRateBranchResponse(domain);

        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(10L);
        assertThat(result.getBranchName()).isEqualTo("Sucursal Centro");
        assertThat(result.getRatePerHour()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void toRateBranchResponse_withNullInput_shouldReturnNull() {
        RateBranchResponse result = mapper.toRateBranchResponse(null);
        assertThat(result).isNull();
    }
}
