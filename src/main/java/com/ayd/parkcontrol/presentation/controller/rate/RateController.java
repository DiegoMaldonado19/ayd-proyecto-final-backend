package com.ayd.parkcontrol.presentation.controller.rate;

import com.ayd.parkcontrol.application.dto.request.rate.CreateRateBaseRequest;
import com.ayd.parkcontrol.application.dto.request.rate.UpdateBranchRateRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.rate.RateBaseResponse;
import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.application.usecase.rate.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/rates")
@RequiredArgsConstructor
@Tag(name = "Rates", description = "Rate management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class RateController {

    private final GetCurrentRateBaseUseCase getCurrentRateBaseUseCase;
    private final GetRateBaseHistoryUseCase getRateBaseHistoryUseCase;
    private final CreateRateBaseUseCase createRateBaseUseCase;
    private final ListRateBranchesUseCase listRateBranchesUseCase;
    private final GetRateBranchUseCase getRateBranchUseCase;
    private final UpdateBranchRateUseCase updateBranchRateUseCase;
    private final DeleteBranchRateUseCase deleteBranchRateUseCase;

    @GetMapping("/base")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Get current base rate", description = "Retrieves the currently active base rate for the system.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current rate retrieved successfully", content = @Content(schema = @Schema(implementation = RateBaseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No current rate found")
    })
    public ResponseEntity<ApiResponse<RateBaseResponse>> getCurrentRate() {
        RateBaseResponse response = getCurrentRateBaseUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response, "Current rate retrieved successfully"));
    }

    @GetMapping("/base/history")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Get rate base history", description = "Retrieves the complete history of base rates ordered by start date descending.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rate history retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<RateBaseResponse>>> getRateHistory() {
        List<RateBaseResponse> response = getRateBaseHistoryUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response, "Rate history retrieved successfully"));
    }

    @PostMapping("/base")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Create new base rate", description = "Creates a new base rate and deactivates the previous one. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Rate created successfully", content = @Content(schema = @Schema(implementation = RateBaseResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<RateBaseResponse>> createBaseRate(
            @Valid @RequestBody CreateRateBaseRequest request) {
        RateBaseResponse response = createRateBaseUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Base rate created successfully"));
    }

    @GetMapping("/branches")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "List all branch rates", description = "Retrieves the rates configured for all branches. Branches without specific rate use the base rate.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch rates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<RateBranchResponse>>> listBranchRates() {
        List<RateBranchResponse> response = listRateBranchesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(response, "Branch rates retrieved successfully"));
    }

    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal', 'Operador Back Office')")
    @Operation(summary = "Get branch rate", description = "Retrieves the rate configured for a specific branch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch rate retrieved successfully", content = @Content(schema = @Schema(implementation = RateBranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<RateBranchResponse>> getBranchRate(@PathVariable Long branchId) {
        RateBranchResponse response = getRateBranchUseCase.execute(branchId);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch rate retrieved successfully"));
    }

    @PutMapping("/branches/{branchId}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Update branch rate", description = "Updates the rate for a specific branch. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch rate updated successfully", content = @Content(schema = @Schema(implementation = RateBranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<RateBranchResponse>> updateBranchRate(
            @PathVariable Long branchId,
            @Valid @RequestBody UpdateBranchRateRequest request) {
        RateBranchResponse response = updateBranchRateUseCase.execute(branchId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch rate updated successfully"));
    }

    @DeleteMapping("/branches/{branchId}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Delete branch rate", description = "Removes the specific rate from a branch, making it use the base rate. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch rate deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteBranchRate(@PathVariable Long branchId) {
        deleteBranchRateUseCase.execute(branchId);
        return ResponseEntity.ok(ApiResponse.success(null, "Branch rate deleted successfully"));
    }
}
