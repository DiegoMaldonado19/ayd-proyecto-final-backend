package com.ayd.parkcontrol.config;

import com.ayd.parkcontrol.security.jwt.JwtAuthenticationEntryPoint;
import com.ayd.parkcontrol.presentation.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/verify-2fa").permitAll()
                        .requestMatchers("/auth/refresh-token").permitAll()
                        .requestMatchers("/auth/forgot-password").permitAll()
                        .requestMatchers("/auth/reset-password").permitAll()
                        .requestMatchers("/auth/validate-reset-token").permitAll()
                        .requestMatchers("/auth/resend-2fa-code").permitAll()
                        .requestMatchers("/auth/validate-token").permitAll()

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMINISTRATOR")

                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/auth/change-password").authenticated()
                        .requestMatchers("/auth/enable-2fa").authenticated()
                        .requestMatchers("/auth/request-disable-2fa").authenticated()
                        .requestMatchers("/auth/confirm-disable-2fa").authenticated()
                        .requestMatchers("/auth/me").authenticated()
                        .requestMatchers("/auth/sessions").authenticated()
                        .requestMatchers("/auth/revoke-token").authenticated()

                        .requestMatchers("/rates/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/branches/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/subscription-plans/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR")

                        .requestMatchers("/subscriptions/**").authenticated()
                        .requestMatchers("/tickets/**").hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR")
                        .requestMatchers("/occupancy/**").hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR")

                        .requestMatchers("/commerce/**").hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR", "COMMERCE")
                        .requestMatchers("/settlements/**").hasAnyRole("ADMINISTRATOR", "COMMERCE")

                        .requestMatchers("/plate-changes/**").hasRole("BACKOFFICE_OPERATOR")
                        .requestMatchers("/temporal-permits/**").hasRole("BACKOFFICE_OPERATOR")

                        .requestMatchers("/incidents/**")
                        .hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR", "BACKOFFICE_OPERATOR")
                        .requestMatchers("/fleets/**").hasAnyRole("ADMINISTRATOR", "COMPANY")

                        .requestMatchers("/reports/**").hasAnyRole("ADMINISTRATOR", "BRANCH_OPERATOR")

                        .anyRequest().authenticated())

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}