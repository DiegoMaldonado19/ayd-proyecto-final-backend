package com.ayd.parkcontrol.presentation.controller.validation;

import com.ayd.parkcontrol.application.dto.request.validation.CreateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.request.validation.UpdateTemporalPermitRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.validation.TemporalPermitResponse;
import com.ayd.parkcontrol.application.usecase.validation.*;
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
@RequestMapping("/temporal-permits")
@RequiredArgsConstructor
@Tag(name = "Permisos Temporales", description = "Endpoints para gestionar permisos temporales de placas alternas")
@SecurityRequirement(name = "bearerAuth")
public class TemporalPermitController {

    private final CreateTemporalPermitUseCase createTemporalPermitUseCase;
    private final ListTemporalPermitsUseCase listTemporalPermitsUseCase;
    private final GetTemporalPermitUseCase getTemporalPermitUseCase;
    private final UpdateTemporalPermitUseCase updateTemporalPermitUseCase;
    private final RevokeTemporalPermitUseCase revokeTemporalPermitUseCase;
    private final ListActiveTemporalPermitsUseCase listActiveTemporalPermitsUseCase;
    private final ListTemporalPermitsBySubscriptionUseCase listTemporalPermitsBySubscriptionUseCase;

    @Operation(summary = "Listar permisos temporales", description = "Obtiene todos los permisos temporales del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<List<TemporalPermitResponse>>> listTemporalPermits() {
        List<TemporalPermitResponse> response = listTemporalPermitsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Emitir permiso temporal", description = "Crea un nuevo permiso temporal para una placa alterna")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permiso creado exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o suscripción ya tiene permiso activo", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para emitir", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suscripción no encontrada", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<TemporalPermitResponse>> createTemporalPermit(
            @Valid @RequestBody CreateTemporalPermitRequest request) {
        TemporalPermitResponse response = createTemporalPermitUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permiso temporal creado exitosamente"));
    }

    @Operation(summary = "Obtener permiso temporal", description = "Obtiene los detalles de un permiso temporal específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permiso encontrado", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permiso no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
    public ResponseEntity<ApiResponse<TemporalPermitResponse>> getTemporalPermit(
            @Parameter(description = "ID del permiso temporal", example = "1") @PathVariable Long id) {
        TemporalPermitResponse response = getTemporalPermitUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Actualizar permiso temporal", description = "Actualiza los datos de un permiso temporal activo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o permiso no está activo", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para actualizar", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permiso no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<TemporalPermitResponse>> updateTemporalPermit(
            @Parameter(description = "ID del permiso temporal", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateTemporalPermitRequest request) {
        TemporalPermitResponse response = updateTemporalPermitUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Permiso temporal actualizado exitosamente"));
    }

    @Operation(summary = "Revocar permiso temporal", description = "Revoca un permiso temporal activo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Permiso revocado exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Permiso no está activo", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para revocar", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Permiso no encontrado", content = @Content)
    })
    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<TemporalPermitResponse>> revokeTemporalPermit(
            @Parameter(description = "ID del permiso temporal", example = "1") @PathVariable Long id) {
        TemporalPermitResponse response = revokeTemporalPermitUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Permiso temporal revocado"));
    }

    @Operation(summary = "Listar permisos activos", description = "Obtiene todos los permisos temporales activos")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<List<TemporalPermitResponse>>> listActivePermits() {
        List<TemporalPermitResponse> response = listActiveTemporalPermitsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar permisos por suscripción", description = "Obtiene todos los permisos temporales de una suscripción específica")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = TemporalPermitResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suscripción no encontrada", content = @Content)
    })
    @GetMapping("/by-subscription/{subscriptionId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
    public ResponseEntity<ApiResponse<List<TemporalPermitResponse>>> listPermitsBySubscription(
            @Parameter(description = "ID de la suscripción", example = "1") @PathVariable Long subscriptionId) {
        List<TemporalPermitResponse> response = listTemporalPermitsBySubscriptionUseCase.execute(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
