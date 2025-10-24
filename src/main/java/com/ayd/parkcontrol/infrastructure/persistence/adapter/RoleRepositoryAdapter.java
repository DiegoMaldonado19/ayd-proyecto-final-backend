package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.RoleMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;
    private final RoleMapper roleMapper;

    @Override
    public Optional<Role> findById(Integer id) {
        return jpaRoleRepository.findById(id)
                .map(roleMapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRoleRepository.findByName(name)
                .map(roleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAll()
                .stream()
                .map(roleMapper::toDomain)
                .collect(Collectors.toList());
    }
}
