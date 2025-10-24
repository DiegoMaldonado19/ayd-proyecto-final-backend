package com.ayd.parkcontrol.presentation.controller.branch;

import com.ayd.parkcontrol.application.dto.request.branch.CreateBranchRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateBranchRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateCapacityRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateScheduleRequest;
import com.ayd.parkcontrol.application.dto.response.branch.*;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.usecase.branch.*;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Branch management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BranchController {

    private final CreateBranchUseCase createBranchUseCase;
    private final GetBranchUseCase getBranchUseCase;
    private final ListBranchesUseCase listBranchesUseCase;
    private final UpdateBranchUseCase updateBranchUseCase;
    private final DeleteBranchUseCase deleteBranchUseCase;
    private final GetCapacityUseCase getCapacityUseCase;
    private final UpdateCapacityUseCase updateCapacityUseCase;
    private final GetScheduleUseCase getScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final GetOccupancyUseCase getOccupancyUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new branch", description = "Creates a new branch in the system. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Branch created successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Branch name already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse response = createBranchUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Branch created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'BACKOFFICE')")
    @Operation(summary = "List all branches", description = "Retrieves a paginated list of branches. Can be filtered by active status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branches retrieved successfully")
    })
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> listBranches(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive) {

        PageResponse<BranchResponse> response = listBranchesUseCase.execute(page, size, sortBy, sortDirection,
                isActive);
        return ResponseEntity.ok(ApiResponse.success(response, "Branches retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'BACKOFFICE')")
    @Operation(summary = "Get branch by ID", description = "Retrieves detailed information about a specific branch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch retrieved successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id) {
        BranchResponse response = getBranchUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update branch", description = "Updates an existing branch. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch updated successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Branch name already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse response = updateBranchUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete branch (soft delete)", description = "Soft deletes a branch by setting its active status to false. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branch deleted successfully", content = @Content(schema = @Schema(implementation = BranchResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<BranchResponse>> deleteBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id) {
        BranchResponse response = deleteBranchUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Branch deleted successfully"));
    }

    @GetMapping("/{id}/capacity")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'BACKOFFICE')")
    @Operation(summary = "Get branch capacity", description = "Retrieves the capacity information for a specific branch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Capacity retrieved successfully", content = @Content(schema = @Schema(implementation = BranchCapacityResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<BranchCapacityResponse>> getCapacity(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id) {
        BranchCapacityResponse response = getCapacityUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Capacity retrieved successfully"));
    }

    @PutMapping("/{id}/capacity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update branch capacity", description = "Updates the capacity for 2-wheel and 4-wheel vehicles at a branch. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Capacity updated successfully", content = @Content(schema = @Schema(implementation = BranchCapacityResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<BranchCapacityResponse>> updateCapacity(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCapacityRequest request) {
        BranchCapacityResponse response = updateCapacityUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Capacity updated successfully"));
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'BACKOFFICE', 'CLIENT')")
    @Operation(summary = "Get branch schedule", description = "Retrieves the operating hours for a specific branch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule retrieved successfully", content = @Content(schema = @Schema(implementation = BranchScheduleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<BranchScheduleResponse>> getSchedule(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id) {
        BranchScheduleResponse response = getScheduleUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule retrieved successfully"));
    }

    @PutMapping("/{id}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update branch schedule", description = "Updates the operating hours for a branch. Only accessible by administrators.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule updated successfully", content = @Content(schema = @Schema(implementation = BranchScheduleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation")
    })
    public ResponseEntity<ApiResponse<BranchScheduleResponse>> updateSchedule(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        BranchScheduleResponse response = updateScheduleUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Schedule updated successfully"));
    }

    @GetMapping("/{id}/occupancy")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'BACKOFFICE')")
    @Operation(summary = "Get real-time branch occupancy", description = "Retrieves real-time occupancy information for a specific branch, including available spaces by vehicle type.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Occupancy retrieved successfully", content = @Content(schema = @Schema(implementation = OccupancyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<ApiResponse<OccupancyResponse>> getOccupancy(
            @Parameter(description = "Branch ID", required = true) @PathVariable Long id) {
        OccupancyResponse response = getOccupancyUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Occupancy retrieved successfully"));
    }
}
