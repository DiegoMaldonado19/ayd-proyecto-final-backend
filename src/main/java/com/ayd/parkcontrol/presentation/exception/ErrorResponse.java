package com.ayd.parkcontrol.presentation.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    @JsonProperty("status")
    private Integer status;

    @Schema(description = "Error type", example = "Bad Request")
    @JsonProperty("error")
    private String error;

    @Schema(description = "Error message", example = "Validation failed")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/auth/login")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Timestamp", example = "2025-10-22T10:30:00")
    @JsonProperty("timestamp")
    private String timestamp;

    @Schema(description = "Validation errors")
    @JsonProperty("validation_errors")
    private Map<String, String> validationErrors;
}
