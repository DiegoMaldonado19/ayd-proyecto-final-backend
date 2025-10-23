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
@Schema(description = "User profile response")
public class UserProfileResponse {

    @Schema(description = "User ID", example = "1")
    @JsonProperty("user_id")
    private Long userId;

    @Schema(description = "User email", example = "user@parkcontrol.com")
    @JsonProperty("email")
    private String email;

    @Schema(description = "First name", example = "John")
    @JsonProperty("first_name")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @JsonProperty("last_name")
    private String lastName;

    @Schema(description = "Phone number", example = "12345678")
    @JsonProperty("phone")
    private String phone;

    @Schema(description = "User role", example = "ADMIN")
    @JsonProperty("role")
    private String role;

    @Schema(description = "Indicates if 2FA is enabled", example = "true")
    @JsonProperty("has_2fa_enabled")
    private Boolean has2faEnabled;

    @Schema(description = "Indicates if account is active", example = "true")
    @JsonProperty("is_active")
    private Boolean isActive;

    @Schema(description = "Last login timestamp", example = "2025-10-22 10:30:00")
    @JsonProperty("last_login")
    private String lastLogin;
}
