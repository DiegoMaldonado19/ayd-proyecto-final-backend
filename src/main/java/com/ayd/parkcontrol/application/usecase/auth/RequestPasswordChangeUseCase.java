package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPasswordResetTokenRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestPasswordChangeUseCase {

    private static final String DIGITS = "0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_VALIDITY_MINUTES = 15;

    private final JpaUserRepository userRepository;
    private final JpaPasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<Void> execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Validar que no es el primer login (no requiere cambio de contraseña)
        if (user.getRequiresPasswordChange()) {
            throw new BusinessRuleException("Para el primer cambio de contraseña utiliza el endpoint /auth/password/first-change");
        }

        // Invalidar todos los tokens previos para este usuario
        tokenRepository.invalidateAllTokensForUser(user.getId(), LocalDateTime.now());

        // Generar nuevo código de 6 dígitos
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);

        // Guardar token
        PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        tokenRepository.save(tokenEntity);

        // Enviar código 2FA por email usando el template de 2FA
        emailService.send2FACode(user.getEmail(), token);

        log.info("Password change 2FA code sent to user: {}", user.getEmail());

        return ApiResponse.success("Se ha enviado un código de verificación a tu correo electrónico para confirmar el cambio de contraseña");
    }

    private String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return token.toString();
    }
}