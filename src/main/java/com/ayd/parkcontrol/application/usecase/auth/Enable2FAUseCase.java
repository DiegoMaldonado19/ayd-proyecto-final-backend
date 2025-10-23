package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.auth.TwoFAResponse;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.twofa.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class Enable2FAUseCase {

    private final JpaUserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;

    @Transactional
    public TwoFAResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getHas2faEnabled()) {
            return TwoFAResponse.builder()
                    .message("2FA is already enabled")
                    .isEnabled(true)
                    .email(maskEmail(email))
                    .build();
        }

        userRepository.update2FAStatus(email, true);
        twoFactorAuthService.generateAndSend2FACode(email);

        log.info("2FA enabled for user: {}", email);

        return TwoFAResponse.builder()
                .message("2FA enabled successfully. Verification code sent to your email.")
                .isEnabled(true)
                .email(maskEmail(email))
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "*".repeat(localPart.length()) + "@" + domain;
        }

        return localPart.charAt(0) + "***" + "@" + domain;
    }
}
