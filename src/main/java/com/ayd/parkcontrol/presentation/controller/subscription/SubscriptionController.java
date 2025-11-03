package com.ayd.parkcontrol.presentation.controller.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.request.subscription.RenewSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
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

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Customer subscription management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

        private final PurchaseSubscriptionUseCase purchaseSubscriptionUseCase;
        private final GetSubscriptionUseCase getSubscriptionUseCase;
        private final ListSubscriptionsUseCase listSubscriptionsUseCase;
        private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
        private final CheckSubscriptionBalanceUseCase checkSubscriptionBalanceUseCase;
        private final RenewSubscriptionUseCase renewSubscriptionUseCase;
        private final GetSubscriptionUsageUseCase getSubscriptionUsageUseCase;
        private final GetSubscriptionOveragesUseCase getSubscriptionOveragesUseCase;
        private final GetMySubscriptionUseCase getMySubscriptionUseCase;
        private final GetMySubscriptionBalanceUseCase getMySubscriptionBalanceUseCase;

        @GetMapping
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
        @Operation(summary = "List all subscriptions", description = "Returns a paginated list of all subscriptions (admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public ResponseEntity<Page<SubscriptionResponse>> listSubscriptions(
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<SubscriptionResponse> subscriptions = listSubscriptionsUseCase.execute(pageable);
                return ResponseEntity.ok(subscriptions);
        }

        @GetMapping("/by-status")
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office')")
        @Operation(summary = "List subscriptions by status", description = "Returns subscriptions filtered by status")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "403", description = "Forbidden")
        })
        public ResponseEntity<Page<SubscriptionResponse>> listSubscriptionsByStatus(
                        @RequestParam Integer statusTypeId,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<SubscriptionResponse> subscriptions = listSubscriptionsUseCase.executeByStatus(statusTypeId,
                                pageable);
                return ResponseEntity.ok(subscriptions);
        }

        @PostMapping
        @PreAuthorize("hasRole('Cliente')")
        @Operation(summary = "Purchase a new subscription", description = "Allows a customer to purchase a subscription plan")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "License plate already has active subscription")
        })
        public ResponseEntity<SubscriptionResponse> purchaseSubscription(
                        @Valid @RequestBody PurchaseSubscriptionRequest request) {
                SubscriptionResponse response = purchaseSubscriptionUseCase.executeForCurrentUser(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
        @Operation(summary = "Get subscription by ID", description = "Returns details of a specific subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found")
        })
        public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
                SubscriptionResponse response = getSubscriptionUseCase.execute(id);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('Administrador', 'Cliente')")
        @Operation(summary = "Cancel subscription", description = "Cancels an active subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Subscription cancelled successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found")
        })
        public ResponseEntity<Void> cancelSubscription(@PathVariable Long id) {
                cancelSubscriptionUseCase.execute(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}/balance")
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
        @Operation(summary = "Check subscription balance", description = "Returns remaining hours and consumption percentage")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found")
        })
        public ResponseEntity<SubscriptionBalanceResponse> checkBalance(@PathVariable Long id) {
                SubscriptionBalanceResponse response = checkSubscriptionBalanceUseCase.execute(id);
                return ResponseEntity.ok(response);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('Administrador', 'Cliente')")
        @Operation(summary = "Renew subscription", description = "Renews an existing subscription with new period")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription renewed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found"),
                        @ApiResponse(responseCode = "422", description = "Subscription cannot be renewed")
        })
        public ResponseEntity<SubscriptionResponse> renewSubscription(
                        @PathVariable Long id,
                        @Valid @RequestBody RenewSubscriptionRequest request) {
                SubscriptionResponse response = renewSubscriptionUseCase.execute(id, request);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}/usage")
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
        @Operation(summary = "Get subscription usage history", description = "Returns paginated list of all usage records for the subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Usage history retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found")
        })
        public ResponseEntity<Page<SubscriptionUsageResponse>> getSubscriptionUsage(
                        @PathVariable Long id,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<SubscriptionUsageResponse> usage = getSubscriptionUsageUseCase.execute(id, pageable);
                return ResponseEntity.ok(usage);
        }

        @GetMapping("/{id}/overages")
        @PreAuthorize("hasAnyRole('Administrador', 'Operador Back Office', 'Cliente')")
        @Operation(summary = "Get subscription overages", description = "Returns paginated list of overage charges for the subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Overages retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Subscription not found")
        })
        public ResponseEntity<Page<SubscriptionOverageResponse>> getSubscriptionOverages(
                        @PathVariable Long id,
                        @PageableDefault(size = 10) Pageable pageable) {
                Page<SubscriptionOverageResponse> overages = getSubscriptionOveragesUseCase.execute(id, pageable);
                return ResponseEntity.ok(overages);
        }

        @GetMapping("/my-subscription")
        @PreAuthorize("hasRole('Cliente')")
        @Operation(summary = "Get my active subscription", description = "Returns the active subscription of the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        public ResponseEntity<SubscriptionResponse> getMySubscription() {
                SubscriptionResponse response = getMySubscriptionUseCase.execute();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/my-subscription")
        @PreAuthorize("hasRole('Cliente')")
        @Operation(summary = "Purchase my subscription", description = "Allows authenticated user to purchase a subscription plan")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "License plate already has active subscription")
        })
        public ResponseEntity<SubscriptionResponse> purchaseMySubscription(
                        @Valid @RequestBody PurchaseSubscriptionRequest request) {
                SubscriptionResponse response = purchaseSubscriptionUseCase.executeForCurrentUser(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/my-subscription/balance")
        @PreAuthorize("hasRole('Cliente')")
        @Operation(summary = "Get my subscription balance", description = "Returns remaining hours and consumption percentage for authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        public ResponseEntity<SubscriptionBalanceResponse> getMySubscriptionBalance() {
                SubscriptionBalanceResponse response = getMySubscriptionBalanceUseCase.execute();
                return ResponseEntity.ok(response);
        }
}
