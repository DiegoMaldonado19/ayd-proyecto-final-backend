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
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @Schema(description = "Current password", example = "OldPassword123!", required = true)
    @NotBlank(message = "Current password is required")
    @JsonProperty("current_password")
    private String currentPassword;

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
