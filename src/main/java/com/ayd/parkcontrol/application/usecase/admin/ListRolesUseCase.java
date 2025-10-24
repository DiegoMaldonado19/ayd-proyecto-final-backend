package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.RoleResponse;
import com.ayd.parkcontrol.application.mapper.RoleDtoMapper;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListRolesUseCase {

    private final RoleRepository roleRepository;
    private final RoleDtoMapper roleDtoMapper;

    @Transactional(readOnly = true)
    public List<RoleResponse> execute() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(roleDtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
