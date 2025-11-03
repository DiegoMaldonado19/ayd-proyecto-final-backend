package com.ayd.parkcontrol.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para JwtTokenProvider.
 * Valida generación, validación y extracción de claims de tokens JWT.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpirationMs;
    private long jwtRefreshExpirationMs;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "MySuperSecretKeyForJWTTokensThatIsAtLeast32CharactersLong123456";
        jwtExpirationMs = 3600000L; // 1 hora
        jwtRefreshExpirationMs = 86400000L; // 24 horas

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", jwtRefreshExpirationMs);
    }

    @Test
    void generateToken_withAuthentication_shouldCreateValidToken() {
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_withUsername_shouldCreateValidToken() {
        String username = "user@example.com";

        String token = jwtTokenProvider.generateToken(username);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
    }

    @Test
    void generateToken_withClaims_shouldIncludeUserIdAndRole() {
        String username = "admin@example.com";
        Integer userId = 123;
        String role = "ADMIN";

        String token = jwtTokenProvider.generateToken(username, userId, role);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
    }

    @Test
    void generateRefreshToken_shouldCreateValidToken() {
        String username = "user@example.com";

        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo(username);
    }

    @Test
    void getUsernameFromToken_shouldExtractCorrectUsername() {
        String username = "test@example.com";
        String token = jwtTokenProvider.generateToken(username);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void getUserIdFromToken_shouldExtractCorrectUserId() {
        String username = "user@example.com";
        Integer userId = 456;
        String role = "USER";
        String token = jwtTokenProvider.generateToken(username, userId, role);

        Integer extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void getRoleFromToken_shouldExtractCorrectRole() {
        String username = "admin@example.com";
        Integer userId = 1;
        String role = "ADMINISTRATOR";
        String token = jwtTokenProvider.generateToken(username, userId, role);

        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void getExpirationDateFromToken_shouldReturnFutureDate() {
        String username = "user@example.com";
        String token = jwtTokenProvider.generateToken(username);

        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    void extractClaim_shouldExtractCustomClaim() {
        String username = "user@example.com";
        Integer userId = 789;
        String role = "OPERATOR";
        String token = jwtTokenProvider.generateToken(username, userId, role);

        Integer extractedUserId = jwtTokenProvider.extractClaim(token, claims -> claims.get("userId", Integer.class));

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String username = "valid@example.com";
        String token = jwtTokenProvider.generateToken(username);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_withMalformedToken_shouldReturnFalse() {
        String malformedToken = "this.is.not.a.valid.jwt";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withInvalidSignature_shouldReturnFalse() {
        String username = "user@example.com";
        String token = jwtTokenProvider.generateToken(username);
        // Modificar el token para invalidar la firma
        String invalidToken = token.substring(0, token.length() - 5) + "XXXXX";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void getSigningKey_withShortSecret_shouldThrowException() {
        JwtTokenProvider providerWithShortSecret = new JwtTokenProvider();
        ReflectionTestUtils.setField(providerWithShortSecret, "jwtSecret", "short");

        assertThatThrownBy(() -> providerWithShortSecret.generateToken("test@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret must be at least 32 characters long");
    }

    @Test
    void refreshToken_shouldHaveLongerExpiration() {
        String username = "user@example.com";
        String accessToken = jwtTokenProvider.generateToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        Date accessExpiration = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        Date refreshExpiration = jwtTokenProvider.getExpirationDateFromToken(refreshToken);

        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    @Test
    void tokenGeneration_shouldIncludeIssuedAtDate() {
        String username = "user@example.com";
        String token = jwtTokenProvider.generateToken(username);

        Date issuedAt = jwtTokenProvider.extractClaim(token, Claims::getIssuedAt);

        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt).isBeforeOrEqualTo(new Date());
    }
}
