package com.ayd.parkcontrol.presentation.controller.admin;

import com.ayd.parkcontrol.application.dto.request.admin.CreateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserStatusRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.usecase.admin.*;
import com.ayd.parkcontrol.domain.exception.DuplicateEmailException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private CreateUserUseCase createUserUseCase;

        @MockitoBean
        private ListUsersUseCase listUsersUseCase;

        @MockitoBean
        private GetUserUseCase getUserUseCase;

        @MockitoBean
        private UpdateUserUseCase updateUserUseCase;

        @MockitoBean
        private DeleteUserUseCase deleteUserUseCase;

        @MockitoBean
        private UpdateUserStatusUseCase updateUserStatusUseCase;

        @MockitoBean
        private ListUsersByRoleUseCase listUsersByRoleUseCase;

        @MockitoBean
        private ListUsersByStatusUseCase listUsersByStatusUseCase;

        private UserResponse mockUserResponse;
        private CreateUserRequest createUserRequest;

        @BeforeEach
        void setUp() {
                mockUserResponse = UserResponse.builder()
                                .id(1L)
                                .email("test@parkcontrol.com")
                                .first_name("Test")
                                .last_name("User")
                                .role_type_id(2)
                                .role_name("Operador Sucursal")
                                .is_active(true)
                                .requires_password_change(true)
                                .has_2fa_enabled(false)
                                .failed_login_attempts(0)
                                .build();

                createUserRequest = CreateUserRequest.builder()
                                .email("test@parkcontrol.com")
                                .password("TempPass123!")
                                .first_name("Test")
                                .last_name("User")
                                .phone("50012345678")
                                .role_type_id(2)
                                .is_active(true)
                                .build();
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void listUsers_shouldReturnPageOfUsers_whenAuthenticated() throws Exception {
                PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                .content(Collections.singletonList(mockUserResponse))
                                .page_number(0)
                                .page_size(20)
                                .total_elements(1L)
                                .total_pages(1)
                                .is_first(true)
                                .is_last(true)
                                .has_previous(false)
                                .has_next(false)
                                .build();

                when(listUsersUseCase.execute(anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(pageResponse);

                mockMvc.perform(get("/users")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDirection", "desc")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.content[0].email").value("test@parkcontrol.com"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void createUser_shouldReturnCreatedUser_whenValidRequest() throws Exception {
                when(createUserUseCase.execute(any(CreateUserRequest.class)))
                                .thenReturn(mockUserResponse);

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createUserRequest))
                                .with(csrf()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.email").value("test@parkcontrol.com"))
                                .andExpect(jsonPath("$.data.first_name").value("Test"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
                when(createUserUseCase.execute(any(CreateUserRequest.class)))
                                .thenThrow(new DuplicateEmailException("test@parkcontrol.com"));

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createUserRequest))
                                .with(csrf()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void createUser_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
                CreateUserRequest invalidRequest = CreateUserRequest.builder()
                                .email("invalid-email")
                                .password("TempPass123!")
                                .first_name("Test")
                                .last_name("User")
                                .role_type_id(2)
                                .build();

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getUser_shouldReturnUser_whenUserExists() throws Exception {
                when(getUserUseCase.execute(1L)).thenReturn(mockUserResponse);

                mockMvc.perform(get("/users/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.email").value("test@parkcontrol.com"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void getUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
                when(getUserUseCase.execute(999L))
                                .thenThrow(new UserNotFoundException(999L));

                mockMvc.perform(get("/users/999")
                                .accept(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateUser_shouldReturnUpdatedUser_whenValidRequest() throws Exception {
                UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                                .first_name("Updated")
                                .last_name("Name")
                                .build();

                UserResponse updatedResponse = UserResponse.builder()
                                .id(1L)
                                .email("test@parkcontrol.com")
                                .first_name("Updated")
                                .last_name("Name")
                                .role_type_id(2)
                                .build();

                when(updateUserUseCase.execute(any(Long.class), any(UpdateUserRequest.class)))
                                .thenReturn(updatedResponse);

                mockMvc.perform(put("/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.first_name").value("Updated"))
                                .andExpect(jsonPath("$.data.last_name").value("Name"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void deleteUser_shouldReturnSuccess_whenUserExists() throws Exception {
                mockMvc.perform(delete("/users/1")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Usuario eliminado exitosamente"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void updateUserStatus_shouldReturnUpdatedUser_whenValidRequest() throws Exception {
                UpdateUserStatusRequest statusRequest = UpdateUserStatusRequest.builder()
                                .is_active(false)
                                .build();

                UserResponse updatedResponse = UserResponse.builder()
                                .id(1L)
                                .email("test@parkcontrol.com")
                                .first_name("Test")
                                .last_name("User")
                                .is_active(false)
                                .build();

                when(updateUserStatusUseCase.execute(any(Long.class), any(UpdateUserStatusRequest.class)))
                                .thenReturn(updatedResponse);

                mockMvc.perform(patch("/users/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(statusRequest))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.is_active").value(false));
        }

        @Test
        void listUsers_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/users")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void listUsers_shouldReturnForbidden_whenInsufficientPermissions() throws Exception {
                mockMvc.perform(get("/users")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void listUsersByRole_shouldReturnUsers_whenRoleIdProvided() throws Exception {
                // Given
                Integer roleId = 1;
                PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                .content(Collections.singletonList(UserResponse.builder()
                                                .id(1L)
                                                .email("admin@parkcontrol.com")
                                                .role_name("Administrador")
                                                .is_active(true)
                                                .build()))
                                .page_number(0)
                                .page_size(20)
                                .total_elements(1L)
                                .total_pages(1)
                                .build();

                when(listUsersByRoleUseCase.execute(any(Integer.class), anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(pageResponse);

                // When & Then
                mockMvc.perform(get("/users/by-role/{roleId}", roleId)
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDirection", "desc")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.content[0].email").value("admin@parkcontrol.com"))
                                .andExpect(jsonPath("$.data.content[0].role_name").value("Administrador"));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void listUsersByStatus_shouldReturnActiveUsers_whenIsActiveTrue() throws Exception {
                // Given
                PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                .content(Collections.singletonList(UserResponse.builder()
                                                .id(1L)
                                                .email("active@parkcontrol.com")
                                                .role_name("Cliente")
                                                .is_active(true)
                                                .build()))
                                .page_number(0)
                                .page_size(20)
                                .total_elements(1L)
                                .total_pages(1)
                                .build();

                when(listUsersByStatusUseCase.execute(any(Boolean.class), anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(pageResponse);

                // When & Then
                mockMvc.perform(get("/users/by-status")
                                .param("isActive", "true")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDirection", "desc")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.content[0].is_active").value(true));
        }

        @Test
        @WithMockUser(roles = "Administrador")
        void listUsersByStatus_shouldReturnInactiveUsers_whenIsActiveFalse() throws Exception {
                // Given
                PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                .content(Collections.singletonList(UserResponse.builder()
                                                .id(2L)
                                                .email("inactive@parkcontrol.com")
                                                .role_name("Cliente")
                                                .is_active(false)
                                                .build()))
                                .page_number(0)
                                .page_size(20)
                                .total_elements(1L)
                                .total_pages(1)
                                .build();

                when(listUsersByStatusUseCase.execute(any(Boolean.class), anyInt(), anyInt(), anyString(), anyString()))
                                .thenReturn(pageResponse);

                // When & Then
                mockMvc.perform(get("/users/by-status")
                                .param("isActive", "false")
                                .param("page", "0")
                                .param("size", "20")
                                .param("sortBy", "createdAt")
                                .param("sortDirection", "desc")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.content[0].is_active").value(false));
        }

        @Test
        void listUsersByRole_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/users/by-role/1")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void listUsersByRole_shouldReturnForbidden_whenInsufficientPermissions() throws Exception {
                mockMvc.perform(get("/users/by-role/1")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void listUsersByStatus_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/users/by-status")
                                .param("isActive", "true")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "Cliente")
        void listUsersByStatus_shouldReturnForbidden_whenInsufficientPermissions() throws Exception {
                mockMvc.perform(get("/users/by-status")
                                .param("isActive", "true")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }
}
