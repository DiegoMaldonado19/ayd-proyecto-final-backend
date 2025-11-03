package com.ayd.parkcontrol.application.usecase.incident;

import com.ayd.parkcontrol.application.dto.request.incident.UploadEvidenceRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
import com.ayd.parkcontrol.application.mapper.IncidentMapper;
import com.ayd.parkcontrol.application.port.output.BlobStorageService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadEvidenceUseCaseTest {

    @Mock
    private JpaIncidentRepository incidentRepository;

    @Mock
    private JpaIncidentEvidenceRepository evidenceRepository;

    @Mock
    private JpaDocumentTypeRepository documentTypeRepository;

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private IncidentMapper mapper;

    @Mock
    private BlobStorageService blobStorageService;

    @Mock
    private MultipartFile mockFile;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UploadEvidenceUseCase uploadEvidenceUseCase;

    private IncidentEntity incident;
    private DocumentTypeEntity documentType;
    private UserEntity user;
    private UploadEvidenceRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadEvidenceUseCase, "incidentsContainer", "evidencias-incidentes");

        incident = IncidentEntity.builder()
                .id(1L)
                .ticketId(100L)
                .incidentTypeId(1)
                .branchId(1L)
                .reportedByUserId(1L)
                .build();

        documentType = DocumentTypeEntity.builder()
                .id(1)
                .code("PHOTO")
                .name("Photograph")
                .build();

        user = UserEntity.builder()
                .id(1L)
                .email("operator@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        request = UploadEvidenceRequest.builder()
                .file(mockFile)
                .documentTypeId(1)
                .notes("Evidence notes")
                .build();
    }

    @Test
    void execute_WithValidData_ShouldUploadEvidence() throws Exception {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("evidence.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("operator@parkcontrol.com")).thenReturn(Optional.of(user));
        when(blobStorageService.uploadFile(anyString(), any(MultipartFile.class), anyString()))
                .thenReturn("https://storage.azure.com/evidence.jpg");

        IncidentEvidenceEntity savedEvidence = IncidentEvidenceEntity.builder()
                .id(1L)
                .incidentId(1L)
                .documentTypeId(1)
                .filePath("https://storage.azure.com/evidence.jpg")
                .build();

        when(evidenceRepository.save(any(IncidentEvidenceEntity.class))).thenReturn(savedEvidence);
        
        IncidentEvidenceResponse expectedResponse = IncidentEvidenceResponse.builder()
                .id(1L)
                .build();
        
        when(mapper.toEvidenceResponseWithDetails(any(), any(), any())).thenReturn(expectedResponse);

        // Act
        IncidentEvidenceResponse result = uploadEvidenceUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        
        verify(incidentRepository).findById(1L);
        verify(documentTypeRepository).findById(1);
        verify(blobStorageService).uploadFile(eq("evidencias-incidentes"), eq(mockFile), anyString());
        verify(evidenceRepository).save(any(IncidentEvidenceEntity.class));
    }

    @Test
    void execute_WithEmptyFile_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is empty");

        verify(mockFile).isEmpty();
        verify(incidentRepository, never()).findById(anyLong());
    }

    @Test
    void execute_WithFileExceedingMaxSize_ShouldThrowIllegalArgumentException() {
        // Arrange
        long maxSize = 10 * 1024 * 1024; // 10MB
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(maxSize + 1);

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds maximum limit");

        verify(mockFile).getSize();
        verify(incidentRepository, never()).findById(anyLong());
    }

    @Test
    void execute_WithNonExistentIncident_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found with ID: 999");

        verify(incidentRepository).findById(999L);
        verify(blobStorageService, never()).uploadFile(anyString(), any(), anyString());
    }

    @Test
    void execute_WithNonExistentDocumentType_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(documentTypeRepository.findById(999)).thenReturn(Optional.empty());

        UploadEvidenceRequest invalidRequest = UploadEvidenceRequest.builder()
                .file(mockFile)
                .documentTypeId(999)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(1L, invalidRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Document type not found with ID: 999");

        verify(documentTypeRepository).findById(999);
        verify(blobStorageService, never()).uploadFile(anyString(), any(), anyString());
    }

    @Test
    void execute_WithNonExistentUser_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("unknown@parkcontrol.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("unknown@parkcontrol.com");
        verify(blobStorageService, never()).uploadFile(anyString(), any(), anyString());
    }

    @Test
    void execute_WithBlobStorageFailure_ShouldThrowRuntimeException() throws Exception {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("evidence.jpg");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("operator@parkcontrol.com")).thenReturn(Optional.of(user));
        when(blobStorageService.uploadFile(anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(new RuntimeException("Azure Storage error"));

        // Act & Assert
        assertThatThrownBy(() -> uploadEvidenceUseCase.execute(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload file to Azure Storage");

        verify(blobStorageService).uploadFile(anyString(), any(), anyString());
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    void execute_WithSpecialCharactersInFilename_ShouldSanitize() throws Exception {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("evidence file@#$.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("operator@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("operator@parkcontrol.com")).thenReturn(Optional.of(user));
        when(blobStorageService.uploadFile(anyString(), any(MultipartFile.class), anyString()))
                .thenReturn("https://storage.azure.com/evidence.jpg");

        IncidentEvidenceEntity savedEvidence = IncidentEvidenceEntity.builder()
                .id(1L)
                .fileName("evidence_file___.jpg")
                .build();

        when(evidenceRepository.save(any(IncidentEvidenceEntity.class))).thenReturn(savedEvidence);
        when(mapper.toEvidenceResponseWithDetails(any(), any(), any()))
                .thenReturn(IncidentEvidenceResponse.builder().id(1L).build());

        // Act
        IncidentEvidenceResponse result = uploadEvidenceUseCase.execute(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(evidenceRepository).save(argThat(evidence -> 
            evidence.getFileName().equals("evidence_file___.jpg")
        ));
    }
}
