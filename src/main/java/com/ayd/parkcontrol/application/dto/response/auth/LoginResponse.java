package com.ayd.parkcontrol.application.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response")
public class LoginResponse {

    @Schema(description = "Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "Token expiration time in milliseconds", example = "3600000")
    @JsonProperty("expires_in")
    private Long expiresIn;

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "User email", example = "user@parkcontrol.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "User full name", example = "John Doe")
    @JsonProperty("full_name")
    private String fullName;

    @Schema(description = "User role", example = "ADMIN")
    @JsonProperty("role")
    private String role;

    @Schema(description = "Indicates if 2FA is enabled", example = "true")
    @JsonProperty("has_2fa_enabled")
    private Boolean has2faEnabled;

    @Schema(description = "Indicates if 2FA verification is required", example = "false")
    @JsonProperty("requires_2fa_verification")
    private Boolean requires2faVerification;

    @Schema(description = "Indicates if password change is required", example = "false")
    @JsonProperty("requires_password_change")
    private Boolean requiresPasswordChange;
}
