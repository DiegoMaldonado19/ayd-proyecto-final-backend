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
public class RegisterIncidentRequest {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Incident type ID is required")
    private Integer incidentTypeId;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}
