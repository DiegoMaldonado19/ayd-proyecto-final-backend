package com.ayd.parkcontrol.application.dto.response.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEvidenceResponse {

    private Long id;
    private Long incidentId;
    private Integer documentTypeId;
    private String documentTypeName;
    private String filePath;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private String notes;
    private Long uploadedByUserId;
    private String uploadedByUserName;
    private LocalDateTime createdAt;
}
