package com.ayd.parkcontrol.application.dto.request.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveIncidentRequest {

    @NotBlank(message = "Resolution notes are required")
    @Size(max = 5000, message = "Resolution notes must not exceed 5000 characters")
    private String resolutionNotes;
}
