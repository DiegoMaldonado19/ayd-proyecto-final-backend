package com.ayd.parkcontrol.application.dto.request.validation;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request para rechazar cambio de placa")
public class RejectPlateChangeRequest {

    @Schema(description = "Motivo del rechazo", example = "Documentación incompleta o no válida")
    @NotBlank(message = "El motivo del rechazo es obligatorio")
    @Size(max = 500, message = "El motivo no debe exceder 500 caracteres")
    private String review_notes;
}
