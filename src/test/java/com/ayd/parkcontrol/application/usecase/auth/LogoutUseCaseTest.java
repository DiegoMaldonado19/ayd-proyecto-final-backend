package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldLogoutSuccessfully_whenUserIsAuthenticated() {
        when(authentication.getName()).thenReturn("test@parkcontrol.com");

        ApiResponse<Void> response = logoutUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Logged out successfully");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void execute_shouldLogoutSuccessfully_whenUserIsNotAuthenticated() {
        when(authentication.getName()).thenReturn(null);

        ApiResponse<Void> response = logoutUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Logged out successfully");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void execute_shouldClearSecurityContext_whenLogout() {
        when(authentication.getName()).thenReturn("test@parkcontrol.com");

        logoutUseCase.execute();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
