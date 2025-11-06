package com.ayd.parkcontrol.security;

import com.ayd.parkcontrol.domain.model.user.Role;
import com.ayd.parkcontrol.domain.repository.RoleRepository;
import com.ayd.parkcontrol.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        com.ayd.parkcontrol.domain.model.user.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Get role name from role repository
        Role role = roleRepository.findById(user.getRoleTypeId())
                .orElseThrow(() -> new UsernameNotFoundException("Role not found for user: " + username));

        String roleName = role.getName();
        log.debug("User {} has role: {}", username, roleName);

        // Return CustomUserDetails instead of Spring's User
        return new CustomUserDetails(user, roleName);
    }
}