package com.ayd.parkcontrol.presentation.controller.settlement;

import com.ayd.parkcontrol.application.dto.request.settlement.GenerateSettlementRequest;
import com.ayd.parkcontrol.application.dto.response.settlement.SettlementResponse;
import com.ayd.parkcontrol.application.usecase.settlement.GenerateSettlementUseCase;
import com.ayd.parkcontrol.application.usecase.settlement.GetSettlementByIdUseCase;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BusinessSettlementHistoryEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBusinessSettlementHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlements", description = "Commerce settlement management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SettlementController {

    private final GenerateSettlementUseCase generateSettlementUseCase;
    private final GetSettlementByIdUseCase getSettlementByIdUseCase;
    private final JpaBusinessSettlementHistoryRepository settlementRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get all settlements", description = "Retrieve all settlements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<BusinessSettlementHistoryEntity>> getAllSettlements() {
        List<BusinessSettlementHistoryEntity> settlements = settlementRepository.findAll();
        return ResponseEntity.ok(settlements);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Generate settlement", description = "Generate a new settlement for a commerce and branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Settlement generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Commerce or branch not found"),
            @ApiResponse(responseCode = "409", description = "No unsettled hours found for the period")
    })
    public ResponseEntity<SettlementResponse> generateSettlement(
            @Valid @RequestBody GenerateSettlementRequest request) {
        SettlementResponse settlement = generateSettlementUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlement);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get settlement by ID", description = "Retrieve a settlement by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Settlement not found")
    })
    public ResponseEntity<SettlementResponse> getSettlementById(@PathVariable Long id) {
        SettlementResponse settlement = getSettlementByIdUseCase.execute(id);
        return ResponseEntity.ok(settlement);
    }

    @GetMapping("/by-commerce/{commerceId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get settlements by commerce", description = "Retrieve all settlements for a specific commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<BusinessSettlementHistoryEntity>> getSettlementsByCommerce(
            @PathVariable Long commerceId) {
        List<BusinessSettlementHistoryEntity> settlements = settlementRepository
                .findByBusinessIdOrderBySettledAtDesc(commerceId);
        return ResponseEntity.ok(settlements);
    }

    @GetMapping("/by-period")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "Get settlements by period", description = "Retrieve settlements within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<BusinessSettlementHistoryEntity>> getSettlementsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<BusinessSettlementHistoryEntity> settlements = settlementRepository.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(settlements);
    }
}
