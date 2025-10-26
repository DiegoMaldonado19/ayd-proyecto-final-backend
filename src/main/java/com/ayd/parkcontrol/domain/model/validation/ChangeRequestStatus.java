package com.ayd.parkcontrol.domain.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestStatus {

    private Integer id;
    private String code;
    private String name;
    private LocalDateTime createdAt;
}
