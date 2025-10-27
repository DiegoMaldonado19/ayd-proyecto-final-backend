package com.ayd.parkcontrol.presentation.controller.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.CreateFleetRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetDiscountsRequest;
import com.ayd.parkcontrol.application.dto.request.fleet.UpdateFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetDiscountsResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.usecase.fleet.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fleets")
@RequiredArgsConstructor
@Tag(name = "Fleets", description = "Fleet company management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FleetController {

    private final CreateFleetUseCase createFleetUseCase;
    private final GetFleetUseCase getFleetUseCase;
    private final ListFleetsUseCase listFleetsUseCase;
    private final UpdateFleetUseCase updateFleetUseCase;
    private final DeleteFleetUseCase deleteFleetUseCase;
    private final AddVehicleToFleetUseCase addVehicleToFleetUseCase;
    private final RemoveVehicleFromFleetUseCase removeVehicleFromFleetUseCase;
    private final ListFleetVehiclesUseCase listFleetVehiclesUseCase;
    private final GetFleetDiscountsUseCase getFleetDiscountsUseCase;
    private final UpdateFleetDiscountsUseCase updateFleetDiscountsUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "List all fleets", description = "Returns a paginated list of fleet companies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<FleetResponse>> listFleets(
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FleetResponse> fleets = isActive != null
                ? listFleetsUseCase.executeByActive(isActive, pageable)
                : listFleetsUseCase.execute(pageable);
        return ResponseEntity.ok(fleets);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador')")
    @Operation(summary = "Create fleet company", description = "Creates a new fleet company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fleet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Fleet with this tax_id already exists")
    })
    public ResponseEntity<FleetResponse> createFleet(@Valid @RequestBody CreateFleetRequest request) {
        FleetResponse response = createFleetUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get fleet by ID", description = "Returns details of a specific fleet company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<FleetResponse> getFleet(@PathVariable Long id) {
        FleetResponse response = getFleetUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador')")
    @Operation(summary = "Update fleet company", description = "Updates fleet company information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fleet updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<FleetResponse> updateFleet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFleetRequest request) {
        FleetResponse response = updateFleetUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador')")
    @Operation(summary = "Delete fleet company", description = "Deletes a fleet company (only if no active vehicles)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fleet deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete fleet with active vehicles"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<Void> deleteFleet(@PathVariable Long id) {
        deleteFleetUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vehicles")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "List fleet vehicles", description = "Returns a paginated list of vehicles in a fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<Page<FleetVehicleResponse>> listFleetVehicles(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FleetVehicleResponse> vehicles = isActive != null
                ? listFleetVehiclesUseCase.executeByActive(id, isActive, pageable)
                : listFleetVehiclesUseCase.execute(id, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/{id}/vehicles")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Add vehicle to fleet", description = "Adds a new vehicle to a fleet company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or plate limit reached"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found"),
            @ApiResponse(responseCode = "409", description = "License plate already registered")
    })
    public ResponseEntity<FleetVehicleResponse> addVehicleToFleet(
            @PathVariable Long id,
            @Valid @RequestBody AddVehicleToFleetRequest request) {
        FleetVehicleResponse response = addVehicleToFleetUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Remove vehicle from fleet", description = "Removes a vehicle from a fleet company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet or vehicle not found"),
            @ApiResponse(responseCode = "400", description = "Vehicle does not belong to this fleet")
    })
    public ResponseEntity<Void> removeVehicleFromFleet(
            @PathVariable Long id,
            @PathVariable Long vehicleId) {
        removeVehicleFromFleetUseCase.execute(id, vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/discounts")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get fleet discounts", description = "Returns discount configuration for a fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<FleetDiscountsResponse> getFleetDiscounts(@PathVariable Long id) {
        FleetDiscountsResponse response = getFleetDiscountsUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/discounts")
    @PreAuthorize("hasAnyRole('Administrador')")
    @Operation(summary = "Update fleet discounts", description = "Updates discount configuration for a fleet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discounts updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Fleet not found")
    })
    public ResponseEntity<FleetDiscountsResponse> updateFleetDiscounts(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFleetDiscountsRequest request) {
        FleetDiscountsResponse response = updateFleetDiscountsUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
