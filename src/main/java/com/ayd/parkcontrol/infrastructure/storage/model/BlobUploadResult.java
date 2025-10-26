package com.ayd.parkcontrol.infrastructure.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobUploadResult {

    private String blobUrl;
    private String blobName;
    private String containerName;
    private Long fileSize;
    private String contentType;
    private boolean success;
    private String errorMessage;
}
