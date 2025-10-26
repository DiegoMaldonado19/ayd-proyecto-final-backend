package com.ayd.parkcontrol.application.dto.request.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para aprobar cambio de placa")
public class ApprovePlateChangeRequest {

    @Schema(description = "Notas de la revisión", example = "Documentación verificada y aprobada")
    @Size(max = 500, message = "Las notas no deben exceder 500 caracteres")
    private String review_notes;
}
