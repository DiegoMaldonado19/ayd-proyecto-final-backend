package com.ayd.parkcontrol.presentation.controller.admin;

import com.ayd.parkcontrol.application.dto.request.admin.CreateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserStatusRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.usecase.admin.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Administración de Usuarios", description = "Endpoints para gestionar usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

        private final CreateUserUseCase createUserUseCase;
        private final ListUsersUseCase listUsersUseCase;
        private final ListAllUsersUseCase listAllUsersUseCase;
        private final GetUserUseCase getUserUseCase;
        private final UpdateUserUseCase updateUserUseCase;
        private final DeleteUserUseCase deleteUserUseCase;
        private final UpdateUserStatusUseCase updateUserStatusUseCase;
        private final ListUsersByRoleUseCase listUsersByRoleUseCase;
        private final ListUsersByStatusUseCase listUsersByStatusUseCase;

        @Operation(summary = "Listar usuarios", description = "Obtiene una lista paginada de usuarios del sistema con opciones de ordenamiento")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
        })
        @GetMapping
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
                        @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {
                PageResponse<UserResponse> response = listUsersUseCase.execute(page, size, sortBy, sortDirection);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "Listar todos los usuarios (sin paginación)", description = "Obtiene una lista completa de todos los usuarios del sistema sin paginación, con opciones de ordenamiento")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
        })
        @GetMapping("/all")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<List<UserResponse>>> listAllUsers(
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {
                List<UserResponse> response = listAllUsersUseCase.execute(sortBy, sortDirection);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema con los datos proporcionados")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para crear usuarios", content = @Content)
        })
        @PostMapping
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody CreateUserRequest request) {
                UserResponse response = createUserUseCase.execute(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Usuario creado exitosamente"));
        }

        @Operation(summary = "Obtener usuario", description = "Obtiene la información detallada de un usuario específico por su ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para ver usuarios", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<UserResponse>> getUser(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable Long id) {
                UserResponse response = getUserUseCase.execute(id);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para actualizar usuarios", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable Long id,
                        @Valid @RequestBody UpdateUserRequest request) {
                UserResponse response = updateUserUseCase.execute(id, request);
                return ResponseEntity.ok(ApiResponse.success(response, "Usuario actualizado exitosamente"));
        }

        @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema de forma permanente")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para eliminar usuarios", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<Void>> deleteUser(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable Long id) {
                deleteUserUseCase.execute(id);
                return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"));
        }

        @Operation(summary = "Activar/Desactivar usuario", description = "Cambia el estado activo/inactivo de un usuario")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado del usuario actualizado exitosamente", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para cambiar estado de usuarios", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
        })
        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
                        @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable Long id,
                        @Valid @RequestBody UpdateUserStatusRequest request) {
                UserResponse response = updateUserStatusUseCase.execute(id, request);
                return ResponseEntity.ok(ApiResponse.success(response, "Estado del usuario actualizado exitosamente"));
        }

        @Operation(summary = "Listar usuarios por rol", description = "Obtiene una lista paginada de usuarios filtrados por rol específico")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content)
        })
        @GetMapping("/by-role/{roleId}")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsersByRole(
                        @Parameter(description = "ID del rol", example = "1", required = true) @PathVariable Integer roleId,
                        @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {
                PageResponse<UserResponse> response = listUsersByRoleUseCase.execute(roleId, page, size, sortBy,
                                sortDirection);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "Listar usuarios por estado", description = "Obtiene una lista paginada de usuarios filtrados por estado activo/inactivo")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
        })
        @GetMapping("/by-status")
        @PreAuthorize("hasRole('Administrador')")
        public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsersByStatus(
                        @Parameter(description = "Estado del usuario (true=activo, false=inactivo)", example = "true", required = true) @RequestParam Boolean isActive,
                        @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
                        @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
                        @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {
                PageResponse<UserResponse> response = listUsersByStatusUseCase.execute(isActive, page, size, sortBy,
                                sortDirection);
                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
