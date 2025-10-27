package com.ayd.parkcontrol.application.dto.response.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("module")
    private String module;

    @JsonProperty("entity")
    private String entity;

    @JsonProperty("operation_type")
    private String operationType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("previous_values")
    private String previousValues;

    @JsonProperty("new_values")
    private String newValues;

    @JsonProperty("client_ip")
    private String clientIp;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
