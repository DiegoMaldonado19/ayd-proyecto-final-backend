package com.ayd.parkcontrol.application.usecase.auth;

import com.ayd.parkcontrol.application.dto.request.auth.FirstPasswordChangeRequest;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.domain.exception.InvalidCredentialsException;
import com.ayd.parkcontrol.domain.exception.PasswordMismatchException;
import com.ayd.parkcontrol.domain.exception.UserNotFoundException;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import com.ayd.parkcontrol.infrastructure.persistence.entity.UserEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirstPasswordChangeUseCase {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public ApiResponse<Void> execute(FirstPasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validar que el usuario requiere cambio de contraseña (es un usuario nuevo)
        if (!user.getRequiresPasswordChange()) {
            throw new BusinessRuleException("El usuario no requiere cambio de contraseña. Utiliza el endpoint regular de cambio de contraseña.");
        }

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("La contraseña actual es incorrecta");
        }

        // Validar que las contraseñas nuevas coinciden
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("La nueva contraseña y la confirmación no coinciden");
        }

        // Validar que la nueva contraseña es diferente a la actual
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new PasswordMismatchException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        // Actualizar contraseña y marcar que ya no requiere cambio
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(email, newPasswordHash);

        // Enviar notificación por email
        emailService.sendPasswordChangedNotification(email, user.getFirstName() + " " + user.getLastName());

        log.info("First password changed successfully for new user: {}", email);

        return ApiResponse.success("Contraseña cambiada exitosamente. Ahora puedes usar todas las funciones del sistema.");
    }
}