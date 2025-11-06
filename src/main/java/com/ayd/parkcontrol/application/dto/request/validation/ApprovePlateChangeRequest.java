package com.ayd.parkcontrol.application.dto.request.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para aprobar cambio de placa")
public class ApprovePlateChangeRequest {

    @Schema(description = "Notas de la revisión", example = "Documentación verificada y aprobada")
    @Size(max = 500, message = "Las notas no deben exceder 500 caracteres")
    private String review_notes;

    @Schema(description = "Indica si se debe aplicar cargo administrativo manualmente", example = "false")
    private Boolean apply_administrative_charge;

    @Schema(description = "Monto del cargo administrativo (si se aplica manualmente)", example = "25.00")
    @DecimalMin(value = "0.0", inclusive = true, message = "El cargo administrativo debe ser mayor o igual a 0")
    private BigDecimal administrative_charge_amount;

    @Schema(description = "Razón del cargo administrativo manual", example = "Procesamiento urgente")
    @Size(max = 500, message = "La razón del cargo no debe exceder 500 caracteres")
    private String administrative_charge_reason;
}
