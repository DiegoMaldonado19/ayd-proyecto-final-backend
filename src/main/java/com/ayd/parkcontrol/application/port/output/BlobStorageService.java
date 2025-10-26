package com.ayd.parkcontrol.application.port.output;

import org.springframework.web.multipart.MultipartFile;

public interface BlobStorageService {

    String uploadFile(String containerName, MultipartFile file, String blobName);

    byte[] downloadFile(String containerName, String blobName);

    void deleteFile(String containerName, String blobName);

    boolean fileExists(String containerName, String blobName);

    String getFileUrl(String containerName, String blobName);
}
