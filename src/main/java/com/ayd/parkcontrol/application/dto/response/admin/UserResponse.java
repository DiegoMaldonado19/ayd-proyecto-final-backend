package com.ayd.parkcontrol.application.dto.response.admin;

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
@Schema(description = "Respuesta con información de un usuario")
public class UserResponse {

    @Schema(description = "ID del usuario", example = "1")
    private Long id;

    @Schema(description = "Email del usuario", example = "operador@parkcontrol.com")
    private String email;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String first_name;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String last_name;

    @Schema(description = "Teléfono del usuario", example = "50012345678")
    private String phone;

    @Schema(description = "ID del rol del usuario", example = "2")
    private Integer role_type_id;

    @Schema(description = "Nombre del rol del usuario", example = "Operador Sucursal")
    private String role_name;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean is_active;

    @Schema(description = "Indica si requiere cambio de contraseña", example = "true")
    private Boolean requires_password_change;

    @Schema(description = "Indica si tiene 2FA habilitado", example = "false")
    private Boolean has_2fa_enabled;

    @Schema(description = "Intentos fallidos de login", example = "0")
    private Integer failed_login_attempts;

    @Schema(description = "Fecha hasta la cual está bloqueado", example = "2025-10-23T10:30:00")
    private LocalDateTime locked_until;

    @Schema(description = "Fecha del último login", example = "2025-10-22T15:45:00")
    private LocalDateTime last_login;

    @Schema(description = "Fecha de creación", example = "2025-10-20T08:00:00")
    private LocalDateTime created_at;

    @Schema(description = "Fecha de última actualización", example = "2025-10-22T15:45:00")
    private LocalDateTime updated_at;
}
