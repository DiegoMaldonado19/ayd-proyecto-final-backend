package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.CreateFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFleetUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private CreateFleetUseCase createFleetUseCase;

    private CreateFleetRequest validRequest;
    private FleetCompanyEntity companyEntity;
    private FleetResponse fleetResponse;

    @BeforeEach
    void setUp() {
        validRequest = CreateFleetRequest.builder()
                .name("Test Fleet Company")
                .taxId("12345678-9")
                .contactName("John Doe")
                .corporateEmail("contact@testfleet.com")
                .phone("50212345678")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(20)
                .billingPeriod("MONTHLY")
                .build();

        companyEntity = FleetCompanyEntity.builder()
                .id(1L)
                .name(validRequest.getName())
                .taxId(validRequest.getTaxId())
                .contactName(validRequest.getContactName())
                .corporateEmail(validRequest.getCorporateEmail())
                .phone(validRequest.getPhone())
                .corporateDiscountPercentage(validRequest.getCorporateDiscountPercentage())
                .plateLimit(validRequest.getPlateLimit())
                .billingPeriod(validRequest.getBillingPeriod())
                .monthsUnpaid(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        fleetResponse = FleetResponse.builder()
                .id(1L)
                .name(validRequest.getName())
                .taxId(validRequest.getTaxId())
                .contactName(validRequest.getContactName())
                .corporateEmail(validRequest.getCorporateEmail())
                .phone(validRequest.getPhone())
                .corporateDiscountPercentage(validRequest.getCorporateDiscountPercentage())
                .plateLimit(validRequest.getPlateLimit())
                .billingPeriod(validRequest.getBillingPeriod())
                .monthsUnpaid(0)
                .isActive(true)
                .createdAt(companyEntity.getCreatedAt())
                .activeVehiclesCount(0L)
                .build();
    }

    @Test
    void shouldCreateFleetSuccessfully() {
        when(fleetCompanyRepository.existsByTaxId(validRequest.getTaxId())).thenReturn(false);
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(companyEntity);
        when(fleetDtoMapper.toResponse(any(FleetCompanyEntity.class), anyLong())).thenReturn(fleetResponse);

        FleetResponse result = createFleetUseCase.execute(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(validRequest.getName());
        assertThat(result.getTaxId()).isEqualTo(validRequest.getTaxId());
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getActiveVehiclesCount()).isEqualTo(0L);

        verify(fleetCompanyRepository).existsByTaxId(validRequest.getTaxId());
        verify(fleetCompanyRepository).save(any(FleetCompanyEntity.class));
        verify(fleetDtoMapper).toResponse(any(FleetCompanyEntity.class), eq(0L));
    }

    @Test
    void shouldThrowExceptionWhenTaxIdAlreadyExists() {
        when(fleetCompanyRepository.existsByTaxId(validRequest.getTaxId())).thenReturn(true);

        assertThatThrownBy(() -> createFleetUseCase.execute(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(fleetCompanyRepository).existsByTaxId(validRequest.getTaxId());
        verify(fleetCompanyRepository, never()).save(any(FleetCompanyEntity.class));
        verify(fleetDtoMapper, never()).toResponse(any(), anyLong());
    }

    @Test
    void shouldSetDefaultValuesCorrectly() {
        when(fleetCompanyRepository.existsByTaxId(anyString())).thenReturn(false);
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenAnswer(invocation -> {
            FleetCompanyEntity saved = invocation.getArgument(0);
            assertThat(saved.getMonthsUnpaid()).isEqualTo(0);
            assertThat(saved.getIsActive()).isTrue();
            return companyEntity;
        });
        when(fleetDtoMapper.toResponse(any(FleetCompanyEntity.class), anyLong())).thenReturn(fleetResponse);

        FleetResponse result = createFleetUseCase.execute(validRequest);

        assertThat(result).isNotNull();
        verify(fleetCompanyRepository).save(any(FleetCompanyEntity.class));
    }
}
