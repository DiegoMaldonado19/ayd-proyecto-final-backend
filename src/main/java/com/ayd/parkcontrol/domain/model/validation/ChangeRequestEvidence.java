package com.ayd.parkcontrol.domain.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestEvidence {

    private Long id;
    private Long changeRequestId;
    private Integer documentTypeId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
