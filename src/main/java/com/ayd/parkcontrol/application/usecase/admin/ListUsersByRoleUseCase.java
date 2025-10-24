package com.ayd.parkcontrol.application.usecase.admin;

import com.ayd.parkcontrol.application.dto.response.admin.UserResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.mapper.UserDtoMapper;
import com.ayd.parkcontrol.domain.exception.RoleNotFoundException;
import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.model.user.User;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListUsersByRoleUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(Integer roleId, Integer page, Integer size, String sortBy,
            String sortDirection) {
        // Validar que el rol existe
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado con ID: " + roleId));

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> usersPage = userRepository.findByRoleTypeId(roleId, pageable);

        List<UserResponse> userResponses = usersPage.getContent().stream()
                .map(user -> userDtoMapper.toResponse(user, role))
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page_number(usersPage.getNumber())
                .page_size(usersPage.getSize())
                .total_elements(usersPage.getTotalElements())
                .total_pages(usersPage.getTotalPages())
                .is_first(usersPage.isFirst())
                .is_last(usersPage.isLast())
                .has_previous(usersPage.hasPrevious())
                .has_next(usersPage.hasNext())
                .build();
    }
}
