package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleDtoMapperTest {

    private VehicleDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VehicleDtoMapper();
    }

    @Test
    void toResponse_shouldMapCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(1L);
        vehicle.setUserId(100L);
        vehicle.setLicensePlate("P-123ABC");
        vehicle.setVehicleTypeId(1);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setColor("Blanco");
        vehicle.setYear(2023);
        vehicle.setIsActive(true);
        vehicle.setCreatedAt(now);
        vehicle.setUpdatedAt(now);

        VehicleTypeEntity vehicleType = new VehicleTypeEntity();
        vehicleType.setId(1);
        vehicleType.setCode("2R");
        vehicleType.setName("Dos ruedas");

        VehicleResponse result = mapper.toResponse(vehicle, vehicleType);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getLicensePlate()).isEqualTo("P-123ABC");
        assertThat(result.getBrand()).isEqualTo("Toyota");
        assertThat(result.getModel()).isEqualTo("Corolla");
        assertThat(result.getColor()).isEqualTo("Blanco");
        assertThat(result.getYear()).isEqualTo(2023);
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);

        assertThat(result.getVehicleType()).isNotNull();
        assertThat(result.getVehicleType().getId()).isEqualTo(1);
        assertThat(result.getVehicleType().getCode()).isEqualTo("2R");
        assertThat(result.getVehicleType().getName()).isEqualTo("Dos ruedas");
    }

    @Test
    void toResponse_withNullVehicle_shouldReturnNull() {
        VehicleResponse result = mapper.toResponse(null, new VehicleTypeEntity());
        assertThat(result).isNull();
    }

    @Test
    void toResponse_withNullVehicleType_shouldMapVehicleOnly() {
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setId(1L);
        vehicle.setUserId(100L);
        vehicle.setLicensePlate("P-123ABC");
        vehicle.setBrand("Toyota");

        VehicleResponse result = mapper.toResponse(vehicle, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVehicleType()).isNull();
    }

    @Test
    void toVehicleTypeResponse_shouldMapCorrectly() {
        VehicleTypeEntity vehicleType = new VehicleTypeEntity();
        vehicleType.setId(2);
        vehicleType.setCode("4R");
        vehicleType.setName("Cuatro ruedas");

        VehicleTypeResponse result = mapper.toVehicleTypeResponse(vehicleType);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getCode()).isEqualTo("4R");
        assertThat(result.getName()).isEqualTo("Cuatro ruedas");
    }

    @Test
    void toVehicleTypeResponse_withNullInput_shouldReturnNull() {
        VehicleTypeResponse result = mapper.toVehicleTypeResponse(null);
        assertThat(result).isNull();
    }
}
