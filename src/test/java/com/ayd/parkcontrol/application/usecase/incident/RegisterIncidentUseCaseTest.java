package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.RegisterIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.ResourceNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.*;
import com.ayd.parkcontrol.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterIncidentUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @Mock
    private JpaIncidentTypeRepository incidentTypeRepository;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private JpaIncidentEvidenceRepository evidenceRepository;

    @Mock
    private IncidentMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RegisterIncidentUseCase registerIncidentUseCase;

    private RegisterIncidentRequest request;
    private TicketEntity ticket;
    private IncidentTypeEntity incidentType;
    private BranchEntity branch;
    private UserEntity user;
    private IncidentEntity incident;
    private IncidentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        request = RegisterIncidentRequest.builder()
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .licensePlate("ABC123")
                .description("Lost ticket incident")
                .build();

        ticket = TicketEntity.builder()
                .id(1L)
                .branchId(1L)
                .folio("T-001")
                .licensePlate("ABC123")
                .vehicleTypeId(2)
                .hasIncident(false)
                .isSubscriber(false)
                .statusTypeId(1)
                .build();

        incidentType = IncidentTypeEntity.builder()
                .id(1)
                .code("LOST_TICKET")
                .name("Lost Ticket")
                .requiresDocumentation(true)
                .build();

        branch = BranchEntity.builder()
                .id(1L)
                .name("Centro")
                .build();

        user = UserEntity.builder()
                .id(1L)
                .email("operator@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .licensePlate("ABC123")
                .description("Lost ticket incident")
                .isResolved(false)
                .build();

        expectedResponse = IncidentResponse.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .incidentTypeName("Lost Ticket")
                .branchId(1L)
                .branchName("Centro")
                .reportedByUserId(1L)
                .reportedByUserName("John Doe")
                .licensePlate("ABC123")
                .description("Lost ticket incident")
                .isResolved(false)
                .evidenceCount(0L)
                .build();
    }

    @Test
    void execute_ShouldRegisterIncidentSuccessfully() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@test.com");
        when(userRepository.findByEmail("operator@test.com")).thenReturn(Optional.of(user));

        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), any())).thenReturn(expectedResponse);

        // Act
        IncidentResponse result = registerIncidentUseCase.execute(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTicketId()).isEqualTo(1L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC123");
        assertThat(result.getIsResolved()).isFalse();

        verify(incidentRepository).save(any(IncidentEntity.class));
        verify(ticketRepository).save(any(TicketEntity.class));
        verify(mapper).toResponseWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void execute_ShouldThrowException_WhenTicketNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerIncidentUseCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenIncidentTypeNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerIncidentUseCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident type not found");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenBranchNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerIncidentUseCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Branch not found");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@test.com");
        when(userRepository.findByEmail("operator@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerIncidentUseCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldUpdateTicketHasIncidentFlag() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(incidentTypeRepository.findById(1)).thenReturn(Optional.of(incidentType));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@test.com");
        when(userRepository.findByEmail("operator@test.com")).thenReturn(Optional.of(user));

        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), any())).thenReturn(expectedResponse);

        // Act
        registerIncidentUseCase.execute(request);

        // Assert
        verify(ticketRepository).save(argThat(t -> t.getHasIncident().equals(true)));
    }
}
