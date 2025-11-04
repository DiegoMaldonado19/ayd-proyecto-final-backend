package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting the currently authenticated user's information.
 * Returns the profile of the user making the request.
 */
@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDtoMapper userDtoMapper;

    /**
     * Retrieves the currently authenticated user's information.
     *
     * @return UserResponse with the authenticated user's data
     * @throws UserNotFoundException if the authenticated user is not found in the
     *                               database
     * @throws RoleNotFoundException if the user's role is not found
     */
    @Transactional(readOnly = true)
    public UserResponse execute() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado"));

        Role role = roleRepository.findById(user.getRoleTypeId())
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado"));

        return userDtoMapper.toResponse(user, role);
    }
}
