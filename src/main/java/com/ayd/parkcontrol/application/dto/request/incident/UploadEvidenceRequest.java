package com.ayd.parkcontrol.application.dto.request.incident;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceRequest {

    @NotNull(message = "Document type ID is required")
    private Integer documentTypeId;

    @NotNull(message = "File is required")
    private MultipartFile file;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
