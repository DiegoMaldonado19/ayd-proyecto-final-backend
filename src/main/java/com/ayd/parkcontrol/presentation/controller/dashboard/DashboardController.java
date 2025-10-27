package com.ayd.parkcontrol.presentation.controller.dashboard;

import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.DashboardOverviewResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.OccupancyDetailResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.RevenueTodayResponse;
import com.ayd.parkcontrol.application.dto.response.dashboard.SystemAlertResponse;
import com.ayd.parkcontrol.application.usecase.dashboard.*;
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
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para métricas y visualización en tiempo real del sistema")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final GetDashboardOverviewUseCase getDashboardOverviewUseCase;
    private final GetOccupancyDetailsUseCase getOccupancyDetailsUseCase;
    private final GetRevenueTodayUseCase getRevenueTodayUseCase;
    private final GetActiveSubscriptionsCountUseCase getActiveSubscriptionsCountUseCase;
    private final GetSystemAlertsUseCase getSystemAlertsUseCase;
    private final GetDashboardByBranchUseCase getDashboardByBranchUseCase;

    @Operation(summary = "Resumen general", description = "Obtiene las métricas principales del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente", content = @Content(schema = @Schema(implementation = DashboardOverviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview() {
        DashboardOverviewResponse response = getDashboardOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Ocupación en tiempo real", description = "Obtiene detalles de ocupación de todas las sucursales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Datos de ocupación obtenidos exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<OccupancyDetailResponse>>> getOccupancy() {
        List<OccupancyDetailResponse> response = getOccupancyDetailsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Ingresos del día", description = "Obtiene el resumen de ingresos del día actual")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ingresos obtenidos exitosamente", content = @Content(schema = @Schema(implementation = RevenueTodayResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<RevenueTodayResponse>> getRevenue() {
        RevenueTodayResponse response = getRevenueTodayUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Suscripciones activas", description = "Obtiene el conteo de suscripciones activas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/active-subscriptions")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<Long>> getActiveSubscriptions() {
        Long response = getActiveSubscriptionsCountUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Alertas del sistema", description = "Obtiene las alertas activas del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alertas obtenidas exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<SystemAlertResponse>>> getAlerts() {
        List<SystemAlertResponse> response = getSystemAlertsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Dashboard por sucursal", description = "Obtiene métricas específicas de una sucursal")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente", content = @Content(schema = @Schema(implementation = DashboardOverviewResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @GetMapping("/by-branch/{branchId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getDashboardByBranch(
            @Parameter(description = "ID de la sucursal", example = "1", required = true) @PathVariable Long branchId) {
        DashboardOverviewResponse response = getDashboardByBranchUseCase.execute(branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
