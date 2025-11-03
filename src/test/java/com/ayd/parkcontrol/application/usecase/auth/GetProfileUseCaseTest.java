package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.auth.UserProfileResponse;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

    @Mock
    private JpaUserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GetProfileUseCase getProfileUseCase;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(1L)
                .email("test@parkcontrol.com")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .roleTypeId(1)
                .has2faEnabled(true)
                .requiresPasswordChange(false)
                .isActive(true)
                .lastLogin(LocalDateTime.of(2025, 11, 1, 10, 30, 0))
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@parkcontrol.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void execute_shouldRetrieveProfileSuccessfully_whenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = getProfileUseCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@parkcontrol.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getPhone()).isEqualTo("1234567890");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        assertThat(response.getHas2faEnabled()).isTrue();
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getLastLogin()).isEqualTo("2025-11-01 10:30:00");
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProfileUseCase.execute())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void execute_shouldMapRoleCorrectly_forBranchOperator() {
        testUser.setRoleTypeId(2);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = getProfileUseCase.execute();

        assertThat(response.getRole()).isEqualTo("BRANCH_OPERATOR");
    }

    @Test
    void execute_shouldMapRoleCorrectly_forBackOffice() {
        testUser.setRoleTypeId(3);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = getProfileUseCase.execute();

        assertThat(response.getRole()).isEqualTo("BACK_OFFICE");
    }

    @Test
    void execute_shouldMapRoleCorrectly_forClient() {
        testUser.setRoleTypeId(4);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = getProfileUseCase.execute();

        assertThat(response.getRole()).isEqualTo("CLIENT");
    }

    @Test
    void execute_shouldHandleNullLastLogin_correctly() {
        testUser.setLastLogin(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = getProfileUseCase.execute();

        assertThat(response.getLastLogin()).isNull();
    }
}
