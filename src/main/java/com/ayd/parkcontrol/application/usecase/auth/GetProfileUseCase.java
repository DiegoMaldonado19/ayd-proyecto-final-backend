package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.response.auth.UserProfileResponse;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetProfileUseCase {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JpaUserRepository userRepository;

    public UserProfileResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.debug("Profile retrieved for user: {}", email);

        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(getRoleName(user.getRoleTypeId()))
                .has2faEnabled(user.getHas2faEnabled())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin() != null ? user.getLastLogin().format(FORMATTER) : null)
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
