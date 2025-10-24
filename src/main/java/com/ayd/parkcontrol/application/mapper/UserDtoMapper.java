package com.ayd.parkcontrol.application.mapper;

import com.ayd.parkcontrol.application.dto.request.admin.CreateUserRequest;
import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public User toDomain(CreateUserRequest request, String passwordHash) {
        return User.builder()
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .firstName(request.getFirst_name())
                .lastName(request.getLast_name())
                .phone(request.getPhone())
                .roleTypeId(request.getRole_type_id())
                .isActive(request.getIs_active() != null ? request.getIs_active() : true)
                .requiresPasswordChange(true)
                .has2faEnabled(false)
                .failedLoginAttempts(0)
                .build();
    }

    public void updateDomain(User user, UpdateUserRequest request) {
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirst_name() != null) {
            user.setFirstName(request.getFirst_name());
        }
        if (request.getLast_name() != null) {
            user.setLastName(request.getLast_name());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole_type_id() != null) {
            user.setRoleTypeId(request.getRole_type_id());
        }
    }

    public UserResponse toResponse(User user, Role role) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .first_name(user.getFirstName())
                .last_name(user.getLastName())
                .phone(user.getPhone())
                .role_type_id(user.getRoleTypeId())
                .role_name(role != null ? role.getName() : null)
                .is_active(user.getIsActive())
                .requires_password_change(user.getRequiresPasswordChange())
                .has_2fa_enabled(user.getHas2faEnabled())
                .failed_login_attempts(user.getFailedLoginAttempts())
                .locked_until(user.getLockedUntil())
                .last_login(user.getLastLogin())
                .created_at(user.getCreatedAt())
                .updated_at(user.getUpdatedAt())
                .build();
    }
}
