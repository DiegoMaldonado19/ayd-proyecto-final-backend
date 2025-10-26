package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TemporalPermitDtoMapperTest {

    private TemporalPermitDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TemporalPermitDtoMapper(new ObjectMapper());
    }

    @Test
    void toResponse_ShouldMapEntityToResponse() {
        VehicleTypeEntity vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        TemporalPermitStatusTypeEntity status = TemporalPermitStatusTypeEntity.builder()
                .id(1)
                .code("ACTIVE")
                .name("Activo")
                .build();

        TemporalPermitEntity entity = TemporalPermitEntity.builder()
                .id(1L)
                .subscriptionId(100L)
                .temporalPlate("ABC123")
                .startDate(LocalDateTime.of(2025, 10, 1, 8, 0))
                .endDate(LocalDateTime.of(2025, 10, 15, 8, 0))
                .maxUses(10)
                .currentUses(3)
                .allowedBranches("[1,2,3]")
                .vehicleTypeId(1)
                .vehicleType(vehicleType)
                .statusTypeId(1)
                .status(status)
                .approvedBy(5L)
                .createdAt(LocalDateTime.of(2025, 10, 1, 7, 0))
                .updatedAt(LocalDateTime.of(2025, 10, 1, 7, 0))
                .build();

        UserEntity approver = UserEntity.builder()
                .id(5L)
                .firstName("Carlos")
                .lastName("Rodriguez")
                .build();

        TemporalPermitResponse response = mapper.toResponse(entity, approver);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSubscriptionId()).isEqualTo(100L);
        assertThat(response.getTemporalPlate()).isEqualTo("ABC123");
        assertThat(response.getMaxUses()).isEqualTo(10);
        assertThat(response.getCurrentUses()).isEqualTo(3);
        assertThat(response.getAllowedBranches()).containsExactly(1L, 2L, 3L);
        assertThat(response.getVehicleType()).isEqualTo("Cuatro Ruedas");
        assertThat(response.getStatus()).isEqualTo("Activo");
        assertThat(response.getApprovedBy()).isEqualTo("Carlos Rodriguez");
    }

    @Test
    void toResponse_WithNullApprover_ShouldHandleGracefully() {
        VehicleTypeEntity vehicleType = VehicleTypeEntity.builder()
                .id(1)
                .name("Dos Ruedas")
                .build();

        TemporalPermitStatusTypeEntity status = TemporalPermitStatusTypeEntity.builder()
                .id(2)
                .name("Revocado")
                .build();

        TemporalPermitEntity entity = TemporalPermitEntity.builder()
                .id(2L)
                .subscriptionId(200L)
                .temporalPlate("XYZ789")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .maxUses(5)
                .currentUses(0)
                .vehicleType(vehicleType)
                .status(status)
                .approvedBy(10L)
                .build();

        TemporalPermitResponse response = mapper.toResponse(entity, null);

        assertThat(response).isNotNull();
        assertThat(response.getApprovedBy()).isNull();
    }

    @Test
    void serializeAllowedBranches_ShouldConvertListToJson() {
        List<Long> branches = Arrays.asList(1L, 2L, 3L, 4L);

        String json = mapper.serializeAllowedBranches(branches);

        assertThat(json).isNotNull();
        assertThat(json).contains("1", "2", "3", "4");
    }

    @Test
    void serializeAllowedBranches_WithNullList_ShouldReturnNull() {
        String json = mapper.serializeAllowedBranches(null);

        assertThat(json).isNull();
    }

    @Test
    void serializeAllowedBranches_WithEmptyList_ShouldReturnNull() {
        String json = mapper.serializeAllowedBranches(Arrays.asList());

        assertThat(json).isNull();
    }
}
