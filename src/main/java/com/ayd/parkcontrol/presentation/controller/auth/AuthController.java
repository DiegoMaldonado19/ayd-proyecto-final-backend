package com.ayd.parkcontrol.presentation.controller.auth;

import com.ayd.parkcontrol.application.dto.request.auth.*;
import com.ayd.parkcontrol.application.dto.response.auth.*;
import com.ayd.parkcontrol.application.dto.response.common.ApiResponse;
import com.ayd.parkcontrol.application.usecase.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and security endpoints")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final Enable2FAUseCase enable2FAUseCase;
    private final Verify2FACodeUseCase verify2FACodeUseCase;
    private final Disable2FAUseCase disable2FAUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RequestPasswordChangeUseCase requestPasswordChangeUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final ChangePasswordWith2FAUseCase changePasswordWith2FAUseCase;
    private final FirstPasswordChangeUseCase firstPasswordChangeUseCase;
    private final GetProfileUseCase getProfileUseCase;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password. Returns JWT tokens if successful.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Account locked")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout authenticated user and clear security context")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ApiResponse<Void> response = logoutUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "Enable 2FA", description = "Enable two-factor authentication for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA enabled successfully", content = @Content(schema = @Schema(implementation = TwoFAResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFAResponse> enable2FA() {
        TwoFAResponse response = enable2FAUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify 2FA code", description = "Verify two-factor authentication code and complete login")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA verification successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired 2FA code")
    })
    public ResponseEntity<LoginResponse> verify2FA(@Valid @RequestBody Verify2FARequest request) {
        LoginResponse response = verify2FACodeUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/disable")
    @Operation(summary = "Disable 2FA", description = "Disable two-factor authentication for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "2FA disabled successfully", content = @Content(schema = @Schema(implementation = TwoFAResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TwoFAResponse> disable2FA() {
        TwoFAResponse response = disable2FAUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change/request")
    @Operation(summary = "Solicitar cambio de contraseña", description = "Envía código 2FA al correo del usuario autenticado para iniciar proceso de cambio de contraseña")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Código 2FA enviado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Usuario requiere primer cambio de contraseña"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> requestPasswordChange() {
        ApiResponse<Void> response = requestPasswordChangeUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Request password reset", description = "Send password reset instructions to user email")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset instructions sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ApiResponse<Void> response = resetPasswordUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/first-change")
    @Operation(summary = "First password change for new users", description = "Change password for new users without 2FA requirement. Only works when user requires password change.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid password or user does not require password change"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Current password incorrect")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> firstPasswordChange(@Valid @RequestBody FirstPasswordChangeRequest request) {
        ApiResponse<Void> response = firstPasswordChangeUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change")
    @Operation(summary = "Change password with 2FA", description = "Change password using 2FA code received via email after password reset request")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid password or code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired verification code")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePasswordWith2FA(
            @Valid @RequestBody ChangePasswordWith2FARequest request) {
        ApiResponse<Void> response = changePasswordWith2FAUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/change/legacy")
    @Operation(summary = "Change password (legacy)", description = "Change password for authenticated user using current password (deprecated, use /password/change with 2FA)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Current password incorrect")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    @Deprecated
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        ApiResponse<Void> response = changePasswordUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get profile information for authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getProfile() {
        UserProfileResponse response = getProfileUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
