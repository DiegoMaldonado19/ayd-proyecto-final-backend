package com.ayd.parkcontrol.application.usecase.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.ApplyBenefitRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.BusinessFreeHoursResponse;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.NotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.AffiliatedBusinessEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessFreeHoursEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.TicketStatusTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaAffiliatedBusinessRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBusinessFreeHoursRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketStatusTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyCommerceBenefitUseCaseTest {

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaBusinessFreeHoursRepository businessFreeHoursRepository;

    @Mock
    private JpaTicketStatusTypeRepository ticketStatusTypeRepository;

    @Mock
    private JpaAffiliatedBusinessRepository affiliatedBusinessRepository;

    @InjectMocks
    private ApplyCommerceBenefitUseCase applyCommerceBenefitUseCase;

    private ApplyBenefitRequest request;
    private TicketEntity ticket;
    private TicketStatusTypeEntity inProgressStatus;
    private AffiliatedBusinessEntity business;

    @BeforeEach
    void setUp() {
        request = ApplyBenefitRequest.builder()
                .businessId(1L)
                .grantedHours(2.0)
                .build();

        ticket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-1-12345678")
                .licensePlate("ABC-123")
                .vehicleTypeId(2)
                .entryTime(LocalDateTime.now())
                .statusTypeId(1)
                .build();

        inProgressStatus = TicketStatusTypeEntity.builder()
                .id(1)
                .code("IN_PROGRESS")
                .name("En Curso")
                .build();

        business = AffiliatedBusinessEntity.builder()
                .id(1L)
                .name("Restaurant El Portal")
                .taxId("12345678-9")
                .ratePerHour(BigDecimal.valueOf(8.00))
                .isActive(true)
                .build();
    }

    @Test
    void execute_ShouldApplyBenefit_WhenDataIsValid() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(affiliatedBusinessRepository.findById(1L)).thenReturn(Optional.of(business));

        BusinessFreeHoursEntity savedEntity = BusinessFreeHoursEntity.builder()
                .id(1L)
                .ticketId(1L)
                .businessId(1L)
                .branchId(1L)
                .grantedHours(BigDecimal.valueOf(2.0))
                .grantedAt(LocalDateTime.now())
                .isSettled(false)
                .build();

        when(businessFreeHoursRepository.save(any(BusinessFreeHoursEntity.class))).thenReturn(savedEntity);

        // Act
        BusinessFreeHoursResponse response = applyCommerceBenefitUseCase.execute(1L, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTicketId()).isEqualTo(1L);
        assertThat(response.getBusinessId()).isEqualTo(1L);
        assertThat(response.getBusinessName()).isEqualTo("Restaurant El Portal");
        assertThat(response.getGrantedHours()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        assertThat(response.getIsSettled()).isFalse();

        verify(businessFreeHoursRepository, times(1)).save(any(BusinessFreeHoursEntity.class));
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> applyCommerceBenefitUseCase.execute(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenTicketIsNotInProgress() {
        // Arrange
        ticket.setStatusTypeId(2); // COMPLETED
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));

        // Act & Assert
        assertThatThrownBy(() -> applyCommerceBenefitUseCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not in progress");
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenGrantedHoursIsZero() {
        // Arrange
        request.setGrantedHours(0.0);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));

        // Act & Assert
        assertThatThrownBy(() -> applyCommerceBenefitUseCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void execute_ShouldThrowBusinessRuleException_WhenGrantedHoursIsNegative() {
        // Arrange
        request.setGrantedHours(-1.0);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));

        // Act & Assert
        assertThatThrownBy(() -> applyCommerceBenefitUseCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void execute_ShouldThrowNotFoundException_WhenBusinessNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketStatusTypeRepository.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgressStatus));
        when(affiliatedBusinessRepository.findById(999L)).thenReturn(Optional.empty());
        request.setBusinessId(999L);

        // Act & Assert
        assertThatThrownBy(() -> applyCommerceBenefitUseCase.execute(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Affiliated business not found");
    }
}
