package com.ayd.parkcontrol.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation
 * This class wraps your User entity and provides Spring Security integration
 * 
 * TODO: Update this class when you create your User entity
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    // TODO: Replace these fields with your actual User entity
    private final Integer userId;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;
    private final boolean accountNonLocked;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role to Spring Security format (ROLE_*)
        String springRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return Collections.singletonList(new SimpleGrantedAuthority(springRole));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Helper method to get the role without ROLE_ prefix
     */
    public String getRoleName() {
        return role.replace("ROLE_", "");
    }
}