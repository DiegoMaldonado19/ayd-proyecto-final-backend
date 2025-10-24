package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    Page<User> findByRoleTypeId(Integer roleTypeId, Pageable pageable);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    void deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
