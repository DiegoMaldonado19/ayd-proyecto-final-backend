package com.ayd.parkcontrol.presentation.controller.report;

import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.application.usecase.report.*;
import com.ayd.parkcontrol.application.usecase.report.export.ExportBillingReportUseCase;
import com.ayd.parkcontrol.application.usecase.report.export.ExportOccupancyReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para generar y exportar reportes del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final GenerateOccupancyReportUseCase generateOccupancyReportUseCase;
    private final GenerateBillingReportUseCase generateBillingReportUseCase;
    private final GenerateSubscriptionReportUseCase generateSubscriptionReportUseCase;
    private final GenerateCommerceBenefitsReportUseCase generateCommerceBenefitsReportUseCase;
    private final GenerateCashClosingReportUseCase generateCashClosingReportUseCase;
    private final GenerateIncidentsReportUseCase generateIncidentsReportUseCase;
    private final GenerateFleetsReportUseCase generateFleetsReportUseCase;

    // Export use cases
    private final ExportOccupancyReportUseCase exportOccupancyReportUseCase;
    private final ExportBillingReportUseCase exportBillingReportUseCase;

    @Operation(summary = "Reporte de ocupación", description = "Genera reporte de ocupación de sucursales. Operador Sucursal solo ve su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<OccupancyReportResponse>>> getOccupancyReport() {
        List<OccupancyReportResponse> response = generateOccupancyReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de facturación", description = "Genera reporte de facturación por sucursal. Operador Sucursal solo ve su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/billing")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<BillingReportResponse>>> getBillingReport() {
        List<BillingReportResponse> response = generateBillingReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de suscripciones", description = "Genera reporte de suscripciones activas y vencidas. Operador Sucursal solo ve suscripciones de su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/subscriptions")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<?>>> getSubscriptionsReport() {
        List<?> response = generateSubscriptionReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de beneficios de comercios", description = "Genera reporte de horas gratis otorgadas por comercios afiliados. Operador Sucursal solo ve comercios de su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/commerce-benefits")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<?>>> getCommerceBenefitsReport() {
        List<?> response = generateCommerceBenefitsReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de cortes de caja", description = "Genera reporte de cortes de caja por periodo. Operador Sucursal solo ve su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/cash-closing")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<?>>> getCashClosingReport() {
        List<?> response = generateCashClosingReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de incidencias", description = "Genera reporte de incidencias registradas. Operador Sucursal solo ve incidencias de su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/incidents")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<ApiResponse<List<?>>> getIncidentsReport() {
        List<?> response = generateIncidentsReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de flotillas", description = "Genera reporte de flotillas empresariales y su consumo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/fleets")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<?>>> getFleetsReport() {
        List<?> response = generateFleetsReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== Endpoints de Exportación ====================

    @Operation(summary = "Exportar reporte de ocupación", description = "Exporta el reporte de ocupación en formato PDF, CSV o PNG. Operador Sucursal solo ve su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte exportado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato no soportado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @GetMapping("/occupancy/export")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<byte[]> exportOccupancyReport(
            @RequestParam(name = "format", defaultValue = "PDF") String format) {
        byte[] content = exportOccupancyReportUseCase.export(format);
        return buildExportResponse(content, format, "reporte-ocupacion");
    }

    @Operation(summary = "Exportar reporte de facturación", description = "Exporta el reporte de facturación en formato PDF, CSV o PNG. Operador Sucursal solo ve su sucursal.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte exportado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato no soportado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @GetMapping("/billing/export")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    public ResponseEntity<byte[]> exportBillingReport(
            @RequestParam(name = "format", defaultValue = "PDF") String format) {
        byte[] content = exportBillingReportUseCase.export(format);
        return buildExportResponse(content, format, "reporte-facturacion");
    }

    // ==================== Helper Methods ====================

    private ResponseEntity<byte[]> buildExportResponse(byte[] content, String format, String baseFileName) {
        String extension;
        String mediaType;

        switch (format.toUpperCase()) {
            case "PDF":
                extension = "pdf";
                mediaType = "application/pdf";
                break;
            case "CSV":
                extension = "csv";
                mediaType = "text/csv";
                break;
            case "PNG":
            case "IMAGE":
                extension = "png";
                mediaType = "image/png";
                break;
            default:
                extension = "txt";
                mediaType = "text/plain";
        }

        String fileName = baseFileName + "." + extension;

        return ResponseEntity.ok()
                .header("Content-Type", mediaType)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(content);
    }
}
