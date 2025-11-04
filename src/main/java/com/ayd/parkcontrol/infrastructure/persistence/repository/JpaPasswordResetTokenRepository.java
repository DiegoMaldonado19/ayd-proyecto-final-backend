package com.ayd.parkcontrol.infrastructure.persistence.repository;

import com.ayd.parkcontrol.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Find valid (not used and not expired) token for user
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.userId = :userId AND t.token = :token AND t.isUsed = false AND t.expiresAt > :now")
    Optional<PasswordResetTokenEntity> findValidToken(@Param("userId") Long userId, @Param("token") String token,
            @Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for a user
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.isUsed = true, t.usedAt = :now WHERE t.userId = :userId AND t.isUsed = false")
    void invalidateAllTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
