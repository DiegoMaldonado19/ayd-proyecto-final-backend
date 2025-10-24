package com.ayd.parkcontrol.presentation.controller.admin;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.usecase.admin.GetRoleUseCase;
import com.ayd.parkcontrol.application.usecase.admin.ListRolesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles y Permisos", description = "Endpoints para gestionar roles del sistema")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;

    @Operation(summary = "Listar roles", description = "Obtiene la lista completa de roles disponibles en el sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        List<RoleResponse> response = listRolesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Obtener rol", description = "Obtiene la información detallada de un rol específico por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rol encontrado", content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para ver roles", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @Parameter(description = "ID del rol", example = "1", required = true) @PathVariable Integer id) {
        RoleResponse response = getRoleUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Obtener permisos del rol", description = "Obtiene la lista de permisos asociados a un rol específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permisos del rol obtenidos exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para ver roles", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content)
    })
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRolePermissions(
            @Parameter(description = "ID del rol", example = "1", required = true) @PathVariable Integer id) {
        RoleResponse response = getRoleUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
