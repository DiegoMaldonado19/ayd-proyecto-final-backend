package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.ChangePasswordWith2FARequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.InvalidCredentialsException;
import com.ayd.parkcontrol.domain.exception.PasswordMismatchException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaPasswordResetTokenRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordWith2FAUseCase {

    private final JpaUserRepository userRepository;
    private final JpaPasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<Void> execute(ChangePasswordWith2FARequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate 2FA token
        PasswordResetTokenEntity token = tokenRepository.findValidToken(
                user.getId(),
                request.getCode(),
                LocalDateTime.now())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired verification code"));

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(email, newPasswordHash);

        // Mark token as used
        token.setIsUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Send notification
        emailService.sendPasswordChangedNotification(email, user.getFirstName() + " " + user.getLastName());

        log.info("Password changed successfully with 2FA for user: {}", email);

        return ApiResponse.success("Password changed successfully");
    }
}
