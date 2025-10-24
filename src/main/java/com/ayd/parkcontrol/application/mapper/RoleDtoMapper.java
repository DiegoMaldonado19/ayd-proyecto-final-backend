package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.domain.model.user.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleDtoMapper {

    public RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .created_at(role.getCreatedAt())
                .build();
    }
}
