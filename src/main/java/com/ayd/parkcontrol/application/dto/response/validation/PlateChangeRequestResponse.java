package com.ayd.parkcontrol.application.dto.response.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta con información de una solicitud de cambio de placa")
public class PlateChangeRequestResponse {

    @Schema(description = "ID de la solicitud", example = "1")
    private Long id;

    @Schema(description = "ID de la suscripción", example = "1")
    private Long subscription_id;

    @Schema(description = "ID del usuario solicitante", example = "1")
    private Long user_id;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String user_name;

    @Schema(description = "Placa anterior", example = "P-123456")
    private String old_license_plate;

    @Schema(description = "Placa nueva", example = "P-654321")
    private String new_license_plate;

    @Schema(description = "ID del motivo", example = "1")
    private Integer reason_id;

    @Schema(description = "Nombre del motivo", example = "Robo")
    private String reason_name;

    @Schema(description = "Notas adicionales", example = "Robo reportado a PNC")
    private String notes;

    @Schema(description = "ID del estado", example = "1")
    private Integer status_id;

    @Schema(description = "Código del estado", example = "PENDING")
    private String status_code;

    @Schema(description = "Nombre del estado", example = "Pendiente")
    private String status_name;

    @Schema(description = "ID del revisor", example = "2")
    private Long reviewed_by;

    @Schema(description = "Nombre del revisor", example = "Carlos Rodríguez")
    private String reviewer_name;

    @Schema(description = "Fecha de revisión", example = "2025-10-23T10:30:00")
    private LocalDateTime reviewed_at;

    @Schema(description = "Notas de revisión", example = "Documentación verificada")
    private String review_notes;

    @Schema(description = "Fecha de creación", example = "2025-10-22T08:00:00")
    private LocalDateTime created_at;

    @Schema(description = "Fecha de actualización", example = "2025-10-23T10:30:00")
    private LocalDateTime updated_at;

    @Schema(description = "Cantidad de evidencias adjuntas", example = "3")
    private Long evidence_count;
}
