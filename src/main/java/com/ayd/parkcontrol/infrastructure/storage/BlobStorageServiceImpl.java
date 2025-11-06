package com.ayd.parkcontrol.infrastructure.storage;

import com.ayd.parkcontrol.application.port.output.BlobStorageService;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlobStorageServiceImpl implements BlobStorageService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Override
    public String uploadFile(String containerName, MultipartFile file, String blobName) {
        try {
            log.info("Uploading file to Azure Blob Storage: container={}, blob={}", containerName, blobName);

            BlobContainerClient containerClient = getOrCreateContainer(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            try (InputStream inputStream = file.getInputStream()) {
                BlobHttpHeaders headers = new BlobHttpHeaders()
                        .setContentType(file.getContentType());

                blobClient.upload(inputStream, file.getSize(), true);
                blobClient.setHttpHeaders(headers);
            }

            String blobUrl = blobClient.getBlobUrl();
            log.info("File uploaded successfully to: {}", blobUrl);

            return blobUrl;

        } catch (IOException e) {
            log.error("Failed to read file content: {}", e.getMessage());
            throw new RuntimeException("Failed to read file content", e);
        } catch (BlobStorageException e) {
            log.error("Azure Blob Storage error: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Azure Storage", e);
        }
    }

    @Override
    public byte[] downloadFile(String containerName, String blobName) {
        try {
            log.info("Downloading file from Azure Blob Storage: container={}, blob={}", containerName, blobName);

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (!blobClient.exists()) {
                log.error("Blob does not exist: {}", blobName);
                throw new RuntimeException("File not found: " + blobName);
            }

            byte[] content = blobClient.downloadContent().toBytes();
            log.info("File downloaded successfully: {} bytes", content.length);

            return content;

        } catch (BlobStorageException e) {
            log.error("Azure Blob Storage error during download: {}", e.getMessage());
            throw new RuntimeException("Failed to download file from Azure Storage", e);
        }
    }

    @Override
    public void deleteFile(String containerName, String blobName) {
        try {
            log.info("Deleting file from Azure Blob Storage: container={}, blob={}", containerName, blobName);

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info("File deleted successfully: {}", blobName);
            } else {
                log.warn("Attempted to delete non-existent blob: {}", blobName);
            }

        } catch (BlobStorageException e) {
            log.error("Azure Blob Storage error during deletion: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from Azure Storage", e);
        }
    }

    @Override
    public boolean fileExists(String containerName, String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();

        } catch (BlobStorageException e) {
            log.error("Error checking blob existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFileUrl(String containerName, String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.getBlobUrl();
    }

    private BlobContainerClient getOrCreateContainer(String containerName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            log.info("Container does not exist, creating: {}", containerName);
            containerClient.create();

            // Set public access level to allow anonymous read access to blobs
            containerClient.setAccessPolicy(PublicAccessType.BLOB, null);
            log.info("Container created with public blob access: {}", containerName);
        }

        return containerClient;
    }
}
