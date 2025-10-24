package com.ayd.parkcontrol.application.dto.request.branch;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UpdateScheduleRequest {

    @NotBlank(message = "Opening time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Opening time must be in format HH:mm")
    @JsonProperty("opening_time")
    private String openingTime;

    @NotBlank(message = "Closing time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Closing time must be in format HH:mm")
    @JsonProperty("closing_time")
    private String closingTime;
}
