package com.ayd.parkcontrol.infrastructure.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlobStorageServiceImplTest {

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobClient blobClient;

    @Mock
    private BlockBlobClient blockBlobClient;

    @InjectMocks
    private BlobStorageServiceImpl blobStorageService;

    private static final String ACCOUNT_NAME = "testaccount";
    private static final String CONTAINER_NAME = "evidencias-incidentes";
    private static final String BLOB_NAME = "incident_1/20251026_120000_test.jpg";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(blobStorageService, "accountName", ACCOUNT_NAME);
    }

    @Test
    void uploadFile_ShouldUploadSuccessfully() throws IOException {
        // Arrange
        byte[] content = "test file content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                content);

        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.getBlobUrl())
                .thenReturn("https://" + ACCOUNT_NAME + ".blob.core.windows.net/" + CONTAINER_NAME + "/" + BLOB_NAME);
        doNothing().when(blobClient).upload(any(), anyLong(), anyBoolean());
        doNothing().when(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));

        // Act
        String result = blobStorageService.uploadFile(CONTAINER_NAME, file, BLOB_NAME);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains(ACCOUNT_NAME);
        assertThat(result).contains(CONTAINER_NAME);
        assertThat(result).contains(BLOB_NAME);

        verify(blobServiceClient).getBlobContainerClient(CONTAINER_NAME);
        verify(blobClient).upload(any(), eq((long) content.length), eq(true));
        verify(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));
    }

    @Test
    void uploadFile_ShouldCreateContainerIfNotExists() throws IOException {
        // Arrange
        byte[] content = "test content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                content);

        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(false);
        doNothing().when(blobContainerClient).create();
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.getBlobUrl())
                .thenReturn("https://" + ACCOUNT_NAME + ".blob.core.windows.net/" + CONTAINER_NAME + "/" + BLOB_NAME);
        doNothing().when(blobClient).upload(any(), anyLong(), anyBoolean());
        doNothing().when(blobClient).setHttpHeaders(any(BlobHttpHeaders.class));

        // Act
        String result = blobStorageService.uploadFile(CONTAINER_NAME, file, BLOB_NAME);

        // Assert
        assertThat(result).isNotNull();
        verify(blobContainerClient).create();
    }

    @Test
    void uploadFile_ShouldThrowException_WhenBlobStorageException() throws IOException {
        // Arrange
        byte[] content = "test content".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                content);

        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.exists()).thenReturn(true);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        doThrow(new BlobStorageException("Upload failed", null, null))
                .when(blobClient).upload(any(), anyLong(), anyBoolean());

        // Act & Assert
        assertThatThrownBy(() -> blobStorageService.uploadFile(CONTAINER_NAME, file, BLOB_NAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload file to Azure Storage");
    }

    @Test
    void deleteFile_ShouldDeleteExistingFile() {
        // Arrange
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        doNothing().when(blobClient).delete();

        // Act
        blobStorageService.deleteFile(CONTAINER_NAME, BLOB_NAME);

        // Assert
        verify(blobClient).delete();
    }

    @Test
    void deleteFile_ShouldNotThrow_WhenFileDoesNotExist() {
        // Arrange
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);

        // Act
        blobStorageService.deleteFile(CONTAINER_NAME, BLOB_NAME);

        // Assert
        verify(blobClient, never()).delete();
    }

    @Test
    void fileExists_ShouldReturnTrue_WhenFileExists() {
        // Arrange
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);

        // Act
        boolean result = blobStorageService.fileExists(CONTAINER_NAME, BLOB_NAME);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void fileExists_ShouldReturnFalse_WhenFileDoesNotExist() {
        // Arrange
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);

        // Act
        boolean result = blobStorageService.fileExists(CONTAINER_NAME, BLOB_NAME);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void getFileUrl_ShouldReturnCorrectUrl() {
        // Arrange
        String expectedUrl = "https://" + ACCOUNT_NAME + ".blob.core.windows.net/" + CONTAINER_NAME + "/" + BLOB_NAME;
        when(blobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(BLOB_NAME)).thenReturn(blobClient);
        when(blobClient.getBlobUrl()).thenReturn(expectedUrl);

        // Act
        String result = blobStorageService.getFileUrl(CONTAINER_NAME, BLOB_NAME);

        // Assert
        assertThat(result).isEqualTo(expectedUrl);
        verify(blobServiceClient).getBlobContainerClient(CONTAINER_NAME);
        verify(blobContainerClient).getBlobClient(BLOB_NAME);
        verify(blobClient).getBlobUrl();
    }
}
