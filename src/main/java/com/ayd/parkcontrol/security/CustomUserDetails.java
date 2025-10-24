package com.ayd.parkcontrol.security;

import com.ayd.parkcontrol.domain.model.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation
 * This class wraps the User entity and provides Spring Security integration
 */
@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final String roleName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert role to Spring Security format (ROLE_*)
        String springRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return Collections.singletonList(new SimpleGrantedAuthority(springRole));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getIsActive() && user.getLockedUntil() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.getRequiresPasswordChange();
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }

    /**
     * Helper method to get the user ID
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Helper method to get the role without ROLE_ prefix
     */
    public String getRoleNameWithoutPrefix() {
        return roleName.replace("ROLE_", "");
    }
}