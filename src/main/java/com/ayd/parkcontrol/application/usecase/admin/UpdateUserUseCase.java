package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.request.admin.UpdateUserRequest;
import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.DuplicateEmailException;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    public UserResponse execute(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new DuplicateEmailException(request.getEmail());
            }
        }

        if (request.getRole_type_id() != null && !request.getRole_type_id().equals(user.getRoleTypeId())) {
            roleRepository.findById(request.getRole_type_id())
                    .orElseThrow(() -> new RoleNotFoundException(request.getRole_type_id()));
        }

        userDtoMapper.updateDomain(user, request);
        User updatedUser = userRepository.save(user);

        Role role = roleRepository.findById(updatedUser.getRoleTypeId()).orElse(null);

        return userDtoMapper.toResponse(updatedUser, role);
    }
}
