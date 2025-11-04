package com.ayd.parkcontrol.presentation.controller.auth;

import com.ayd.parkcontrol.application.dto.request.auth.*;
import com.ayd.parkcontrol.application.dto.response.auth.*;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.usecase.auth.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private LoginUseCase loginUseCase;

        @MockitoBean
        private LogoutUseCase logoutUseCase;

        @MockitoBean
        private RefreshTokenUseCase refreshTokenUseCase;

        @MockitoBean
        private Enable2FAUseCase enable2FAUseCase;

        @MockitoBean
        private Verify2FACodeUseCase verify2FACodeUseCase;

        @MockitoBean
        private Disable2FAUseCase disable2FAUseCase;

        @MockitoBean
        private ResetPasswordUseCase resetPasswordUseCase;

        @MockitoBean
        private ChangePasswordUseCase changePasswordUseCase;

        @MockitoBean
        private GetProfileUseCase getProfileUseCase;

        @MockitoBean
        private ChangePasswordWith2FAUseCase changePasswordWith2FAUseCase;

        @Test
        void login_shouldReturnLoginResponse_whenCredentialsAreValid() throws Exception {
                LoginRequest request = LoginRequest.builder()
                                .email("test@parkcontrol.com")
                                .password("password123")
                                .build();

                LoginResponse response = LoginResponse.builder()
                                .accessToken("access-token")
                                .refreshToken("refresh-token")
                                .tokenType("Bearer")
                                .userId(1L)
                                .email("test@parkcontrol.com")
                                .build();

                when(loginUseCase.execute(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.access_token").value("access-token"))
                                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
        }

        @Test
        @WithMockUser
        void logout_shouldReturnSuccess() throws Exception {
                ApiResponse<Void> response = ApiResponse.success("Logged out successfully");
                when(logoutUseCase.execute()).thenReturn(response);

                mockMvc.perform(post("/auth/logout")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"));
        }

        @Test
        void refreshToken_shouldReturnNewTokens() throws Exception {
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("refresh-token")
                                .build();

                TokenResponse response = TokenResponse.builder()
                                .accessToken("new-access-token")
                                .refreshToken("new-refresh-token")
                                .tokenType("Bearer")
                                .build();

                when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class))).thenReturn(response);

                mockMvc.perform(post("/auth/refresh")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.access_token").value("new-access-token"));
        }

        @Test
        @WithMockUser
        void enable2FA_shouldReturnSuccess() throws Exception {
                TwoFAResponse response = TwoFAResponse.builder()
                                .message("2FA enabled successfully")
                                .isEnabled(true)
                                .build();

                when(enable2FAUseCase.execute()).thenReturn(response);

                mockMvc.perform(post("/auth/2fa/enable")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("2FA enabled successfully"))
                                .andExpect(jsonPath("$.is_enabled").value(true));
        }

        @Test
        void verify2FA_shouldReturnLoginResponse_whenCodeIsValid() throws Exception {
                Verify2FARequest request = Verify2FARequest.builder()
                                .email("test@parkcontrol.com")
                                .code("123456")
                                .build();

                LoginResponse response = LoginResponse.builder()
                                .accessToken("access-token")
                                .refreshToken("refresh-token")
                                .tokenType("Bearer")
                                .build();

                when(verify2FACodeUseCase.execute(any(Verify2FARequest.class))).thenReturn(response);

                mockMvc.perform(post("/auth/2fa/verify")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.access_token").value("access-token"));
        }

        @Test
        @WithMockUser
        void disable2FA_shouldReturnSuccess() throws Exception {
                TwoFAResponse response = TwoFAResponse.builder()
                                .message("2FA disabled successfully")
                                .isEnabled(false)
                                .build();

                when(disable2FAUseCase.execute()).thenReturn(response);

                mockMvc.perform(post("/auth/2fa/disable")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("2FA disabled successfully"))
                                .andExpect(jsonPath("$.is_enabled").value(false));
        }

        @Test
        void resetPassword_shouldReturnSuccess() throws Exception {
                ResetPasswordRequest request = ResetPasswordRequest.builder()
                                .email("test@parkcontrol.com")
                                .build();

                ApiResponse<Void> response = ApiResponse.success("Reset instructions sent");
                when(resetPasswordUseCase.execute(any(ResetPasswordRequest.class))).thenReturn(response);

                mockMvc.perform(post("/auth/password/reset")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Reset instructions sent"));
        }

        @Test
        @WithMockUser
        void changePasswordWith2FA_shouldReturnSuccess() throws Exception {
                ChangePasswordWith2FARequest request = ChangePasswordWith2FARequest.builder()
                                .newPassword("newPassword123")
                                .confirmPassword("newPassword123")
                                .code("123456")
                                .build();

                ApiResponse<Void> response = ApiResponse.success("Password changed successfully");
                when(changePasswordWith2FAUseCase.execute(any(ChangePasswordWith2FARequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/auth/password/change")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }

        @Test
        @WithMockUser
        void changePasswordLegacy_shouldReturnSuccess() throws Exception {
                ChangePasswordRequest request = ChangePasswordRequest.builder()
                                .currentPassword("oldPassword123")
                                .newPassword("newPassword123")
                                .confirmPassword("newPassword123")
                                .build();

                ApiResponse<Void> response = ApiResponse.success("Password changed successfully");
                when(changePasswordUseCase.execute(any(ChangePasswordRequest.class))).thenReturn(response);

                mockMvc.perform(post("/auth/password/change/legacy")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"));
        }

        @Test
        @WithMockUser
        void getProfile_shouldReturnUserProfile() throws Exception {
                UserProfileResponse response = UserProfileResponse.builder()
                                .userId(1L)
                                .email("test@parkcontrol.com")
                                .firstName("John")
                                .lastName("Doe")
                                .role("ADMIN")
                                .build();

                when(getProfileUseCase.execute()).thenReturn(response);

                mockMvc.perform(get("/auth/profile")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@parkcontrol.com"))
                                .andExpect(jsonPath("$.first_name").value("John"));
        }

        @Test
        void login_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
                mockMvc.perform(get("/auth/profile"))
                                .andExpect(status().isUnauthorized());
        }
}
