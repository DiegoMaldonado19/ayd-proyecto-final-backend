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
                        .requestMatchers("/auth/2fa/verify").permitAll()
                        .requestMatchers("/auth/refresh").permitAll()
                        .requestMatchers("/auth/password/reset").permitAll()

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("Administrador")

                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/auth/password/change").authenticated()
                        .requestMatchers("/auth/2fa/enable").authenticated()
                        .requestMatchers("/auth/2fa/disable").authenticated()
                        .requestMatchers("/auth/profile").authenticated()

                        .requestMatchers("/rates/**").hasRole("Administrador")
                        .requestMatchers("/branches/**").hasRole("Administrador")
                        .requestMatchers("/subscription-plans/**").hasRole("Administrador")
                        .requestMatchers("/dashboard/**").hasAnyRole("Administrador", "Operador Sucursal")

                        .requestMatchers("/subscriptions/**").authenticated()
                        .requestMatchers("/tickets/**").hasAnyRole("Administrador", "Operador Sucursal")
                        .requestMatchers("/occupancy/**").hasAnyRole("Administrador", "Operador Sucursal")

                        .requestMatchers("/commerce/**").hasAnyRole("Administrador", "Operador Sucursal")
                        .requestMatchers("/settlements/**").hasRole("Administrador")

                        .requestMatchers("/plate-changes/**").hasRole("Operador Back Office")
                        .requestMatchers("/temporal-permits/**").hasRole("Operador Back Office")

                        .requestMatchers("/incidents/**")
                        .hasAnyRole("Administrador", "Operador Sucursal", "Operador Back Office")
                        .requestMatchers("/fleets/**").hasAnyRole("Administrador", "Administrador Flotilla")

                        .requestMatchers("/reports/**").hasAnyRole("Administrador", "Operador Sucursal")

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