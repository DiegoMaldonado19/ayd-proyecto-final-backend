package com.ayd.parkcontrol.application.dto.request.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request para actualizar un usuario existente")
public class UpdateUserRequest {

    @Schema(description = "Email del usuario", example = "operador@parkcontrol.com")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no debe exceder 255 caracteres")
    private String email;

    @Schema(description = "Nombre del usuario", example = "Juan")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String first_name;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    @Size(max = 100, message = "El apellido no debe exceder 100 caracteres")
    private String last_name;

    @Schema(description = "Teléfono del usuario", example = "50012345678")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "El teléfono debe contener entre 8 y 15 dígitos")
    private String phone;

    @Schema(description = "ID del rol del usuario", example = "2")
    private Integer role_type_id;
}
