package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.user.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findById(Integer id);

    Optional<Role> findByName(String name);

    List<Role> findAll();
}
