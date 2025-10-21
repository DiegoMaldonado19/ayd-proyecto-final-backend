package com.ayd.parkcontrol.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: Inject your UserRepository here when you create it
    // private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // TODO: Replace this with actual database lookup
        // Example:
        // User user = userRepository.findByEmail(username)
        // .orElseThrow(() -> new UsernameNotFoundException("User not found: " +
        // username));

        // For now, returning a temporary implementation to allow the app to start
        // You should replace this with your actual User entity lookup

        if ("admin@parkcontrol.com".equals(username)) {
            return User.builder()
                    .username("admin@parkcontrol.com")
                    .password("$2a$12$dummy") // This is just a placeholder
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}