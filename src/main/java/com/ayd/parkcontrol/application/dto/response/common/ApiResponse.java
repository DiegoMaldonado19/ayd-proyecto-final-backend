package com.ayd.parkcontrol.application.dto.response.common;

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
@Schema(description = "Generic API response")
public class ApiResponse<T> {

    @Schema(description = "Response status", example = "success")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Response message", example = "Operation completed successfully")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Response data")
    @JsonProperty("data")
    private T data;

    @Schema(description = "Timestamp", example = "2025-10-22 10:30:00")
    @JsonProperty("timestamp")
    private String timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
