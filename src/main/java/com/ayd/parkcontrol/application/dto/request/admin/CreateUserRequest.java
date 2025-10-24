package com.ayd.parkcontrol.application.dto.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request para crear un nuevo usuario")
public class CreateUserRequest {

    @Schema(description = "Email del usuario", example = "operador@parkcontrol.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no debe exceder 255 caracteres")
    private String email;

    @Schema(description = "Contraseña temporal del usuario", example = "TempPass123!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    private String password;

    @Schema(description = "Nombre del usuario", example = "Juan")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String first_name;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no debe exceder 100 caracteres")
    private String last_name;

    @Schema(description = "Teléfono del usuario", example = "50012345678")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "El teléfono debe contener entre 8 y 15 dígitos")
    private String phone;

    @Schema(description = "ID del rol del usuario (1=Admin, 2=Operador Sucursal, 3=Back Office, 4=Cliente, 5=Admin Flotilla)", example = "2")
    @NotNull(message = "El ID del rol es obligatorio")
    private Integer role_type_id;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    @Builder.Default
    private Boolean is_active = true;
}
