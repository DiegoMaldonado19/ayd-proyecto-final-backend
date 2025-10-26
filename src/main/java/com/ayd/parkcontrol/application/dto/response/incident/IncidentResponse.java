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
public class IncidentResponse {

    private Long id;
    private Long ticketId;
    private Integer incidentTypeId;
    private String incidentTypeName;
    private Long branchId;
    private String branchName;
    private Long reportedByUserId;
    private String reportedByUserName;
    private String licensePlate;
    private String description;
    private String resolutionNotes;
    private Boolean isResolved;
    private Long resolvedByUserId;
    private String resolvedByUserName;
    private LocalDateTime resolvedAt;
    private Long evidenceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
