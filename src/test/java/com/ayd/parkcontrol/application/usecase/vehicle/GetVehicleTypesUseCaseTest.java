package com.ayd.parkcontrol.application.usecase.vehicle;

import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.application.mapper.VehicleDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.VehicleTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaVehicleTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetVehicleTypesUseCaseTest {

    @Mock
    private JpaVehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleDtoMapper mapper;

    @InjectMocks
    private GetVehicleTypesUseCase getVehicleTypesUseCase;

    @Test
    void execute_shouldReturnAllVehicleTypes() {
        VehicleTypeEntity type1 = VehicleTypeEntity.builder().id(1).code("2R").name("Dos Ruedas").build();
        VehicleTypeEntity type2 = VehicleTypeEntity.builder().id(2).code("4R").name("Cuatro Ruedas").build();

        when(vehicleTypeRepository.findAll()).thenReturn(Arrays.asList(type1, type2));
        when(mapper.toVehicleTypeResponse(any(VehicleTypeEntity.class)))
                .thenReturn(VehicleTypeResponse.builder().build());

        List<VehicleTypeResponse> result = getVehicleTypesUseCase.execute();

        assertThat(result).hasSize(2);
        verify(vehicleTypeRepository).findAll();
        verify(mapper, times(2)).toVehicleTypeResponse(any(VehicleTypeEntity.class));
    }
}
