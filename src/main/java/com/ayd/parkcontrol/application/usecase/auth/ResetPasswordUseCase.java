package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.ResetPasswordRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPasswordResetTokenRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase {

    private static final String DIGITS = "0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_VALIDITY_MINUTES = 15;

    private final JpaUserRepository userRepository;
    private final JpaPasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Transactional
    public ApiResponse<Void> execute(ResetPasswordRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Invalidate all previous tokens for this user
        tokenRepository.invalidateAllTokensForUser(user.getId(), LocalDateTime.now());

        // Generate new 6-digit code
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);

        // Save token
        PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        tokenRepository.save(tokenEntity);

        // Send 2FA code via email
        String resetMessage = String.format(
                "Your password reset verification code is: %s\n\n" +
                        "This code will expire in %d minutes.\n" +
                        "If you didn't request this, please ignore this email.",
                token, TOKEN_VALIDITY_MINUTES);
        emailService.sendPasswordResetEmail(user.getEmail(), resetMessage);

        log.info("Password reset 2FA code sent to user: {}", user.getEmail());

        return ApiResponse.success("A verification code has been sent to your email");
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return token.toString();
    }
}
