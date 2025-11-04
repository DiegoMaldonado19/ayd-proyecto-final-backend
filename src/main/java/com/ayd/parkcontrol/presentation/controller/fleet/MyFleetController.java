package com.ayd.parkcontrol.presentation.controller.fleet;

import com.ayd.parkcontrol.application.dto.request.fleet.AddVehicleToFleetRequest;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetConsumptionResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetResponse;
import com.ayd.parkcontrol.application.dto.response.fleet.FleetVehicleResponse;
import com.ayd.parkcontrol.application.usecase.fleet.AddVehicleToMyFleetUseCase;
import com.ayd.parkcontrol.application.usecase.fleet.GetMyFleetConsumptionUseCase;
import com.ayd.parkcontrol.application.usecase.fleet.GetMyFleetUseCase;
import com.ayd.parkcontrol.application.usecase.fleet.ListMyFleetVehiclesUseCase;
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

/**
 * Controller for fleet administrators to manage their own fleets.
 * All endpoints require the "Administrador Flotilla" role.
 */
@RestController
@RequestMapping("/my-fleet")
@RequiredArgsConstructor
@Tag(name = "My Fleet", description = "Endpoints para administradores de flotilla para gestionar su propia flotilla")
@SecurityRequirement(name = "bearerAuth")
public class MyFleetController {

    private final GetMyFleetUseCase getMyFleetUseCase;
    private final ListMyFleetVehiclesUseCase listMyFleetVehiclesUseCase;
    private final AddVehicleToMyFleetUseCase addVehicleToMyFleetUseCase;
    private final GetMyFleetConsumptionUseCase getMyFleetConsumptionUseCase;

    @GetMapping
    @PreAuthorize("hasRole('Administrador Flotilla')")
    @Operation(summary = "Obtener mi flotilla", description = "Retorna la información de la flotilla asociada al administrador autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flotilla obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos - Requiere rol Administrador Flotilla"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene una flotilla asignada")
    })
    public ResponseEntity<FleetResponse> getMyFleet() {
        FleetResponse response = getMyFleetUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('Administrador Flotilla')")
    @Operation(summary = "Listar vehículos de mi flotilla", description = "Retorna lista paginada de todos los vehículos pertenecientes a la flotilla del administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos - Requiere rol Administrador Flotilla"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene una flotilla asignada")
    })
    public ResponseEntity<Page<FleetVehicleResponse>> getMyFleetVehicles(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FleetVehicleResponse> vehicles = listMyFleetVehiclesUseCase.execute(pageable);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/vehicles")
    @PreAuthorize("hasRole('Administrador Flotilla')")
    @Operation(summary = "Agregar vehículo a mi flotilla", description = "Permite al administrador agregar un nuevo vehículo a su flotilla")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehículo agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o vehículo ya existe en la flotilla"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos - Requiere rol Administrador Flotilla"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene una flotilla asignada")
    })
    public ResponseEntity<FleetVehicleResponse> addVehicle(
            @Valid @RequestBody AddVehicleToFleetRequest request) {
        FleetVehicleResponse response = addVehicleToMyFleetUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consumption")
    @PreAuthorize("hasRole('Administrador Flotilla')")
    @Operation(summary = "Obtener consumo de mi flotilla", description = "Retorna estadísticas de consumo de estacionamiento de la flotilla del administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consumo obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos - Requiere rol Administrador Flotilla"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene una flotilla asignada")
    })
    public ResponseEntity<FleetConsumptionResponse> getMyFleetConsumption() {
        FleetConsumptionResponse response = getMyFleetConsumptionUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
