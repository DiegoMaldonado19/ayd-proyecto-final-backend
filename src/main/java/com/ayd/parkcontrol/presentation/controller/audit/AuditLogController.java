package com.ayd.parkcontrol.presentation.controller.audit;

import com.ayd.parkcontrol.application.dto.response.audit.AuditLogResponse;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.usecase.audit.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Endpoints para consultar logs de auditoría del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final ListAuditLogsUseCase listAuditLogsUseCase;
    private final GetAuditLogUseCase getAuditLogUseCase;
    private final ListAuditLogsByUserUseCase listAuditLogsByUserUseCase;
    private final ListAuditLogsByModuleUseCase listAuditLogsByModuleUseCase;
    private final ListAuditLogsByDateRangeUseCase listAuditLogsByDateRangeUseCase;

    @Operation(summary = "Listar logs de auditoría", description = "Obtiene una lista paginada de todos los logs de auditoría del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de logs obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> listAuditLogs(
            @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> response = listAuditLogsUseCase.execute(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Obtener log de auditoría", description = "Obtiene el detalle de un log de auditoría específico por su ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log encontrado", content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Log no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(
            @Parameter(description = "ID del log de auditoría", example = "1", required = true) @PathVariable Long id) {
        AuditLogResponse response = getAuditLogUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar logs por usuario", description = "Obtiene logs de auditoría filtrados por ID de usuario")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de logs obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> listAuditLogsByUser(
            @Parameter(description = "ID del usuario", example = "1", required = true) @PathVariable Long userId,
            @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> response = listAuditLogsByUserUseCase.execute(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar logs por módulo", description = "Obtiene logs de auditoría filtrados por módulo del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de logs obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/by-module/{module}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> listAuditLogsByModule(
            @Parameter(description = "Nombre del módulo", example = "usuarios", required = true) @PathVariable String module,
            @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> response = listAuditLogsByModuleUseCase.execute(module, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar logs por rango de fechas", description = "Obtiene logs de auditoría filtrados por rango de fechas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de logs obtenida exitosamente", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> listAuditLogsByDateRange(
            @Parameter(description = "Fecha inicio (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-01-01T00:00:00", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha fin (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-12-31T23:59:59", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Número de página (base 0)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamaño de página", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo de ordenamiento", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de ordenamiento", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> response = listAuditLogsByDateRangeUseCase.execute(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
