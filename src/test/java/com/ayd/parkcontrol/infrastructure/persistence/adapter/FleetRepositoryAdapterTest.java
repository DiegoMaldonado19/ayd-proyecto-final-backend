package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.FleetCompanyEntityMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetRepositoryAdapterTest {

    @Mock
    private JpaFleetCompanyRepository jpaFleetCompanyRepository;

    @Mock
    private FleetCompanyEntityMapper fleetCompanyEntityMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FleetRepositoryAdapter fleetRepositoryAdapter;

    private FleetCompany fleetCompany;
    private FleetCompanyEntity fleetCompanyEntity;

    @BeforeEach
    void setUp() {
        fleetCompany = FleetCompany.builder()
                .id(1L)
                .name("Test Fleet")
                .taxId("123456789")
                .contactName("John Doe")
                .corporateEmail("test@fleet.com")
                .contactPhone("1234567890")
                .corporateDiscountPercentage(new BigDecimal("5.00"))
                .plateLimit(10)
                .billingPeriod("MONTHLY")
                .monthsUnpaid(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        fleetCompanyEntity = new FleetCompanyEntity();
        fleetCompanyEntity.setId(1L);
        fleetCompanyEntity.setName("Test Fleet");
        fleetCompanyEntity.setTaxId("123456789");
        fleetCompanyEntity.setIsActive(true);
    }

    @Test
    void findByAdminUserId_shouldReturnFleetCompany_whenExists() {
        // Given
        Long userId = 1L;
        when(jpaFleetCompanyRepository.findByAdminUserId(userId))
                .thenReturn(Optional.of(fleetCompanyEntity));
        when(fleetCompanyEntityMapper.toDomain(fleetCompanyEntity))
                .thenReturn(fleetCompany);

        // When
        Optional<FleetCompany> result = fleetRepositoryAdapter.findByAdminUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(fleetCompany);
        verify(jpaFleetCompanyRepository).findByAdminUserId(userId);
        verify(fleetCompanyEntityMapper).toDomain(fleetCompanyEntity);
    }

    @Test
    void findByAdminUserId_shouldReturnEmpty_whenNotExists() {
        // Given
        Long userId = 999L;
        when(jpaFleetCompanyRepository.findByAdminUserId(userId))
                .thenReturn(Optional.empty());

        // When
        Optional<FleetCompany> result = fleetRepositoryAdapter.findByAdminUserId(userId);

        // Then
        assertThat(result).isEmpty();
        verify(jpaFleetCompanyRepository).findByAdminUserId(userId);
        verify(fleetCompanyEntityMapper, never()).toDomain(any());
    }

    @Test
    void getTotalConsumptionByFleetId_shouldReturnTotalConsumption() {
        // Given
        Long fleetId = 1L;
        BigDecimal expectedTotal = new BigDecimal("1500.00");
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId)))
                .thenReturn(expectedTotal);

        // When
        Optional<BigDecimal> result = fleetRepositoryAdapter.getTotalConsumptionByFleetId(fleetId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(expectedTotal);
        verify(jdbcTemplate).queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId));
    }

    @Test
    void getTotalConsumptionByFleetId_shouldReturnZero_whenNull() {
        // Given
        Long fleetId = 1L;
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId)))
                .thenReturn(null);

        // When
        Optional<BigDecimal> result = fleetRepositoryAdapter.getTotalConsumptionByFleetId(fleetId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentMonthConsumptionByFleetId_shouldReturnMonthlyConsumption() {
        // Given
        Long fleetId = 1L;
        BigDecimal expectedMonthly = new BigDecimal("300.00");
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId)))
                .thenReturn(expectedMonthly);

        // When
        Optional<BigDecimal> result = fleetRepositoryAdapter.getCurrentMonthConsumptionByFleetId(fleetId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(expectedMonthly);
        verify(jdbcTemplate).queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId));
    }

    @Test
    void getCurrentMonthConsumptionByFleetId_shouldReturnZero_whenNull() {
        // Given
        Long fleetId = 1L;
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), eq(fleetId)))
                .thenReturn(null);

        // When
        Optional<BigDecimal> result = fleetRepositoryAdapter.getCurrentMonthConsumptionByFleetId(fleetId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void countActiveVehiclesByFleetId_shouldReturnCount() {
        // Given
        Long fleetId = 1L;
        Integer expectedCount = 5;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(fleetId)))
                .thenReturn(expectedCount);

        // When
        Integer result = fleetRepositoryAdapter.countActiveVehiclesByFleetId(fleetId);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(fleetId));
    }

    @Test
    void countActiveVehiclesByFleetId_shouldReturnZero_whenNull() {
        // Given
        Long fleetId = 1L;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(fleetId)))
                .thenReturn(null);

        // When
        Integer result = fleetRepositoryAdapter.countActiveVehiclesByFleetId(fleetId);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void countTotalVehiclesByFleetId_shouldReturnCount() {
        // Given
        Long fleetId = 1L;
        Integer expectedCount = 10;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(fleetId)))
                .thenReturn(expectedCount);

        // When
        Integer result = fleetRepositoryAdapter.countTotalVehiclesByFleetId(fleetId);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(fleetId));
    }

    @Test
    void countTotalVehiclesByFleetId_shouldReturnZero_whenNull() {
        // Given
        Long fleetId = 1L;
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(fleetId)))
                .thenReturn(null);

        // When
        Integer result = fleetRepositoryAdapter.countTotalVehiclesByFleetId(fleetId);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void save_shouldSaveAndReturnFleetCompany() {
        // Given
        when(fleetCompanyEntityMapper.toEntity(fleetCompany))
                .thenReturn(fleetCompanyEntity);
        when(jpaFleetCompanyRepository.save(fleetCompanyEntity))
                .thenReturn(fleetCompanyEntity);
        when(fleetCompanyEntityMapper.toDomain(fleetCompanyEntity))
                .thenReturn(fleetCompany);

        // When
        FleetCompany result = fleetRepositoryAdapter.save(fleetCompany);

        // Then
        assertThat(result).isEqualTo(fleetCompany);
        verify(fleetCompanyEntityMapper).toEntity(fleetCompany);
        verify(jpaFleetCompanyRepository).save(fleetCompanyEntity);
        verify(fleetCompanyEntityMapper).toDomain(fleetCompanyEntity);
    }

    @Test
    void findById_shouldReturnFleetCompany_whenExists() {
        // Given
        Long id = 1L;
        when(jpaFleetCompanyRepository.findById(id))
                .thenReturn(Optional.of(fleetCompanyEntity));
        when(fleetCompanyEntityMapper.toDomain(fleetCompanyEntity))
                .thenReturn(fleetCompany);

        // When
        Optional<FleetCompany> result = fleetRepositoryAdapter.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(fleetCompany);
        verify(jpaFleetCompanyRepository).findById(id);
        verify(fleetCompanyEntityMapper).toDomain(fleetCompanyEntity);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        // Given
        Long id = 999L;
        when(jpaFleetCompanyRepository.findById(id))
                .thenReturn(Optional.empty());

        // When
        Optional<FleetCompany> result = fleetRepositoryAdapter.findById(id);

        // Then
        assertThat(result).isEmpty();
        verify(jpaFleetCompanyRepository).findById(id);
        verify(fleetCompanyEntityMapper, never()).toDomain(any());
    }
}
