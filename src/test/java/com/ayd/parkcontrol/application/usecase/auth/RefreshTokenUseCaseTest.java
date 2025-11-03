package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.RefreshTokenRequest;
import com.ayd.parkcontrol.application.dto.response.auth.TokenResponse;
import com.ayd.parkcontrol.domain.exception.TokenExpiredException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import com.ayd.parkcontrol.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JpaUserRepository userRepository;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    private UserEntity testUser;
    private RefreshTokenRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenUseCase, "jwtExpirationMs", 3600000L);

        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .roleTypeId(1)
                .isActive(true)
                .build();

        request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();
    }

    @Test
    void execute_shouldRefreshTokensSuccessfully_whenRefreshTokenIsValid() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("test@parkcontrol.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), anyString())).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh-token");

        TokenResponse response = refreshTokenUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600000L);
    }

    @Test
    void execute_shouldThrowTokenExpiredException_whenRefreshTokenIsInvalid() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenUseCase.execute(request))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("Invalid or expired refresh token");
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("test@parkcontrol.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void execute_shouldThrowTokenExpiredException_whenUserIsInactive() {
        testUser.setIsActive(false);
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("test@parkcontrol.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(request))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("User account is inactive");
    }

    @Test
    void execute_shouldGenerateTokensWithCorrectRole_forDifferentRoleTypes() {
        testUser.setRoleTypeId(4); // CLIENT role
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("test@parkcontrol.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(anyString(), anyInt(), eq("CLIENT"))).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");

        TokenResponse response = refreshTokenUseCase.execute(request);

        assertThat(response).isNotNull();
    }
}
