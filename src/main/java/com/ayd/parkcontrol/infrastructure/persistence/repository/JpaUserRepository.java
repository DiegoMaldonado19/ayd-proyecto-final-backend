package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<UserEntity> findByRoleTypeId(Integer roleTypeId, Pageable pageable);

    Page<UserEntity> findByIsActive(Boolean isActive, Pageable pageable);

    @Modifying
    @Query("UPDATE UserEntity u SET u.failedLoginAttempts = :attempts WHERE u.email = :email")
    void updateFailedLoginAttempts(@Param("email") String email, @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE UserEntity u SET u.lockedUntil = :lockedUntil WHERE u.email = :email")
    void lockAccount(@Param("email") String email, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLogin = :lastLogin WHERE u.email = :email")
    void updateLastLogin(@Param("email") String email, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE UserEntity u SET u.has2faEnabled = :enabled WHERE u.email = :email")
    void update2FAStatus(@Param("email") String email, @Param("enabled") Boolean enabled);

    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :passwordHash, u.requiresPasswordChange = false WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("passwordHash") String passwordHash);
}
