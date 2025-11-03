package com.ayd.parkcontrol.presentation.controller.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.ConfigureBenefitRequest;
import com.ayd.parkcontrol.application.dto.request.commerce.CreateCommerceRequest;
import com.ayd.parkcontrol.application.dto.request.commerce.UpdateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.BenefitResponse;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.application.usecase.commerce.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommerceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private CreateCommerceUseCase createCommerceUseCase;

        @MockitoBean
        private GetAllCommercesUseCase getAllCommercesUseCase;

        @MockitoBean
        private GetCommerceByIdUseCase getCommerceByIdUseCase;

        @MockitoBean
        private UpdateCommerceUseCase updateCommerceUseCase;

        @MockitoBean
        private DeleteCommerceUseCase deleteCommerceUseCase;

        @MockitoBean
        private ConfigureBenefitUseCase configureBenefitUseCase;

        @MockitoBean
        private GetCommerceBenefitsUseCase getCommerceBenefitsUseCase;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @Test
        @WithMockUser(roles = "Administrador")
        void getAllCommerces_shouldReturnCommercesPage() throws Exception {
                CommerceResponse commerce = CommerceResponse.builder()
                                .id(1L)
                                .name("Test Commerce")
                                .taxId("123456789")
                                .ratePerHour(BigDecimal.valueOf(8.00))
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .build();

                Page<CommerceResponse> page = new PageImpl<>(Arrays.asList(commerce));
                when(getAllCommercesUseCase.execute(any(Pageable.class))).thenReturn(page);

                mockMvc.perform(get("/commerces")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1))
                                .andExpect(jsonPath("$.content[0].name").value("Test Commerce"));

                verify(getAllCommercesUseCase, times(1)).execute(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void getAllCommerces_asOperadorSucursal_shouldBeAllowed() throws Exception {
                Page<CommerceResponse> page = new PageImpl<>(Arrays.asList());
                when(getAllCommercesUseCase.execute(any(Pageable.class))).thenReturn(page);

                mockMvc.perform(get("/commerces")
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void createCommerce_ShouldReturnCreated_WhenValidRequest() throws Exception {
                CreateCommerceRequest request = CreateCommerceRequest.builder()
                                .name("Restaurant El Portal")
                                .taxId("12345678-9")
                                .contactName("Juan Perez")
                                .email("contact@elportal.com")
                                .phone("50212345678")
                                .ratePerHour(BigDecimal.valueOf(8.00))
                                .build();

                CommerceResponse response = CommerceResponse.builder()
                                .id(1L)
                                .name("Restaurant El Portal")
                                .taxId("12345678-9")
                                .contactName("Juan Perez")
                                .email("contact@elportal.com")
                                .phone("50212345678")
                                .ratePerHour(BigDecimal.valueOf(8.00))
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                when(createCommerceUseCase.execute(any(CreateCommerceRequest.class))).thenReturn(response);

                mockMvc.perform(post("/commerces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Restaurant El Portal"));

                verify(createCommerceUseCase, times(1)).execute(any(CreateCommerceRequest.class));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void createCommerce_asOperadorSucursal_shouldBeForbidden() throws Exception {
                CreateCommerceRequest request = CreateCommerceRequest.builder()
                                .name("New Commerce")
                                .taxId("987654321")
                                .ratePerHour(BigDecimal.valueOf(10.00))
                                .build();

                mockMvc.perform(post("/commerces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());

                verify(createCommerceUseCase, never()).execute(any(CreateCommerceRequest.class));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getCommerceById_ShouldReturnCommerce_WhenExists() throws Exception {
                CommerceResponse response = CommerceResponse.builder()
                                .id(1L)
                                .name("Restaurant El Portal")
                                .taxId("12345678-9")
                                .ratePerHour(BigDecimal.valueOf(8.00))
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                when(getCommerceByIdUseCase.execute(1L)).thenReturn(response);

                mockMvc.perform(get("/commerces/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Restaurant El Portal"));

                verify(getCommerceByIdUseCase, times(1)).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void getCommerceById_asOperadorSucursal_shouldBeAllowed() throws Exception {
                CommerceResponse response = CommerceResponse.builder()
                                .id(1L)
                                .name("Test Commerce")
                                .build();

                when(getCommerceByIdUseCase.execute(1L)).thenReturn(response);

                mockMvc.perform(get("/commerces/1")
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateCommerce_shouldReturnUpdatedCommerce() throws Exception {
                UpdateCommerceRequest request = UpdateCommerceRequest.builder()
                                .name("Updated Commerce")
                                .contactName("Updated Contact")
                                .email("updated@test.com")
                                .phone("50299998888")
                                .ratePerHour(BigDecimal.valueOf(12.00))
                                .build();

                CommerceResponse response = CommerceResponse.builder()
                                .id(1L)
                                .name("Updated Commerce")
                                .contactName("Updated Contact")
                                .email("updated@test.com")
                                .phone("50299998888")
                                .ratePerHour(BigDecimal.valueOf(12.00))
                                .build();

                when(updateCommerceUseCase.execute(eq(1L), any(UpdateCommerceRequest.class))).thenReturn(response);

                mockMvc.perform(put("/commerces/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Updated Commerce"));

                verify(updateCommerceUseCase, times(1)).execute(eq(1L), any(UpdateCommerceRequest.class));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void updateCommerce_asOperadorSucursal_shouldBeForbidden() throws Exception {
                UpdateCommerceRequest request = UpdateCommerceRequest.builder()
                                .name("Updated Commerce")
                                .build();

                mockMvc.perform(put("/commerces/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());

                verify(updateCommerceUseCase, never()).execute(anyLong(), any(UpdateCommerceRequest.class));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void deleteCommerce_shouldReturnNoContent() throws Exception {
                doNothing().when(deleteCommerceUseCase).execute(1L);

                mockMvc.perform(delete("/commerces/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(deleteCommerceUseCase, times(1)).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void deleteCommerce_asOperadorSucursal_shouldBeForbidden() throws Exception {
                mockMvc.perform(delete("/commerces/1")
                                .with(csrf()))
                                .andExpect(status().isForbidden());

                verify(deleteCommerceUseCase, never()).execute(anyLong());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getCommerceBenefits_shouldReturnBenefitsList() throws Exception {
                BenefitResponse benefit1 = BenefitResponse.builder()
                                .id(1L)
                                .businessId(1L)
                                .businessName("Test Commerce")
                                .branchId(1L)
                                .branchName("Main Branch")
                                .benefitTypeCode("DISCOUNT")
                                .benefitTypeName("Discount")
                                .settlementPeriodCode("MONTHLY")
                                .settlementPeriodName("Monthly")
                                .isActive(true)
                                .build();

                BenefitResponse benefit2 = BenefitResponse.builder()
                                .id(2L)
                                .businessId(1L)
                                .businessName("Test Commerce")
                                .branchId(2L)
                                .branchName("Second Branch")
                                .benefitTypeCode("HOURS")
                                .benefitTypeName("Free Hours")
                                .settlementPeriodCode("WEEKLY")
                                .settlementPeriodName("Weekly")
                                .isActive(true)
                                .build();

                List<BenefitResponse> benefits = Arrays.asList(benefit1, benefit2);
                when(getCommerceBenefitsUseCase.execute(1L)).thenReturn(benefits);

                mockMvc.perform(get("/commerces/1/benefits")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].branch_id").value(1))
                                .andExpect(jsonPath("$[1].id").value(2))
                                .andExpect(jsonPath("$[1].branch_id").value(2));

                verify(getCommerceBenefitsUseCase, times(1)).execute(1L);
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void getCommerceBenefits_asOperadorSucursal_shouldBeAllowed() throws Exception {
                when(getCommerceBenefitsUseCase.execute(1L)).thenReturn(Arrays.asList());

                mockMvc.perform(get("/commerces/1/benefits")
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void configureBenefit_shouldReturnCreatedBenefit() throws Exception {
                ConfigureBenefitRequest request = ConfigureBenefitRequest.builder()
                                .branchId(1L)
                                .benefitType("DISCOUNT")
                                .settlementPeriod("MONTHLY")
                                .build();

                BenefitResponse response = BenefitResponse.builder()
                                .id(1L)
                                .businessId(1L)
                                .businessName("Test Commerce")
                                .branchId(1L)
                                .branchName("Main Branch")
                                .benefitTypeCode("DISCOUNT")
                                .benefitTypeName("Discount")
                                .settlementPeriodCode("MONTHLY")
                                .settlementPeriodName("Monthly")
                                .isActive(true)
                                .build();

                when(configureBenefitUseCase.execute(eq(1L), any(ConfigureBenefitRequest.class))).thenReturn(response);

                mockMvc.perform(post("/commerces/1/benefits")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.business_id").value(1))
                                .andExpect(jsonPath("$.branch_id").value(1))
                                .andExpect(jsonPath("$.benefit_type_code").value("DISCOUNT"));

                verify(configureBenefitUseCase, times(1)).execute(eq(1L), any(ConfigureBenefitRequest.class));
        }

        @Test
        @WithMockUser(roles = "Operador Sucursal")
        void configureBenefit_asOperadorSucursal_shouldBeForbidden() throws Exception {
                ConfigureBenefitRequest request = ConfigureBenefitRequest.builder()
                                .branchId(1L)
                                .benefitType("DISCOUNT")
                                .settlementPeriod("MONTHLY")
                                .build();

                mockMvc.perform(post("/commerces/1/benefits")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());

                verify(configureBenefitUseCase, never()).execute(anyLong(), any(ConfigureBenefitRequest.class));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getCommerceBenefits_whenNoBenefits_shouldReturnEmptyList() throws Exception {
                when(getCommerceBenefitsUseCase.execute(1L)).thenReturn(Arrays.asList());

                mockMvc.perform(get("/commerces/1/benefits")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void createCommerce_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
                CreateCommerceRequest request = CreateCommerceRequest.builder()
                                .name("Restaurant El Portal")
                                .taxId("12345678-9")
                                .ratePerHour(BigDecimal.valueOf(8.00))
                                .build();

                mockMvc.perform(post("/commerces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }
}
