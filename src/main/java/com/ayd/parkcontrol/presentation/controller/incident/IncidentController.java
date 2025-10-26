package com.ayd.parkcontrol.presentation.controller.incident;

import com.ayd.parkcontrol.application.dto.request.incident.RegisterIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.ResolveIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.UpdateIncidentRequest;
import com.ayd.parkcontrol.application.dto.request.incident.UploadEvidenceRequest;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentEvidenceResponse;
import com.ayd.parkcontrol.application.dto.response.incident.IncidentResponse;
import com.ayd.parkcontrol.application.usecase.incident.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident management operations")
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    private final ListIncidentsUseCase listIncidentsUseCase;
    private final RegisterIncidentUseCase registerIncidentUseCase;
    private final GetIncidentByIdUseCase getIncidentByIdUseCase;
    private final UpdateIncidentUseCase updateIncidentUseCase;
    private final UploadEvidenceUseCase uploadEvidenceUseCase;
    private final ListEvidencesByIncidentUseCase listEvidencesByIncidentUseCase;
    private final ResolveIncidentUseCase resolveIncidentUseCase;
    private final GetIncidentsByBranchUseCase getIncidentsByBranchUseCase;
    private final GetIncidentsByTypeUseCase getIncidentsByTypeUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "List all incidents", description = "Retrieves all incidents in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<IncidentResponse>> listIncidents() {
        List<IncidentResponse> incidents = listIncidentsUseCase.execute();
        return ResponseEntity.ok(incidents);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Register a new incident", description = "Creates a new incident record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incident registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket, incident type, or branch not found")
    })
    public ResponseEntity<IncidentResponse> registerIncident(@Valid @RequestBody RegisterIncidentRequest request) {
        IncidentResponse incident = registerIncidentUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(incident);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Get incident by ID", description = "Retrieves a specific incident by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable Long id) {
        IncidentResponse incident = getIncidentByIdUseCase.execute(id);
        return ResponseEntity.ok(incident);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Update incident", description = "Updates an existing incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentRequest request) {
        IncidentResponse incident = updateIncidentUseCase.execute(id, request);
        return ResponseEntity.ok(incident);
    }

    @PostMapping("/{id}/evidence")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Upload evidence", description = "Uploads evidence for an incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evidence uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Incident or document type not found")
    })
    public ResponseEntity<IncidentEvidenceResponse> uploadEvidence(
            @PathVariable Long id,
            @Valid @RequestBody UploadEvidenceRequest request) {
        IncidentEvidenceResponse evidence = uploadEvidenceUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
    }

    @GetMapping("/{id}/evidence")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "List incident evidences", description = "Retrieves all evidences for a specific incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evidences retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<List<IncidentEvidenceResponse>> listEvidences(@PathVariable Long id) {
        List<IncidentEvidenceResponse> evidences = listEvidencesByIncidentUseCase.execute(id);
        return ResponseEntity.ok(evidences);
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Resolve incident", description = "Marks an incident as resolved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident resolved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or incident already resolved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    public ResponseEntity<IncidentResponse> resolveIncident(
            @PathVariable Long id,
            @Valid @RequestBody ResolveIncidentRequest request) {
        IncidentResponse incident = resolveIncidentUseCase.execute(id, request);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/by-branch/{branchId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Get incidents by branch", description = "Retrieves all incidents for a specific branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<IncidentResponse>> getIncidentsByBranch(@PathVariable Long branchId) {
        List<IncidentResponse> incidents = getIncidentsByBranchUseCase.execute(branchId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get incidents by type", description = "Retrieves all incidents of a specific type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<IncidentResponse>> getIncidentsByType(@PathVariable Integer type) {
        List<IncidentResponse> incidents = getIncidentsByTypeUseCase.execute(type);
        return ResponseEntity.ok(incidents);
    }
}
