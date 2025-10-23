package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.Verify2FARequest;
import com.ayd.parkcontrol.application.dto.response.auth.LoginResponse;
import com.ayd.parkcontrol.domain.exception.InvalidTwoFactorCodeException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import com.ayd.parkcontrol.security.twofa.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class Verify2FACodeUseCase {

    private final JpaUserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Transactional
    public LoginResponse execute(Verify2FARequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isValid = twoFactorAuthService.verify2FACode(request.getEmail(), request.getCode());

        if (!isValid) {
            throw new InvalidTwoFactorCodeException("Invalid or expired 2FA code");
        }

        userRepository.updateFailedLoginAttempts(user.getEmail(), 0);
        userRepository.updateLastLogin(user.getEmail(), LocalDateTime.now());

        String accessToken = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId().intValue(),
                getRoleName(user.getRoleTypeId()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        log.info("User completed 2FA login successfully: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(getRoleName(user.getRoleTypeId()))
                .has2faEnabled(true)
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
