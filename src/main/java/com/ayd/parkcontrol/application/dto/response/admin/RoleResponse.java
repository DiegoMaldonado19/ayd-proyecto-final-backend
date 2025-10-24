package com.ayd.parkcontrol.application.dto.response.admin;

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
@Schema(description = "Respuesta con información de un rol")
public class RoleResponse {

    @Schema(description = "ID del rol", example = "1")
    private Integer id;

    @Schema(description = "Nombre del rol", example = "Administrador")
    private String name;

    @Schema(description = "Descripción del rol", example = "Administrador del sistema con acceso total")
    private String description;

    @Schema(description = "Fecha de creación", example = "2025-10-20T08:00:00")
    private LocalDateTime created_at;
}
