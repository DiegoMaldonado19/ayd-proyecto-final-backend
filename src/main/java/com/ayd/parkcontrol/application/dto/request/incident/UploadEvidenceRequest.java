package com.ayd.parkcontrol.application.dto.request.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceRequest {

    @NotNull(message = "Document type ID is required")
    private Integer documentTypeId;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path must not exceed 500 characters")
    private String filePath;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    private Long fileSize;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
