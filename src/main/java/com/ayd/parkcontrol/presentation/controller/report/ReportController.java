package com.ayd.parkcontrol.presentation.controller.report;

import com.ayd.parkcontrol.application.dto.request.report.ExportReportRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.application.usecase.report.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ExportReportUseCase exportReportUseCase;

    @Operation(summary = "Reporte de ocupación", description = "Genera reporte de ocupación de sucursales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/occupancy")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<OccupancyReportResponse>>> getOccupancyReport() {
        List<OccupancyReportResponse> response = generateOccupancyReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de facturación", description = "Genera reporte de facturación por sucursal")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/billing")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<BillingReportResponse>>> getBillingReport() {
        List<BillingReportResponse> response = generateBillingReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de suscripciones", description = "Genera reporte de suscripciones activas y vencidas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<Object>>> getSubscriptionsReport() {
        List<Object> response = generateSubscriptionReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de beneficios de comercios", description = "Genera reporte de horas gratis otorgadas por comercios afiliados")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/commerce-benefits")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<Object>>> getCommerceBenefitsReport() {
        List<Object> response = generateCommerceBenefitsReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de cortes de caja", description = "Genera reporte de cortes de caja por periodo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/cash-closing")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<Object>>> getCashClosingReport() {
        List<Object> response = generateCashClosingReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Reporte de incidencias", description = "Genera reporte de incidencias registradas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte generado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/incidents")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ApiResponse<List<Object>>> getIncidentsReport() {
        List<Object> response = generateIncidentsReportUseCase.execute();
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
    public ResponseEntity<ApiResponse<List<Object>>> getFleetsReport() {
        List<Object> response = generateFleetsReportUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Exportar reporte", description = "Exporta un reporte en formato PDF, Excel o Imagen")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporte exportado exitosamente", content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @PostMapping("/export")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<byte[]> exportReport(@Valid @RequestBody ExportReportRequest request) {
        byte[] fileContent = exportReportUseCase.execute(request);

        String fileName = "report." + request.getExportFormat().toLowerCase();
        MediaType mediaType = switch (request.getExportFormat().toUpperCase()) {
            case "PDF" -> MediaType.APPLICATION_PDF;
            case "EXCEL" -> MediaType.parseMediaType("application/vnd.ms-excel");
            case "IMAGE" -> MediaType.IMAGE_PNG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileContent);
    }
}
