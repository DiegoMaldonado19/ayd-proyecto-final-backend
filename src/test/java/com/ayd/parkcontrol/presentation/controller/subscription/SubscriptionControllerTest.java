package com.ayd.parkcontrol.presentation.controller.subscription;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.request.subscription.RenewSubscriptionRequest;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionBalanceResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionOverageResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionResponse;
import com.ayd.parkcontrol.application.dto.response.subscription.SubscriptionUsageResponse;
import com.ayd.parkcontrol.application.usecase.subscription.*;
import com.ayd.parkcontrol.infrastructure.security.SubscriptionSecurityEvaluator;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para SubscriptionController.
 * Valida todos los endpoints de gestión de suscripciones.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PurchaseSubscriptionUseCase purchaseSubscriptionUseCase;

        @MockitoBean
        private GetSubscriptionUseCase getSubscriptionUseCase;

        @MockitoBean
        private ListSubscriptionsUseCase listSubscriptionsUseCase;

        @MockitoBean
        private CancelSubscriptionUseCase cancelSubscriptionUseCase;

        @MockitoBean
        private CheckSubscriptionBalanceUseCase checkSubscriptionBalanceUseCase;

        @MockitoBean
        private RenewSubscriptionUseCase renewSubscriptionUseCase;

        @MockitoBean
        private GetSubscriptionUsageUseCase getSubscriptionUsageUseCase;

        @MockitoBean
        private GetSubscriptionOveragesUseCase getSubscriptionOveragesUseCase;

        @MockitoBean
        private GetMySubscriptionUseCase getMySubscriptionUseCase;

        @MockitoBean
        private GetMySubscriptionBalanceUseCase getMySubscriptionBalanceUseCase;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private SubscriptionSecurityEvaluator subscriptionSecurityEvaluator;

        @Test
        @WithMockUser(roles = "Administrador")
        void listSubscriptions_shouldReturnPageOfSubscriptions() throws Exception {
                SubscriptionResponse subscription = createSubscriptionResponse();
                Page<SubscriptionResponse> page = new PageImpl<>(List.of(subscription), PageRequest.of(0, 10), 1);

                when(listSubscriptionsUseCase.execute(any())).thenReturn(page);

                mockMvc.perform(get("/subscriptions")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].id").value(1))
                                .andExpect(jsonPath("$.totalElements").value(1));

                verify(listSubscriptionsUseCase).execute(any());
        }

        @Test
        @WithMockUser(roles = "Operador Back Office")
        void listSubscriptionsByStatus_shouldReturnFilteredSubscriptions() throws Exception {
                SubscriptionResponse subscription = createSubscriptionResponse();
                Page<SubscriptionResponse> page = new PageImpl<>(List.of(subscription), PageRequest.of(0, 10), 1);

                when(listSubscriptionsUseCase.executeByStatus(eq(1), any())).thenReturn(page);

                mockMvc.perform(get("/subscriptions/by-status")
                                .param("statusTypeId", "1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].id").value(1));

                verify(listSubscriptionsUseCase).executeByStatus(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void purchaseSubscription_shouldCreateSubscription() throws Exception {
                SubscriptionResponse response = createSubscriptionResponse();

                when(purchaseSubscriptionUseCase.executeForCurrentUser(any())).thenReturn(response);

                String requestJson = """
                                {
                                    "plan_id": 1,
                                    "license_plate": "ABC123",
                                    "is_annual": false,
                                    "auto_renew_enabled": true
                                }
                                """;

                mockMvc.perform(post("/subscriptions")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.license_plate").value("ABC123"));

                verify(purchaseSubscriptionUseCase).executeForCurrentUser(any(PurchaseSubscriptionRequest.class));
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void getSubscription_shouldReturnSubscriptionDetails() throws Exception {
                SubscriptionResponse response = createSubscriptionResponse();

                when(getSubscriptionUseCase.execute(1L)).thenReturn(response);

                mockMvc.perform(get("/subscriptions/1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.license_plate").value("ABC123"));

                verify(getSubscriptionUseCase).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void cancelSubscription_shouldReturnNoContent() throws Exception {
                doNothing().when(cancelSubscriptionUseCase).execute(1L);

                mockMvc.perform(delete("/subscriptions/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent());

                verify(cancelSubscriptionUseCase).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void checkBalance_shouldReturnSubscriptionBalance() throws Exception {
                SubscriptionBalanceResponse balance = SubscriptionBalanceResponse.builder()
                                .subscriptionId(1L)
                                .monthlyHours(100)
                                .consumedHours(new BigDecimal("25.00"))
                                .remainingHours(new BigDecimal("75.00"))
                                .consumptionPercentage(new BigDecimal("25.00"))
                                .build();

                when(checkSubscriptionBalanceUseCase.execute(1L)).thenReturn(balance);

                mockMvc.perform(get("/subscriptions/1/balance")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscription_id").value(1))
                                .andExpect(jsonPath("$.monthly_hours").value(100))
                                .andExpect(jsonPath("$.consumed_hours").value(25.00))
                                .andExpect(jsonPath("$.remaining_hours").value(75.00));

                verify(checkSubscriptionBalanceUseCase).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void renewSubscription_shouldReturnRenewedSubscription() throws Exception {
                SubscriptionResponse response = createSubscriptionResponse();

                when(subscriptionSecurityEvaluator.isOwner(1L)).thenReturn(true);
                when(renewSubscriptionUseCase.execute(eq(1L), any())).thenReturn(response);

                String requestJson = """
                                {
                                    "is_annual": true,
                                    "auto_renew_enabled": false
                                }
                                """;

                mockMvc.perform(put("/subscriptions/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));

                verify(renewSubscriptionUseCase).execute(eq(1L), any(RenewSubscriptionRequest.class));
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void getSubscriptionUsage_shouldReturnUsageHistory() throws Exception {
                SubscriptionUsageResponse usage = SubscriptionUsageResponse.builder()
                                .ticketId(100L)
                                .folio("TICKET-001")
                                .hoursConsumed(new BigDecimal("5.00"))
                                .entryTime(LocalDateTime.now().minusHours(5))
                                .exitTime(LocalDateTime.now())
                                .build();

                Page<SubscriptionUsageResponse> page = new PageImpl<>(List.of(usage), PageRequest.of(0, 10), 1);

                when(getSubscriptionUsageUseCase.execute(eq(1L), any())).thenReturn(page);

                mockMvc.perform(get("/subscriptions/1/usage")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].ticket_id").value(100))
                                .andExpect(jsonPath("$.content[0].hours_consumed").value(5.00));

                verify(getSubscriptionUsageUseCase).execute(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void getSubscriptionOverages_shouldReturnOverageCharges() throws Exception {
                SubscriptionOverageResponse overage = SubscriptionOverageResponse.builder()
                                .ticketId(100L)
                                .overageHours(new BigDecimal("2.50"))
                                .rateApplied(new BigDecimal("15.00"))
                                .amountCharged(new BigDecimal("37.50"))
                                .chargeDate(LocalDateTime.now())
                                .build();

                Page<SubscriptionOverageResponse> page = new PageImpl<>(List.of(overage), PageRequest.of(0, 10), 1);

                when(getSubscriptionOveragesUseCase.execute(eq(1L), any())).thenReturn(page);

                mockMvc.perform(get("/subscriptions/1/overages")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].ticket_id").value(100))
                                .andExpect(jsonPath("$.content[0].overage_hours").value(2.50));

                verify(getSubscriptionOveragesUseCase).execute(eq(1L), any());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void getMySubscription_shouldReturnUserActiveSubscription() throws Exception {
                SubscriptionResponse response = createSubscriptionResponse();

                when(getMySubscriptionUseCase.execute()).thenReturn(response);

                mockMvc.perform(get("/subscriptions/my-subscription")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.license_plate").value("ABC123"));

                verify(getMySubscriptionUseCase).execute();
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void purchaseMySubscription_shouldCreateSubscription() throws Exception {
                SubscriptionResponse response = createSubscriptionResponse();

                when(purchaseSubscriptionUseCase.executeForCurrentUser(any())).thenReturn(response);

                String requestJson = """
                                {
                                    "plan_id": 1,
                                    "license_plate": "ABC123",
                                    "is_annual": false,
                                    "auto_renew_enabled": true
                                }
                                """;

                mockMvc.perform(post("/subscriptions/my-subscription")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1));

                verify(purchaseSubscriptionUseCase).executeForCurrentUser(any(PurchaseSubscriptionRequest.class));
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void getMySubscriptionBalance_shouldReturnBalance() throws Exception {
                SubscriptionBalanceResponse balance = SubscriptionBalanceResponse.builder()
                                .subscriptionId(1L)
                                .monthlyHours(100)
                                .consumedHours(new BigDecimal("30.00"))
                                .remainingHours(new BigDecimal("70.00"))
                                .consumptionPercentage(new BigDecimal("30.00"))
                                .build();

                when(getMySubscriptionBalanceUseCase.execute()).thenReturn(balance);

                mockMvc.perform(get("/subscriptions/my-subscription/balance")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.subscription_id").value(1))
                                .andExpect(jsonPath("$.remaining_hours").value(70.00));

                verify(getMySubscriptionBalanceUseCase).execute();
        }

        private SubscriptionResponse createSubscriptionResponse() {
                return SubscriptionResponse.builder()
                                .id(1L)
                                .userId(100L)
                                .licensePlate("ABC123")
                                .statusName("Activa")
                                .purchaseDate(LocalDateTime.now().minusDays(10))
                                .startDate(LocalDateTime.now().minusDays(10))
                                .endDate(LocalDateTime.now().plusDays(20))
                                .frozenRateBase(new BigDecimal("12.00"))
                                .isAnnual(false)
                                .autoRenewEnabled(true)
                                .consumedHours(new BigDecimal("25.00"))
                                .build();
        }
}
