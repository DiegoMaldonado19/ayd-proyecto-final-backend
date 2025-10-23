package com.ayd.parkcontrol.security.twofa;

import com.ayd.parkcontrol.application.port.cache.CacheService;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private static final String CODE_PREFIX = "2FA:";
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CacheService cacheService;
    private final EmailService emailService;

    public String generateAndSend2FACode(String email) {
        String code = generateCode();
        String cacheKey = CODE_PREFIX + email;

        cacheService.save(cacheKey, code, CODE_EXPIRATION);
        emailService.send2FACode(email, code);

        log.info("2FA code generated and sent to: {}", maskEmail(email));
        return code;
    }

    public boolean verify2FACode(String email, String code) {
        String cacheKey = CODE_PREFIX + email;

        return cacheService.get(cacheKey, String.class)
                .map(storedCode -> {
                    boolean isValid = storedCode.equals(code);
                    if (isValid) {
                        cacheService.delete(cacheKey);
                        log.info("2FA code verified successfully for: {}", maskEmail(email));
                    } else {
                        log.warn("Invalid 2FA code attempt for: {}", maskEmail(email));
                    }
                    return isValid;
                })
                .orElse(false);
    }

    public void invalidate2FACode(String email) {
        String cacheKey = CODE_PREFIX + email;
        cacheService.delete(cacheKey);
        log.info("2FA code invalidated for: {}", maskEmail(email));
    }

    private String generateCode() {
        int code = RANDOM.nextInt(1000000);
        return String.format("%06d", code);
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
