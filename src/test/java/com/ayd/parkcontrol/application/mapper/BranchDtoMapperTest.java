package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.branch.BranchCapacityResponse;
import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.dto.response.branch.BranchScheduleResponse;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class BranchDtoMapperTest {

    private BranchDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BranchDtoMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Central")
                .address("Av. Principal 123")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(18, 0))
                .capacity2r(50)
                .capacity4r(100)
                .ratePerHour(new BigDecimal("10.00"))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        BranchResponse result = mapper.toResponse(branch);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sucursal Central");
        assertThat(result.getAddress()).isEqualTo("Av. Principal 123");
        assertThat(result.getOpeningTime()).isEqualTo("08:00");
        assertThat(result.getClosingTime()).isEqualTo("18:00");
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(100);
        assertThat(result.getRatePerHour()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toResponse_withNullInput_shouldReturnNull() {
        BranchResponse result = mapper.toResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toResponse_withNullTimes_shouldMapWithoutTimes() {
        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Central")
                .openingTime(null)
                .closingTime(null)
                .capacity2r(50)
                .capacity4r(100)
                .build();

        BranchResponse result = mapper.toResponse(branch);

        assertThat(result).isNotNull();
        assertThat(result.getOpeningTime()).isNull();
        assertThat(result.getClosingTime()).isNull();
    }

    @Test
    void toCapacityResponse_shouldMapCorrectly() {
        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Central")
                .capacity2r(50)
                .capacity4r(100)
                .build();

        BranchCapacityResponse result = mapper.toCapacityResponse(branch);

        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(1L);
        assertThat(result.getBranchName()).isEqualTo("Sucursal Central");
        assertThat(result.getCapacity2r()).isEqualTo(50);
        assertThat(result.getCapacity4r()).isEqualTo(100);
        assertThat(result.getTotalCapacity()).isEqualTo(150);
    }

    @Test
    void toCapacityResponse_withNullInput_shouldReturnNull() {
        BranchCapacityResponse result = mapper.toCapacityResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toScheduleResponse_shouldMapCorrectly() {
        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Central")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(18, 0))
                .build();

        BranchScheduleResponse result = mapper.toScheduleResponse(branch);

        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(1L);
        assertThat(result.getBranchName()).isEqualTo("Sucursal Central");
        assertThat(result.getOpeningTime()).isEqualTo("08:00");
        assertThat(result.getClosingTime()).isEqualTo("18:00");
        assertThat(result.getIsOpenNow()).isNotNull(); // Depends on current time
    }

    @Test
    void toScheduleResponse_withNullInput_shouldReturnNull() {
        BranchScheduleResponse result = mapper.toScheduleResponse(null);
        assertThat(result).isNull();
    }

    @Test
    void toScheduleResponse_withNullTimes_shouldMapWithIsOpenFalse() {
        Branch branch = Branch.builder()
                .id(1L)
                .name("Sucursal Central")
                .openingTime(null)
                .closingTime(null)
                .build();

        BranchScheduleResponse result = mapper.toScheduleResponse(branch);

        assertThat(result).isNotNull();
        assertThat(result.getOpeningTime()).isNull();
        assertThat(result.getClosingTime()).isNull();
        assertThat(result.getIsOpenNow()).isFalse();
    }
}
