package com.ayd.parkcontrol.infrastructure.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlobMetadata {

    private String blobName;
    private String containerName;
    private String contentType;
    private Long contentLength;
    private LocalDateTime lastModified;
    private String eTag;
}
