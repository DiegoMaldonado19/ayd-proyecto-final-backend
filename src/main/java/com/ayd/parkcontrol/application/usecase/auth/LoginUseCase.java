package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.LoginRequest;
import com.ayd.parkcontrol.application.dto.response.auth.LoginResponse;
import com.ayd.parkcontrol.domain.exception.AccountLockedException;
import com.ayd.parkcontrol.domain.exception.InvalidCredentialsException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.ayd.parkcontrol.security.twofa.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUseCase {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TwoFactorAuthService twoFactorAuthService;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Transactional
    public LoginResponse execute(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        validateAccountStatus(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getHas2faEnabled()) {
            return handle2FALogin(user);
        }

        return completeLogin(user);
    }

    private void validateAccountStatus(UserEntity user) {
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is inactive");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException("Account is locked until: " + user.getLockedUntil());
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            userRepository.lockAccount(user.getEmail(), null);
            userRepository.updateFailedLoginAttempts(user.getEmail(), 0);
        }
    }

    private void handleFailedLogin(UserEntity user) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        userRepository.updateFailedLoginAttempts(user.getEmail(), failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            userRepository.lockAccount(user.getEmail(), lockUntil);
            log.warn("Account locked due to failed login attempts: {}", user.getEmail());
        }
    }

    private LoginResponse handle2FALogin(UserEntity user) {
        twoFactorAuthService.generateAndSend2FACode(user.getEmail());
        log.info("2FA code sent to user: {}", user.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(getRoleName(user.getRoleTypeId()))
                .has2faEnabled(true)
                .requires2faVerification(true)
                .requiresPasswordChange(user.getRequiresPasswordChange())
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public LoginResponse completeLogin(UserEntity user) {
        userRepository.updateFailedLoginAttempts(user.getEmail(), 0);
        userRepository.updateLastLogin(user.getEmail(), LocalDateTime.now());

        String accessToken = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId().intValue(),
                getRoleName(user.getRoleTypeId()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(getRoleName(user.getRoleTypeId()))
                .has2faEnabled(user.getHas2faEnabled())
                .requires2faVerification(false)
                .requiresPasswordChange(user.getRequiresPasswordChange())
                .build();
    }

    private String getRoleName(Integer roleTypeId) {
        return switch (roleTypeId) {
            case 1 -> "ADMIN";
            case 2 -> "BRANCH_OPERATOR";
            case 3 -> "BACK_OFFICE";
            case 4 -> "CLIENT";
            case 5 -> "COMPANY";
            case 6 -> "COMMERCE";
            default -> "UNKNOWN";
        };
    }
}
