package com.ayd.parkcontrol.presentation.controller.validation;

import com.ayd.parkcontrol.application.dto.request.validation.ApprovePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.request.validation.CreatePlateChangeRequest;
import com.ayd.parkcontrol.application.dto.request.validation.RejectPlateChangeRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.validation.EvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.validation.PlateChangeRequestResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/plate-changes")
@RequiredArgsConstructor
@Tag(name = "Validaciones Especiales (Back Office)", description = "Endpoints para gestionar solicitudes de cambio de placa")
@SecurityRequirement(name = "bearerAuth")
public class PlateChangeRequestController {

    private final CreatePlateChangeRequestUseCase createPlateChangeRequestUseCase;
    private final ListPlateChangeRequestsUseCase listPlateChangeRequestsUseCase;
    private final GetPlateChangeRequestUseCase getPlateChangeRequestUseCase;
    private final ApprovePlateChangeRequestUseCase approvePlateChangeRequestUseCase;
    private final RejectPlateChangeRequestUseCase rejectPlateChangeRequestUseCase;
    private final UploadPlateChangeEvidenceUseCase uploadPlateChangeEvidenceUseCase;
    private final ListPendingPlateChangeRequestsUseCase listPendingPlateChangeRequestsUseCase;
    private final ListPlateChangeRequestsByUserUseCase listPlateChangeRequestsByUserUseCase;

    @Operation(summary = "Listar solicitudes de cambio de placa", description = "Obtiene todas las solicitudes de cambio de placa del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<List<PlateChangeRequestResponse>>> listPlateChangeRequests() {
        List<PlateChangeRequestResponse> response = listPlateChangeRequestsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Solicitar cambio de placa", description = "Crea una nueva solicitud de cambio de placa vehicular")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o ya existe solicitud pendiente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Suscripción o usuario no encontrado", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<PlateChangeRequestResponse>> createPlateChangeRequest(
            @Valid @RequestBody CreatePlateChangeRequest request) {
        PlateChangeRequestResponse response = createPlateChangeRequestUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Solicitud de cambio de placa creada exitosamente"));
    }

    @Operation(summary = "Obtener solicitud de cambio de placa", description = "Obtiene los detalles de una solicitud específica")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud encontrada", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<PlateChangeRequestResponse>> getPlateChangeRequest(
            @Parameter(description = "ID de la solicitud", example = "1") @PathVariable Long id) {
        PlateChangeRequestResponse response = getPlateChangeRequestUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Aprobar cambio de placa", description = "Aprueba una solicitud de cambio de placa y actualiza la suscripción")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud aprobada exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La solicitud no está en estado PENDIENTE", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para aprobar", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<PlateChangeRequestResponse>> approvePlateChange(
            @Parameter(description = "ID de la solicitud", example = "1") @PathVariable Long id,
            @Valid @RequestBody ApprovePlateChangeRequest request) {
        PlateChangeRequestResponse response = approvePlateChangeRequestUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Solicitud aprobada exitosamente"));
    }

    @Operation(summary = "Rechazar cambio de placa", description = "Rechaza una solicitud de cambio de placa con justificación")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud rechazada exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La solicitud no está en estado PENDIENTE o falta motivo", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para rechazar", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<PlateChangeRequestResponse>> rejectPlateChange(
            @Parameter(description = "ID de la solicitud", example = "1") @PathVariable Long id,
            @Valid @RequestBody RejectPlateChangeRequest request) {
        PlateChangeRequestResponse response = rejectPlateChangeRequestUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Solicitud rechazada"));
    }

    @Operation(summary = "Subir evidencia", description = "Sube un archivo de evidencia para una solicitud de cambio de placa")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Evidencia subida exitosamente", content = @Content(schema = @Schema(implementation = EvidenceResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Archivo inválido o tipo de documento no válido", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PostMapping(value = "/{id}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<EvidenceResponse>> uploadEvidence(
            @Parameter(description = "ID de la solicitud", example = "1") @PathVariable Long id,
            @Parameter(description = "ID del tipo de documento", example = "1") @RequestParam Integer document_type_id,
            @Parameter(description = "Archivo de evidencia") @RequestParam("file") MultipartFile file) {
        EvidenceResponse response = uploadPlateChangeEvidenceUseCase.execute(id, document_type_id, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evidencia subida exitosamente"));
    }

    @Operation(summary = "Listar solicitudes pendientes", description = "Obtiene todas las solicitudes en estado PENDIENTE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<List<PlateChangeRequestResponse>>> listPendingRequests() {
        List<PlateChangeRequestResponse> response = listPendingPlateChangeRequestsUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Listar solicitudes por usuario", description = "Obtiene todas las solicitudes de un usuario específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente", content = @Content(schema = @Schema(implementation = PlateChangeRequestResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Sin permisos para acceder", content = @Content)
    })
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente', 'Operador Back Office')")
    public ResponseEntity<ApiResponse<List<PlateChangeRequestResponse>>> listRequestsByUser(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long userId) {
        List<PlateChangeRequestResponse> response = listPlateChangeRequestsByUserUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
