package com.ayd.parkcontrol.presentation.controller.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.CreateVehicleRequest;
import com.ayd.parkcontrol.application.dto.request.vehicle.UpdateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.application.usecase.vehicle.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final GetVehicleByIdUseCase getVehicleByIdUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final GetVehicleTypesUseCase getVehicleTypesUseCase;
    private final GetMyVehiclesUseCase getMyVehiclesUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "List all vehicles", description = "Returns a paginated list of all vehicles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<VehicleResponse>> listVehicles(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<VehicleResponse> vehicles = listVehiclesUseCase.execute(pageable);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Register vehicle", description = "Registers a new vehicle for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Vehicle with this license plate already exists for the user")
    })
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            @RequestParam Long userId) {
        VehicleResponse response = createVehicleUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Operador Sucursal')")
    @Operation(summary = "Get vehicle by ID", description = "Returns details of a specific vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable Long id) {
        VehicleResponse response = getVehicleByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Update vehicle", description = "Updates vehicle information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "409", description = "Vehicle with this license plate already exists for the user")
    })
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request,
            @RequestParam Long userId) {
        VehicleResponse response = updateVehicleUseCase.execute(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Delete vehicle", description = "Soft deletes a vehicle (sets is_active to false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long id,
            @RequestParam Long userId) {
        deleteVehicleUseCase.execute(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Operador Sucursal', 'Cliente')")
    @Operation(summary = "Get vehicle types", description = "Returns all available vehicle types (2R, 4R)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle types retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<VehicleTypeResponse>> getVehicleTypes() {
        List<VehicleTypeResponse> types = getVehicleTypesUseCase.execute();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/my-vehicles")
    @PreAuthorize("hasRole('Cliente')")
    @Operation(summary = "Get my vehicles", description = "Returns all vehicles for the authenticated customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        Integer userId = jwtTokenProvider.getUserIdFromToken(token);
        List<VehicleResponse> vehicles = getMyVehiclesUseCase.execute(userId.longValue());
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/my-vehicles")
    @PreAuthorize("hasRole('Cliente')")
    @Operation(summary = "Register my vehicle", description = "Allows a customer to register their own vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Vehicle with this license plate already exists for you")
    })
    public ResponseEntity<VehicleResponse> createMyVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        Integer userId = jwtTokenProvider.getUserIdFromToken(token);
        VehicleResponse response = createVehicleUseCase.execute(request, userId.longValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
