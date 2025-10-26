package com.ayd.parkcontrol.presentation.controller.commerce;

import com.ayd.parkcontrol.application.dto.request.commerce.CreateCommerceRequest;
import com.ayd.parkcontrol.application.dto.response.commerce.CommerceResponse;
import com.ayd.parkcontrol.application.usecase.commerce.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.name").value("Restaurant El Portal"))
                .andExpect(jsonPath("$.tax_id").value("12345678-9"));
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
