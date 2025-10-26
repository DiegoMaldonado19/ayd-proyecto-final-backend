package com.ayd.parkcontrol.presentation.controller.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.CreateSubscriptionPlanRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionPlanResponse;
import com.ayd.parkcontrol.application.dto.request.subscription.UpdateSubscriptionPlanRequest;
import com.ayd.parkcontrol.application.usecase.subscription.*;
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
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Subscription plan management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionPlanController {

    private final CreateSubscriptionPlanUseCase createSubscriptionPlanUseCase;
    private final UpdateSubscriptionPlanUseCase updateSubscriptionPlanUseCase;
    private final GetSubscriptionPlanUseCase getSubscriptionPlanUseCase;
    private final ListSubscriptionPlansUseCase listSubscriptionPlansUseCase;
    private final DeleteSubscriptionPlanUseCase deleteSubscriptionPlanUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "List all active subscription plans", description = "Returns a list of all active subscription plans ordered by hierarchy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<SubscriptionPlanResponse>> listPlans() {
        List<SubscriptionPlanResponse> plans = listSubscriptionPlansUseCase.execute();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
    @Operation(summary = "List subscription plans with pagination", description = "Returns a paginated list of subscription plans")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<SubscriptionPlanResponse>> listPlansPaginated(
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SubscriptionPlanResponse> plans = listSubscriptionPlansUseCase.execute(isActive, pageable);
        return ResponseEntity.ok(plans);
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Create a new subscription plan", description = "Creates a new subscription plan with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Plan type already exists")
    })
    public ResponseEntity<SubscriptionPlanResponse> createPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        SubscriptionPlanResponse response = createSubscriptionPlanUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
    @Operation(summary = "Get subscription plan by ID", description = "Returns details of a specific subscription plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<SubscriptionPlanResponse> getPlan(@PathVariable Long id) {
        SubscriptionPlanResponse response = getSubscriptionPlanUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Update subscription plan", description = "Updates an existing subscription plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        SubscriptionPlanResponse response = updateSubscriptionPlanUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Delete subscription plan", description = "Soft deletes a subscription plan (sets isActive to false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        deleteSubscriptionPlanUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
