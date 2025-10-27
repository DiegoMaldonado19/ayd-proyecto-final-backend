package com.ayd.parkcontrol.domain.model.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationType {

    private Integer id;
    private String code;
    private String name;
    private LocalDateTime createdAt;
}
