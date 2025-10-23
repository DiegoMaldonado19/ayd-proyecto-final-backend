package com.ayd.parkcontrol.application.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "2FA verification request")
public class Verify2FARequest {

    @Schema(description = "6-digit verification code", example = "123456", required = true)
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code must be 6 digits")
    @JsonProperty("code")
    private String code;

    @Schema(description = "User email", example = "user@parkcontrol.com", required = true)
    @NotBlank(message = "Email is required")
    @JsonProperty("email")
    private String email;
}
