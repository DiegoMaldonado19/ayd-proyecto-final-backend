package com.ayd.parkcontrol.presentation.controller.branch;

import com.ayd.parkcontrol.application.dto.request.branch.CreateBranchRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateBranchRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateCapacityRequest;
import com.ayd.parkcontrol.application.dto.request.branch.UpdateScheduleRequest;
import com.ayd.parkcontrol.application.dto.response.branch.*;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.usecase.branch.*;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBranchUseCase createBranchUseCase;

    @MockitoBean
    private GetBranchUseCase getBranchUseCase;

    @MockitoBean
    private ListBranchesUseCase listBranchesUseCase;

    @MockitoBean
    private UpdateBranchUseCase updateBranchUseCase;

    @MockitoBean
    private DeleteBranchUseCase deleteBranchUseCase;

    @MockitoBean
    private GetCapacityUseCase getCapacityUseCase;

    @MockitoBean
    private UpdateCapacityUseCase updateCapacityUseCase;

    @MockitoBean
    private GetScheduleUseCase getScheduleUseCase;

    @MockitoBean
    private UpdateScheduleUseCase updateScheduleUseCase;

    @MockitoBean
    private GetOccupancyUseCase getOccupancyUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "Administrador")
    void createBranch_shouldReturnCreatedBranch() throws Exception {
        // Given
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("Sucursal Centro");
        request.setAddress("Calle Principal 123");
        request.setCapacity2r(50);
        request.setCapacity4r(30);
        request.setOpeningTime("08:00");
        request.setClosingTime("18:00");

        BranchResponse response = createBranchResponse();
        when(createBranchUseCase.execute(any(CreateBranchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Branch created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Sucursal Centro"));
    }

    @Test
    @WithMockUser(roles = { "Administrador", "Operador Sucursal" })
    void listBranches_shouldReturnPageOfBranches() throws Exception {
        // Given
        BranchResponse branch = createBranchResponse();
        PageResponse<BranchResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(branch));
        pageResponse.setTotal_elements(1L);
        pageResponse.setTotal_pages(1);
        pageResponse.setPage_number(0);
        pageResponse.setPage_size(20);

        when(listBranchesUseCase.execute(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/branches")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "name")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.total_elements").value(1));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void getBranch_shouldReturnBranchDetails() throws Exception {
        // Given
        BranchResponse response = createBranchResponse();
        when(getBranchUseCase.execute(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/branches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Sucursal Centro"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void updateBranch_shouldReturnUpdatedBranch() throws Exception {
        // Given
        UpdateBranchRequest request = new UpdateBranchRequest();
        request.setName("Sucursal Centro Actualizada");
        request.setAddress("Nueva Dirección 456");

        BranchResponse response = createBranchResponse();
        response.setName("Sucursal Centro Actualizada");
        when(updateBranchUseCase.execute(eq(1L), any(UpdateBranchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/branches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Sucursal Centro Actualizada"));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void deleteBranch_shouldReturnDeletedBranch() throws Exception {
        // Given
        BranchResponse response = createBranchResponse();
        response.setIsActive(false);
        when(deleteBranchUseCase.execute(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/v1/branches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Branch deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void getCapacity_shouldReturnCapacityDetails() throws Exception {
        // Given
        BranchCapacityResponse response = new BranchCapacityResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setCapacity2r(50);
        response.setCapacity4r(30);

        when(getCapacityUseCase.execute(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/branches/1/capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.branch_id").value(1))
                .andExpect(jsonPath("$.data.capacity_2r").value(50))
                .andExpect(jsonPath("$.data.capacity_4r").value(30));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void updateCapacity_shouldReturnUpdatedCapacity() throws Exception {
        // Given
        UpdateCapacityRequest request = new UpdateCapacityRequest();
        request.setCapacity2r(60);
        request.setCapacity4r(40);

        BranchCapacityResponse response = new BranchCapacityResponse();
        response.setBranchId(1L);
        response.setCapacity2r(60);
        response.setCapacity4r(40);

        when(updateCapacityUseCase.execute(eq(1L), any(UpdateCapacityRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/branches/1/capacity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.capacity_2r").value(60));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void getSchedule_shouldReturnScheduleDetails() throws Exception {
        // Given
        BranchScheduleResponse response = new BranchScheduleResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setOpeningTime("08:00");
        response.setClosingTime("18:00");

        when(getScheduleUseCase.execute(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/branches/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.branch_id").value(1));
    }

    @Test
    @WithMockUser(roles = "Administrador")
    void updateSchedule_shouldReturnUpdatedSchedule() throws Exception {
        // Given
        UpdateScheduleRequest request = new UpdateScheduleRequest();
        request.setOpeningTime("07:00");
        request.setClosingTime("20:00");

        BranchScheduleResponse response = new BranchScheduleResponse();
        response.setBranchId(1L);
        response.setOpeningTime("07:00");
        response.setClosingTime("20:00");

        when(updateScheduleUseCase.execute(eq(1L), any(UpdateScheduleRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/branches/1/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @WithMockUser(roles = "Operador Back Office")
    void getOccupancy_shouldReturnOccupancyDetails() throws Exception {
        // Given
        OccupancyResponse response = new OccupancyResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setOccupied2r(10);
        response.setAvailable2r(40);
        response.setOccupied4r(15);
        response.setAvailable4r(15);

        when(getOccupancyUseCase.execute(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/branches/1/occupancy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.branch_id").value(1))
                .andExpect(jsonPath("$.data.occupied_2r").value(10))
                .andExpect(jsonPath("$.data.available_2r").value(40));
    }

    @Test
    @WithMockUser(roles = "Cliente")
    void createBranch_withoutAdminRole_shouldReturnForbidden() throws Exception {
        // Given
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("Sucursal Centro");
        request.setAddress("Calle Principal 123");
        request.setCapacity2r(50);
        request.setCapacity4r(30);
        request.setOpeningTime("08:00");
        request.setClosingTime("18:00");

        // When & Then
        mockMvc.perform(post("/api/v1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "Operador Sucursal")
    void updateBranch_withoutAdminRole_shouldReturnForbidden() throws Exception {
        // Given
        UpdateBranchRequest request = new UpdateBranchRequest();
        request.setName("Sucursal Centro Actualizada");
        request.setAddress("Nueva Dirección 456");

        // When & Then
        mockMvc.perform(put("/api/v1/branches/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private BranchResponse createBranchResponse() {
        BranchResponse response = new BranchResponse();
        response.setId(1L);
        response.setName("Sucursal Centro");
        response.setAddress("Calle Principal 123");
        response.setCapacity2r(50);
        response.setCapacity4r(30);
        response.setOpeningTime("08:00");
        response.setClosingTime("18:00");
        response.setIsActive(true);
        return response;
    }
}
