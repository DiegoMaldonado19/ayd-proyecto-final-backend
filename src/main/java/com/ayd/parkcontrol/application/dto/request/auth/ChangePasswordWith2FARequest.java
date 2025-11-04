package com.ayd.parkcontrol.application.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password with 2FA code request")
public class ChangePasswordWith2FARequest {

    @Schema(description = "2FA code received via email", example = "123456", required = true)
    @NotBlank(message = "2FA code is required")
    @Size(min = 6, max = 6, message = "2FA code must be 6 digits")
    @JsonProperty("code")
    private String code;

    @Schema(description = "New password", example = "NewPassword123!", required = true)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty("new_password")
    private String newPassword;

    @Schema(description = "Confirm new password", example = "NewPassword123!", required = true)
    @NotBlank(message = "Password confirmation is required")
    @JsonProperty("confirm_password")
    private String confirmPassword;
}
