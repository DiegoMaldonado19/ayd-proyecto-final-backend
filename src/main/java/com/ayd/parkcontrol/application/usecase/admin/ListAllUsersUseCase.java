package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.UserMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListAllUsersUseCase {

    private final JpaUserRepository jpaUserRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> execute(String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        List<UserEntity> userEntities = jpaUserRepository.findAll(sort);

        // Load all roles and create a cache
        Map<Integer, Role> roleCache = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        log.info("Retrieved all users: {} users found", userEntities.size());

        return userEntities.stream()
                .map(entity -> {
                    var user = userMapper.toDomain(entity);
                    Role role = roleCache.get(user.getRoleTypeId());
                    return userDtoMapper.toResponse(user, role);
                })
                .collect(Collectors.toList());
    }
}
