package com.ayd.parkcontrol.application.dto.response.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypeResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;
}
