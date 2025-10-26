package com.ayd.parkcontrol.application.dto.request.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para solicitar cambio de placa")
public class CreatePlateChangeRequest {

    @Schema(description = "ID de la suscripción", example = "1")
    @NotNull(message = "El ID de la suscripción es obligatorio")
    private Long subscription_id;

    @Schema(description = "ID del usuario solicitante", example = "1")
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long user_id;

    @Schema(description = "Placa anterior", example = "P-123456")
    @NotBlank(message = "La placa anterior es obligatoria")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "Formato de placa inválido")
    @Size(max = 20, message = "La placa no debe exceder 20 caracteres")
    private String old_license_plate;

    @Schema(description = "Placa nueva", example = "P-654321")
    @NotBlank(message = "La placa nueva es obligatoria")
    @Pattern(regexp = "^[A-Z]{1,3}-?[0-9]{3,4}$|^[A-Z]{1,3}[0-9]{3,4}$|^P-[0-9]{5,6}$", message = "Formato de placa inválido")
    @Size(max = 20, message = "La placa no debe exceder 20 caracteres")
    private String new_license_plate;

    @Schema(description = "ID del motivo del cambio", example = "1")
    @NotNull(message = "El motivo del cambio es obligatorio")
    private Integer reason_id;

    @Schema(description = "Notas adicionales", example = "Robo de vehículo reportado a PNC")
    @Size(max = 500, message = "Las notas no deben exceder 500 caracteres")
    private String notes;
}
