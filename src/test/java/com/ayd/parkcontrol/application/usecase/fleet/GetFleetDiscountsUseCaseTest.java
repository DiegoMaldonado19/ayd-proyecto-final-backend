package com.ayd.parkcontrol.application.usecase.fleet;

import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFleetDiscountsUseCaseTest {

    @Mock
    private JpaFleetCompanyRepository fleetCompanyRepository;

    @Mock
    private FleetDtoMapper fleetDtoMapper;

    @InjectMocks
    private GetFleetDiscountsUseCase getFleetDiscountsUseCase;

    private FleetCompanyEntity company;
    private FleetDiscountsResponse expectedResponse;

    @BeforeEach
    void setUp() {
        company = FleetCompanyEntity.builder()
                .id(1L)
                .name("Test Company")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .build();

        expectedResponse = FleetDiscountsResponse.builder()
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .maxTotalDiscount(new BigDecimal("35.00"))
                .build();
    }

    @Test
    void shouldGetFleetDiscountsSuccessfully() {
        // Given
        Long companyId = 1L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(fleetDtoMapper.toDiscountsResponse(company)).thenReturn(expectedResponse);

        // When
        FleetDiscountsResponse result = getFleetDiscountsUseCase.execute(companyId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCorporateDiscountPercentage()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getMaxTotalDiscount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        Long companyId = 999L;
        when(fleetCompanyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getFleetDiscountsUseCase.execute(companyId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
