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
@Schema(description = "2FA response")
public class TwoFAResponse {

    @Schema(description = "Status message", example = "2FA code sent to email")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Indicates if 2FA is enabled", example = "true")
    @JsonProperty("is_enabled")
    private Boolean isEnabled;

    @Schema(description = "User email (masked)", example = "u***@parkcontrol.com")
    @JsonProperty("email")
    private String email;
}
