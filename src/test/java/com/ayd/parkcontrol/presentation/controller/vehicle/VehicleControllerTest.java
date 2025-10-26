package com.ayd.parkcontrol.presentation.controller.vehicle;

import com.ayd.parkcontrol.application.dto.request.vehicle.CreateVehicleRequest;
import com.ayd.parkcontrol.application.dto.request.vehicle.UpdateVehicleRequest;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleResponse;
import com.ayd.parkcontrol.application.dto.response.vehicle.VehicleTypeResponse;
import com.ayd.parkcontrol.application.usecase.vehicle.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateVehicleUseCase createVehicleUseCase;

    @MockitoBean
    private UpdateVehicleUseCase updateVehicleUseCase;

    @MockitoBean
    private DeleteVehicleUseCase deleteVehicleUseCase;

    @MockitoBean
    private GetVehicleByIdUseCase getVehicleByIdUseCase;

    @MockitoBean
    private ListVehiclesUseCase listVehiclesUseCase;

    @MockitoBean
    private GetVehicleTypesUseCase getVehicleTypesUseCase;

    @MockitoBean
    private GetMyVehiclesUseCase getMyVehiclesUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "Administrador")
    void listVehicles_shouldReturnVehiclesPage() throws Exception {
        VehicleResponse vehicle = VehicleResponse.builder()
                .id(1L)
                .userId(1L)
                .licensePlate("ABC-123")
                .build();

        Page<VehicleResponse> page = new PageImpl<>(Arrays.asList(vehicle));
        when(listVehiclesUseCase.execute(any())).thenReturn(page);

        mockMvc.perform(get("/vehicles")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void createVehicle_shouldReturnCreatedVehicle() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .brand("Toyota")
                .model("Corolla")
                .build();

        VehicleResponse response = VehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(createVehicleUseCase.execute(any(), anyLong())).thenReturn(response);

        mockMvc.perform(post("/vehicles")
                .with(csrf())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void getVehicle_shouldReturnVehicle() throws Exception {
        VehicleResponse response = VehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(getVehicleByIdUseCase.execute(1L)).thenReturn(response);

        mockMvc.perform(get("/vehicles/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void updateVehicle_shouldReturnUpdatedVehicle() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .brand("Honda")
                .model("Civic")
                .build();

        VehicleResponse response = VehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(updateVehicleUseCase.execute(anyLong(), any(), anyLong())).thenReturn(response);

        mockMvc.perform(put("/vehicles/1")
                .with(csrf())
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void deleteVehicle_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/vehicles/1")
                .with(csrf())
                .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void getVehicleTypes_shouldReturnAllTypes() throws Exception {
        VehicleTypeResponse type1 = VehicleTypeResponse.builder()
                .id(1)
                .code("2R")
                .name("Dos Ruedas")
                .build();

        VehicleTypeResponse type2 = VehicleTypeResponse.builder()
                .id(2)
                .code("4R")
                .name("Cuatro Ruedas")
                .build();

        List<VehicleTypeResponse> types = Arrays.asList(type1, type2);
        when(getVehicleTypesUseCase.execute()).thenReturn(types);

        mockMvc.perform(get("/vehicles/types")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("2R"))
                .andExpect(jsonPath("$[1].code").value("4R"));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void getMyVehicles_shouldReturnUserVehicles() throws Exception {
        VehicleResponse vehicle = VehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn(1);
        when(getMyVehiclesUseCase.execute(1L)).thenReturn(Arrays.asList(vehicle));

        mockMvc.perform(get("/vehicles/my-vehicles")
                .with(csrf())
                .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void createMyVehicle_shouldReturnCreatedVehicle() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("ABC-123")
                .vehicleTypeId(1)
                .build();

        VehicleResponse response = VehicleResponse.builder()
                .id(1L)
                .licensePlate("ABC-123")
                .build();

        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn(1);
        when(createVehicleUseCase.execute(any(), anyLong())).thenReturn(response);

        mockMvc.perform(post("/vehicles/my-vehicles")
                .with(csrf())
                .header("Authorization", "Bearer fake-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
