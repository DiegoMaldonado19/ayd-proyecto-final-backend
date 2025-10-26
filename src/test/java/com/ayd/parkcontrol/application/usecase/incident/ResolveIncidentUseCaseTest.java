package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.ResolveIncidentRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
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
class ResolveIncidentUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

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
    private ResolveIncidentUseCase resolveIncidentUseCase;

    private ResolveIncidentRequest request;
    private IncidentEntity incident;
    private UserEntity user;
    private IncidentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        request = ResolveIncidentRequest.builder()
                .resolutionNotes("Issue resolved successfully")
                .build();

        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(1L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .licensePlate("ABC123")
                .isResolved(false)
                .build();

        user = UserEntity.builder()
                .id(2L)
                .email("resolver@test.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        expectedResponse = IncidentResponse.builder()
                .id(1L)
                .ticketId(1L)
                .isResolved(true)
                .resolvedByUserId(2L)
                .resolutionNotes("Issue resolved successfully")
                .build();
    }

    @Test
    void execute_ShouldResolveIncidentSuccessfully() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("resolver@test.com");
        when(userRepository.findByEmail("resolver@test.com")).thenReturn(Optional.of(user));

        when(incidentRepository.save(any(IncidentEntity.class))).thenReturn(incident);
        when(incidentTypeRepository.findById(any())).thenReturn(Optional.empty());
        when(branchRepository.findById(any())).thenReturn(Optional.empty());
        when(evidenceRepository.countByIncidentId(any())).thenReturn(0L);
        when(mapper.toResponseWithDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(expectedResponse);

        // Act
        IncidentResponse result = resolveIncidentUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsResolved()).isTrue();
        assertThat(result.getResolvedByUserId()).isEqualTo(2L);

        verify(incidentRepository).save(argThat(i -> i.getIsResolved().equals(true) &&
                i.getResolvedByUserId().equals(2L) &&
                i.getResolutionNotes().equals("Issue resolved successfully")));
    }

    @Test
    void execute_ShouldThrowException_WhenIncidentNotFound() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resolveIncidentUseCase.execute(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenIncidentAlreadyResolved() {
        // Arrange
        incident.setIsResolved(true);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        // Act & Assert
        assertThatThrownBy(() -> resolveIncidentUseCase.execute(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already resolved");

        verify(incidentRepository, never()).save(any());
    }

    @Test
    void execute_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("resolver@test.com");
        when(userRepository.findByEmail("resolver@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resolveIncidentUseCase.execute(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(incidentRepository, never()).save(any());
    }
}
