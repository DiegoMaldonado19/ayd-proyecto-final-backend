package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.model.validation.AdministrativeCharge;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AdministrativeChargeConfigEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.ChangeRequestStatusEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PlateChangeRequestEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAdministrativeChargeConfigRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaChangeRequestStatusRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPlateChangeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateChangeChargeCalculationServiceTest {

    @Mock
    private JpaPlateChangeRequestRepository jpaPlateChangeRequestRepository;

    @Mock
    private JpaChangeRequestStatusRepository statusRepository;

    @Mock
    private JpaAdministrativeChargeConfigRepository chargeConfigRepository;

    @InjectMocks
    private PlateChangeChargeCalculationService chargeCalculationService;

    private ChangeRequestStatusEntity approvedStatus;
    private ChangeRequestStatusEntity rejectedStatus;
    private AdministrativeChargeConfigEntity firstChangeConfig;
    private AdministrativeChargeConfigEntity secondChangeConfig;
    private AdministrativeChargeConfigEntity repeatedRequestsConfig;

    @BeforeEach
    void setUp() {
        approvedStatus = new ChangeRequestStatusEntity();
        approvedStatus.setId(2);
        approvedStatus.setCode("APPROVED");

        rejectedStatus = new ChangeRequestStatusEntity();
        rejectedStatus.setId(3);
        rejectedStatus.setCode("REJECTED");

        firstChangeConfig = new AdministrativeChargeConfigEntity();
        firstChangeConfig.setId(1);
        firstChangeConfig.setReasonCode("FIRST_CHANGE_6_TO_12_MONTHS");
        firstChangeConfig.setDescription("Primer cambio entre 6 y 12 meses");
        firstChangeConfig.setChargeAmount(new BigDecimal("25.00"));
        firstChangeConfig.setIsActive(true);

        secondChangeConfig = new AdministrativeChargeConfigEntity();
        secondChangeConfig.setId(2);
        secondChangeConfig.setReasonCode("SECOND_CHANGE_YEAR");
        secondChangeConfig.setDescription("Segundo cambio de placa en menos de 12 meses");
        secondChangeConfig.setChargeAmount(new BigDecimal("50.00"));
        secondChangeConfig.setIsActive(true);

        repeatedRequestsConfig = new AdministrativeChargeConfigEntity();
        repeatedRequestsConfig.setId(3);
        repeatedRequestsConfig.setReasonCode("REPEATED_REQUESTS");
        repeatedRequestsConfig.setDescription("Más de 2 solicitudes rechazadas previas");
        repeatedRequestsConfig.setChargeAmount(new BigDecimal("25.00"));
        repeatedRequestsConfig.setIsActive(true);
    }

    @Test
    void calculateCharge_shouldReturnNoCharge_whenNoPreviousApprovedChanges() {
        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));
        when(jpaPlateChangeRequestRepository.findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        AdministrativeCharge result = chargeCalculationService.calculateCharge(1L, 1, LocalDateTime.now());

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isFalse();
        assertThat(result.getAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateCharge_shouldReturnNoCharge_whenMoreThan12MonthsSinceLastChange() {
        PlateChangeRequestEntity lastChange = new PlateChangeRequestEntity();
        lastChange.setReviewedAt(LocalDateTime.now().minusMonths(13));

        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));
        when(jpaPlateChangeRequestRepository.findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(anyLong(), anyInt()))
                .thenReturn(Collections.singletonList(lastChange));

        AdministrativeCharge result = chargeCalculationService.calculateCharge(1L, 1, LocalDateTime.now());

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isFalse();
    }

    @Test
    void calculateCharge_shouldApplyReducedCharge_whenFirstChangeBetween6And12Months() {
        PlateChangeRequestEntity lastChange = new PlateChangeRequestEntity();
        lastChange.setReviewedAt(LocalDateTime.now().minusMonths(8));

        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));
        when(jpaPlateChangeRequestRepository.findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(anyLong(), anyInt()))
                .thenReturn(Collections.singletonList(lastChange));
        when(chargeConfigRepository.findByReasonCodeAndIsActiveTrue("FIRST_CHANGE_6_TO_12_MONTHS"))
                .thenReturn(Optional.of(firstChangeConfig));

        AdministrativeCharge result = chargeCalculationService.calculateCharge(1L, 1, LocalDateTime.now());

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isTrue();
        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getReasonCode()).isEqualTo("FIRST_CHANGE_6_TO_12_MONTHS");
    }

    @Test
    void calculateCharge_shouldApplyHigherCharge_whenSecondChangeInLessThan12Months() {
        PlateChangeRequestEntity firstChange = new PlateChangeRequestEntity();
        firstChange.setReviewedAt(LocalDateTime.now().minusMonths(10));

        PlateChangeRequestEntity secondChange = new PlateChangeRequestEntity();
        secondChange.setReviewedAt(LocalDateTime.now().minusMonths(4));

        when(statusRepository.findByCode("APPROVED")).thenReturn(Optional.of(approvedStatus));
        when(jpaPlateChangeRequestRepository.findBySubscriptionIdAndStatusIdOrderByReviewedAtDesc(anyLong(), anyInt()))
                .thenReturn(Arrays.asList(firstChange, secondChange));
        when(chargeConfigRepository.findByReasonCodeAndIsActiveTrue("SECOND_CHANGE_YEAR"))
                .thenReturn(Optional.of(secondChangeConfig));

        AdministrativeCharge result = chargeCalculationService.calculateCharge(1L, 1, LocalDateTime.now());

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isTrue();
        assertThat(result.getAmount()).isEqualByComparingTo("50.00");
        assertThat(result.getReasonCode()).isEqualTo("SECOND_CHANGE_YEAR");
    }

    @Test
    void calculateChargeForRejectedRequests_shouldReturnNoCharge_when2OrFewerRejections() {
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.of(rejectedStatus));
        when(jpaPlateChangeRequestRepository.countBySubscriptionIdAndStatusId(anyLong(), anyInt()))
                .thenReturn(2L);

        AdministrativeCharge result = chargeCalculationService.calculateChargeForRejectedRequests(1L);

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isFalse();
    }

    @Test
    void calculateChargeForRejectedRequests_shouldApplyCharge_whenMoreThan2Rejections() {
        when(statusRepository.findByCode("REJECTED")).thenReturn(Optional.of(rejectedStatus));
        when(jpaPlateChangeRequestRepository.countBySubscriptionIdAndStatusId(anyLong(), anyInt()))
                .thenReturn(3L);
        when(chargeConfigRepository.findByReasonCodeAndIsActiveTrue("REPEATED_REQUESTS"))
                .thenReturn(Optional.of(repeatedRequestsConfig));

        AdministrativeCharge result = chargeCalculationService.calculateChargeForRejectedRequests(1L);

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isTrue();
        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
        assertThat(result.getReasonCode()).isEqualTo("REPEATED_REQUESTS");
    }

    @Test
    void combineCharges_shouldReturnSecond_whenFirstHasNoCharge() {
        AdministrativeCharge noCharge = AdministrativeCharge.noCharge();
        AdministrativeCharge withCharge = AdministrativeCharge.builder()
                .amount(new BigDecimal("25.00"))
                .reason("Test charge")
                .reasonCode("TEST")
                .build();

        AdministrativeCharge result = chargeCalculationService.combineCharges(noCharge, withCharge);

        assertThat(result).isEqualTo(withCharge);
    }

    @Test
    void combineCharges_shouldReturnFirst_whenSecondHasNoCharge() {
        AdministrativeCharge withCharge = AdministrativeCharge.builder()
                .amount(new BigDecimal("25.00"))
                .reason("Test charge")
                .reasonCode("TEST")
                .build();
        AdministrativeCharge noCharge = AdministrativeCharge.noCharge();

        AdministrativeCharge result = chargeCalculationService.combineCharges(withCharge, noCharge);

        assertThat(result).isEqualTo(withCharge);
    }

    @Test
    void combineCharges_shouldAddAmounts_whenBothHaveCharges() {
        AdministrativeCharge charge1 = AdministrativeCharge.builder()
                .amount(new BigDecimal("25.00"))
                .reason("Charge 1")
                .reasonCode("C1")
                .build();
        AdministrativeCharge charge2 = AdministrativeCharge.builder()
                .amount(new BigDecimal("50.00"))
                .reason("Charge 2")
                .reasonCode("C2")
                .build();

        AdministrativeCharge result = chargeCalculationService.combineCharges(charge1, charge2);

        assertThat(result).isNotNull();
        assertThat(result.hasCharge()).isTrue();
        assertThat(result.getAmount()).isEqualByComparingTo("75.00");
        assertThat(result.getReason()).contains("Charge 1");
        assertThat(result.getReason()).contains("Charge 2");
        assertThat(result.getReasonCode()).isEqualTo("COMBINED_CHARGES");
    }
}
