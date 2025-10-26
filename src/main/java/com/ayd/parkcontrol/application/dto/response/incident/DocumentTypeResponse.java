package com.ayd.parkcontrol.application.dto.response.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponse {

    private Integer id;
    private String code;
    private String name;
    private String description;
}
