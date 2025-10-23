package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.RefreshTokenRequest;
import com.ayd.parkcontrol.application.dto.response.auth.TokenResponse;
import com.ayd.parkcontrol.domain.exception.TokenExpiredException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final JpaUserRepository userRepository;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    public TokenResponse execute(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenExpiredException("Invalid or expired refresh token");
        }

        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new TokenExpiredException("User account is inactive");
        }

        String newAccessToken = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId().intValue(),
                getRoleName(user.getRoleTypeId()));
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        log.info("Tokens refreshed for user: {}", email);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs)
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
