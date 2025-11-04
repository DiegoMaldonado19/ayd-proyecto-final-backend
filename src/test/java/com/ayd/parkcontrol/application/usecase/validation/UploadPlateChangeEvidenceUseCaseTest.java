package com.ayd.parkcontrol.application.usecase.validation;

import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.mapper.PlateChangeRequestDtoMapper;
import com.ayd.parkcontrol.application.port.output.BlobStorageService;
import com.ayd.parkcontrol.domain.exception.PlateChangeRequestNotFoundException;
import com.ayd.parkcontrol.domain.model.validation.ChangeRequestEvidence;
import com.ayd.parkcontrol.domain.model.validation.PlateChangeRequest;
import com.ayd.parkcontrol.domain.repository.ChangeRequestEvidenceRepository;
import com.ayd.parkcontrol.domain.repository.PlateChangeRequestRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.DocumentTypeEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaDocumentTypeRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UploadPlateChangeEvidenceUseCaseTest {

    @Mock
    private PlateChangeRequestRepository plateChangeRequestRepository;

    @Mock
    private ChangeRequestEvidenceRepository evidenceRepository;

    @Mock
    private JpaDocumentTypeRepository documentTypeRepository;

    @Mock
    private BlobStorageService blobStorageService;

    @Mock
    private PlateChangeRequestDtoMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UploadPlateChangeEvidenceUseCase uploadPlateChangeEvidenceUseCase;

    private PlateChangeRequest mockPlateChangeRequest;
    private ChangeRequestEvidence mockEvidence;
    private EvidenceResponse mockResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadPlateChangeEvidenceUseCase, "containerName", "plate-changes");

        mockPlateChangeRequest = PlateChangeRequest.builder()
                .id(1L)
                .subscriptionId(1L)
                .userId(1L)
                .oldLicensePlate("P-123456")
                .newLicensePlate("P-654321")
                .reasonId(1)
                .statusId(1)
                .build();

        mockEvidence = ChangeRequestEvidence.builder()
                .id(1L)
                .changeRequestId(1L)
                .documentTypeId(1)
                .fileName("dpi-frontal.jpg")
                .fileUrl("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123.jpg")
                .fileSize(1048576L)
                .uploadedBy("juan.perez@email.com")
                .uploadedAt(LocalDateTime.now())
                .build();

        mockResponse = EvidenceResponse.builder()
                .id(1L)
                .change_request_id(1L)
                .document_type_id(1)
                .document_type_code("IDENTIFICATION")
                .document_type_name("Identificación Personal")
                .file_name("dpi-frontal.jpg")
                .file_url("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123.jpg")
                .file_size(1048576L)
                .uploaded_by("juan.perez@email.com")
                .build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("juan.perez@email.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldUploadEvidenceSuccessfully() {
        // Given
        Long requestId = 1L;
        Integer documentTypeId = 1;

        DocumentTypeEntity documentType = new DocumentTypeEntity();
        documentType.setId(1);
        documentType.setCode("IDENTIFICATION");
        documentType.setName("Identificación Personal");

        when(multipartFile.getOriginalFilename()).thenReturn("dpi-frontal.jpg");
        when(multipartFile.getSize()).thenReturn(1048576L);
        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(blobStorageService.uploadFile(eq("plate-changes"), eq(multipartFile), anyString()))
                .thenReturn("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123.jpg");
        when(evidenceRepository.save(any(ChangeRequestEvidence.class))).thenReturn(mockEvidence);
        when(documentTypeRepository.findById(documentTypeId)).thenReturn(Optional.of(documentType));
        when(mapper.toEvidenceResponse(any(), anyString(), anyString())).thenReturn(mockResponse);

        // When
        EvidenceResponse result = uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFile_name()).isEqualTo("dpi-frontal.jpg");
        assertThat(result.getFile_url()).contains("storage.blob.core.windows.net");
        assertThat(result.getDocument_type_code()).isEqualTo("IDENTIFICATION");

        verify(plateChangeRequestRepository).findById(requestId);
        verify(blobStorageService).uploadFile(eq("plate-changes"), eq(multipartFile), contains("request-1"));
        verify(evidenceRepository).save(argThat(evidence -> evidence.getChangeRequestId().equals(1L) &&
                evidence.getDocumentTypeId().equals(1) &&
                evidence.getFileName().equals("dpi-frontal.jpg") &&
                evidence.getUploadedBy().equals("juan.perez@email.com")));
    }

    @Test
    void shouldThrowExceptionWhenRequestNotFound() {
        // Given
        Long requestId = 999L;
        Integer documentTypeId = 1;

        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile))
                .isInstanceOf(PlateChangeRequestNotFoundException.class);

        verify(plateChangeRequestRepository).findById(requestId);
        verify(blobStorageService, never()).uploadFile(any(), any(), any());
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    void shouldHandleFileWithoutExtension() {
        // Given
        Long requestId = 1L;
        Integer documentTypeId = 1;

        DocumentTypeEntity documentType = new DocumentTypeEntity();
        documentType.setId(1);
        documentType.setCode("IDENTIFICATION");
        documentType.setName("Identificación Personal");

        when(multipartFile.getOriginalFilename()).thenReturn("document");
        when(multipartFile.getSize()).thenReturn(1048576L);
        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(blobStorageService.uploadFile(eq("plate-changes"), eq(multipartFile), anyString()))
                .thenReturn("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123");
        when(evidenceRepository.save(any(ChangeRequestEvidence.class))).thenReturn(mockEvidence);
        when(documentTypeRepository.findById(documentTypeId)).thenReturn(Optional.of(documentType));
        when(mapper.toEvidenceResponse(any(), anyString(), anyString())).thenReturn(mockResponse);

        // When
        EvidenceResponse result = uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile);

        // Then
        assertThat(result).isNotNull();
        verify(blobStorageService).uploadFile(eq("plate-changes"), eq(multipartFile),
                argThat(blobName -> !blobName.endsWith(".")));
    }

    @Test
    void shouldHandleNullFilename() {
        // Given
        Long requestId = 1L;
        Integer documentTypeId = 1;

        DocumentTypeEntity documentType = new DocumentTypeEntity();
        documentType.setId(1);
        documentType.setCode("IDENTIFICATION");
        documentType.setName("Identificación Personal");

        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.getSize()).thenReturn(1048576L);
        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(blobStorageService.uploadFile(eq("plate-changes"), eq(multipartFile), anyString()))
                .thenReturn("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123");
        when(evidenceRepository.save(any(ChangeRequestEvidence.class))).thenReturn(mockEvidence);
        when(documentTypeRepository.findById(documentTypeId)).thenReturn(Optional.of(documentType));
        when(mapper.toEvidenceResponse(any(), anyString(), anyString())).thenReturn(mockResponse);

        // When
        EvidenceResponse result = uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile);

        // Then
        assertThat(result).isNotNull();
        verify(blobStorageService).uploadFile(eq("plate-changes"), eq(multipartFile), anyString());
    }

    @Test
    void shouldHandleDocumentTypeNotFound() {
        // Given
        Long requestId = 1L;
        Integer documentTypeId = 999;

        when(multipartFile.getOriginalFilename()).thenReturn("dpi-frontal.jpg");
        when(multipartFile.getSize()).thenReturn(1048576L);
        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(blobStorageService.uploadFile(eq("plate-changes"), eq(multipartFile), anyString()))
                .thenReturn("https://storage.blob.core.windows.net/plate-changes/request-1/abc-123.jpg");
        when(evidenceRepository.save(any(ChangeRequestEvidence.class))).thenReturn(mockEvidence);
        when(documentTypeRepository.findById(documentTypeId)).thenReturn(Optional.empty());
        when(mapper.toEvidenceResponse(any(), isNull(), isNull())).thenReturn(mockResponse);

        // When
        EvidenceResponse result = uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile);

        // Then
        assertThat(result).isNotNull();
        verify(mapper).toEvidenceResponse(any(), isNull(), isNull());
    }

    @Test
    void shouldGenerateUniqueBlobName() {
        // Given
        Long requestId = 1L;
        Integer documentTypeId = 1;

        DocumentTypeEntity documentType = new DocumentTypeEntity();
        documentType.setId(1);
        documentType.setCode("IDENTIFICATION");
        documentType.setName("Identificación Personal");

        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getSize()).thenReturn(2097152L);
        when(plateChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(mockPlateChangeRequest));
        when(blobStorageService.uploadFile(eq("plate-changes"), eq(multipartFile), anyString()))
                .thenReturn("https://storage.blob.core.windows.net/plate-changes/request-1/uuid-123.jpg");
        when(evidenceRepository.save(any(ChangeRequestEvidence.class))).thenReturn(mockEvidence);
        when(documentTypeRepository.findById(documentTypeId)).thenReturn(Optional.of(documentType));
        when(mapper.toEvidenceResponse(any(), anyString(), anyString())).thenReturn(mockResponse);

        // When
        EvidenceResponse result = uploadPlateChangeEvidenceUseCase.execute(requestId, documentTypeId, multipartFile);

        // Then
        assertThat(result).isNotNull();
        verify(blobStorageService).uploadFile(eq("plate-changes"), eq(multipartFile),
                argThat(blobName -> blobName.startsWith("request-1/") && blobName.endsWith(".jpg")));
    }
}
