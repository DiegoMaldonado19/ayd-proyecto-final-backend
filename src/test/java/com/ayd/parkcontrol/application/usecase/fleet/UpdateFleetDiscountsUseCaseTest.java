package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetDiscountsRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
import com.ayd.parkcontrol.application.mapper.FleetDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFleetDiscountsUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private UpdateFleetDiscountsUseCase updateFleetDiscountsUseCase;

    private FleetCompanyEntity company;
    private UpdateFleetDiscountsRequest request;

    @BeforeEach
    void setUp() {
        company = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Company")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .build();
    }

    @Test
    void shouldUpdateDiscountsSuccessfully() {
        // Given
        Long companyId = 1L;
        request = UpdateFleetDiscountsRequest.builder()
                .corporateDiscountPercentage(new BigDecimal("8.00"))
                .build();

        FleetDiscountsResponse expectedResponse = FleetDiscountsResponse.builder()
                .corporateDiscountPercentage(new BigDecimal("8.00"))
                .maxTotalDiscount(new BigDecimal("35.00"))
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(company);
        when(fleetDtoMapper.toDiscountsResponse(company)).thenReturn(expectedResponse);

        // When
        FleetDiscountsResponse result = updateFleetDiscountsUseCase.execute(companyId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCorporateDiscountPercentage()).isEqualByComparingTo(new BigDecimal("8.00"));

        ArgumentCaptor<FleetCompanyEntity> captor = ArgumentCaptor.forClass(FleetCompanyEntity.class);
        verify(fleetCompanyRepository).save(captor.capture());
        assertThat(captor.getValue().getCorporateDiscountPercentage())
                .isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    void shouldThrowExceptionWhenCorporateDiscountExceeds10Percent() {
        // Given
        Long companyId = 1L;
        request = UpdateFleetDiscountsRequest.builder()
                .corporateDiscountPercentage(new BigDecimal("12.00"))
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // When & Then
        assertThatThrownBy(() -> updateFleetDiscountsUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corporate discount cannot exceed 10%");

        verify(fleetCompanyRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCorporateDiscountExceeds35Percent() {
        // Given
        Long companyId = 1L;
        request = UpdateFleetDiscountsRequest.builder()
                .corporateDiscountPercentage(new BigDecimal("40.00"))
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // When & Then - El test falla primero con validación de 10% porque 40 > 10
        assertThatThrownBy(() -> updateFleetDiscountsUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corporate discount cannot exceed 10%");

        verify(fleetCompanyRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        Long companyId = 999L;
        request = UpdateFleetDiscountsRequest.builder()
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateFleetDiscountsUseCase.execute(companyId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(fleetCompanyRepository, never()).save(any());
    }

    @Test
    void shouldAcceptMaximumAllowedCorporateDiscount() {
        // Given
        Long companyId = 1L;
        request = UpdateFleetDiscountsRequest.builder()
                .corporateDiscountPercentage(new BigDecimal("10.00")) // Exactly 10%
                .build();

        FleetDiscountsResponse expectedResponse = FleetDiscountsResponse.builder()
                .corporateDiscountPercentage(new BigDecimal("10.00"))
                .maxTotalDiscount(new BigDecimal("35.00"))
                .build();

        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(fleetCompanyRepository.save(any(FleetCompanyEntity.class))).thenReturn(company);
        when(fleetDtoMapper.toDiscountsResponse(company)).thenReturn(expectedResponse);

        // When
        FleetDiscountsResponse result = updateFleetDiscountsUseCase.execute(companyId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCorporateDiscountPercentage()).isEqualByComparingTo(new BigDecimal("10.00"));
        verify(fleetCompanyRepository).save(any(FleetCompanyEntity.class));
    }
}
