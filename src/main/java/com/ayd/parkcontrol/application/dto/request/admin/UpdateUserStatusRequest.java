package com.ayd.parkcontrol.application.dto.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para cambiar el estado de un usuario")
public class UpdateUserStatusRequest {

    @Schema(description = "Estado activo del usuario", example = "true")
    @NotNull(message = "El estado es obligatorio")
    private Boolean is_active;
}
