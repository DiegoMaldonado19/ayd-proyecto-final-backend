package com.ayd.parkcontrol.presentation.controller.ticket;

import com.ayd.parkcontrol.application.dto.request.ticket.ApplyBenefitRequest;
import com.ayd.parkcontrol.application.dto.request.ticket.RegisterEntryRequest;
import com.ayd.parkcontrol.application.dto.response.ticket.BusinessFreeHoursResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketChargeResponse;
import com.ayd.parkcontrol.application.dto.response.ticket.TicketResponse;
import com.ayd.parkcontrol.application.usecase.ticket.*;
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
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket operations (vehicle entry/exit)")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final RegisterVehicleEntryUseCase registerVehicleEntryUseCase;
    private final GetTicketByIdUseCase getTicketByIdUseCase;
    private final ProcessVehicleExitUseCase processVehicleExitUseCase;
    private final CalculateTicketChargeUseCase calculateTicketChargeUseCase;
    private final ApplyCommerceBenefitUseCase applyCommerceBenefitUseCase;
    private final GetActiveTicketsUseCase getActiveTicketsUseCase;
    private final GetTicketsByBranchUseCase getTicketsByBranchUseCase;
    private final GetTicketsByPlateUseCase getTicketsByPlateUseCase;
    private final GetTicketByFolioUseCase getTicketByFolioUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Administrador')")
    @Operation(summary = "Register vehicle entry", description = "Creates a new ticket for vehicle entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle entry registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Insufficient capacity or vehicle already has active ticket")
    })
    public ResponseEntity<TicketResponse> registerEntry(@Valid @RequestBody RegisterEntryRequest request) {
        TicketResponse response = registerVehicleEntryUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador')")
    @Operation(summary = "Get ticket by ID", description = "Returns ticket details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        TicketResponse response = getTicketByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/exit")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Administrador')")
    @Operation(summary = "Register vehicle exit", description = "Processes vehicle exit and calculates charges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle exit processed successfully"),
            @ApiResponse(responseCode = "400", description = "Ticket is not in progress or already has exit"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> registerExit(@PathVariable Long id) {
        TicketResponse response = processVehicleExitUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/calculate-charge")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador')")
    @Operation(summary = "Calculate ticket charge", description = "Calculates the charge for a ticket without processing exit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Charge calculated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketChargeResponse> calculateCharge(@PathVariable Long id) {
        TicketChargeResponse response = calculateTicketChargeUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply-benefit")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Administrador')")
    @Operation(summary = "Apply commerce benefit", description = "Applies free hours benefit from affiliated commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Benefit applied successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or ticket not in progress"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<BusinessFreeHoursResponse> applyBenefit(
            @PathVariable Long id,
            @Valid @RequestBody ApplyBenefitRequest request) {
        BusinessFreeHoursResponse response = applyCommerceBenefitUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador')")
    @Operation(summary = "Get active tickets", description = "Returns all tickets currently in progress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active tickets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<TicketResponse>> getActiveTickets() {
        List<TicketResponse> response = getActiveTicketsUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-branch/{branchId}")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador')")
    @Operation(summary = "Get tickets by branch", description = "Returns all tickets for a specific branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<TicketResponse>> getTicketsByBranch(@PathVariable Long branchId) {
        List<TicketResponse> response = getTicketsByBranchUseCase.execute(branchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-plate/{plate}")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador', 'Cliente')")
    @Operation(summary = "Get tickets by license plate", description = "Returns all tickets for a specific license plate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<TicketResponse>> getTicketsByPlate(@PathVariable String plate) {
        List<TicketResponse> response = getTicketsByPlateUseCase.execute(plate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-folio/{folio}")
    @PreAuthorize("hasAnyRole('Operador Sucursal', 'Operador Back Office', 'Administrador')")
    @Operation(summary = "Get ticket by folio", description = "Returns ticket details by folio number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> getTicketByFolio(@PathVariable String folio) {
        TicketResponse response = getTicketByFolioUseCase.execute(folio);
        return ResponseEntity.ok(response);
    }
}
