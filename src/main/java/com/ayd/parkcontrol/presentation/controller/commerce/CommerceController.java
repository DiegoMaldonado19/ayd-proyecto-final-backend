package com.ayd.parkcontrol.presentation.controller.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.ConfigureBenefitRequest;
import com.ayd.parkcontrol.application.dto.request.commerce.CreateCommerceRequest;
import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.application.usecase.commerce.*;
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

import java.util.List;

@RestController
@RequestMapping("/commerces")
@RequiredArgsConstructor
@Tag(name = "Commerces", description = "Affiliated commerce management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CommerceController {

    private final CreateCommerceUseCase createCommerceUseCase;
    private final GetAllCommercesUseCase getAllCommercesUseCase;
    private final GetCommerceByIdUseCase getCommerceByIdUseCase;
    private final UpdateCommerceUseCase updateCommerceUseCase;
    private final DeleteCommerceUseCase deleteCommerceUseCase;
    private final ConfigureBenefitUseCase configureBenefitUseCase;
    private final GetCommerceBenefitsUseCase getCommerceBenefitsUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    @Operation(summary = "Get all commerces", description = "Retrieve paginated list of all affiliated commerces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commerces retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CommerceResponse>> getAllCommerces(@PageableDefault(size = 20) Pageable pageable) {
        Page<CommerceResponse> commerces = getAllCommercesUseCase.execute(pageable);
        return ResponseEntity.ok(commerces);
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Create commerce", description = "Create a new affiliated commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commerce created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Commerce with tax ID already exists")
    })
    public ResponseEntity<CommerceResponse> createCommerce(@Valid @RequestBody CreateCommerceRequest request) {
        CommerceResponse commerce = createCommerceUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(commerce);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    @Operation(summary = "Get commerce by ID", description = "Retrieve a commerce by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commerce found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Commerce not found")
    })
    public ResponseEntity<CommerceResponse> getCommerceById(@PathVariable Long id) {
        CommerceResponse commerce = getCommerceByIdUseCase.execute(id);
        return ResponseEntity.ok(commerce);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Update commerce", description = "Update an existing commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commerce updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Commerce not found")
    })
    public ResponseEntity<CommerceResponse> updateCommerce(@PathVariable Long id,
            @Valid @RequestBody UpdateCommerceRequest request) {
        CommerceResponse commerce = updateCommerceUseCase.execute(id, request);
        return ResponseEntity.ok(commerce);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Delete commerce", description = "Delete a commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Commerce deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Commerce not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete commerce with active benefits")
    })
    public ResponseEntity<Void> deleteCommerce(@PathVariable Long id) {
        deleteCommerceUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/benefits")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Sucursal')")
    @Operation(summary = "Get commerce benefits", description = "Get all benefits configured for a commerce")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Benefits retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<BenefitResponse>> getCommerceBenefits(@PathVariable Long id) {
        List<BenefitResponse> benefits = getCommerceBenefitsUseCase.execute(id);
        return ResponseEntity.ok(benefits);
    }

    @PostMapping("/{id}/benefits")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Configure benefit", description = "Configure a new benefit for a commerce at a specific branch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Benefit configured successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Commerce or branch not found"),
            @ApiResponse(responseCode = "409", description = "Benefit already configured at this branch")
    })
    public ResponseEntity<BenefitResponse> configureBenefit(@PathVariable Long id,
            @Valid @RequestBody ConfigureBenefitRequest request) {
        BenefitResponse benefit = configureBenefitUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(benefit);
    }
}
