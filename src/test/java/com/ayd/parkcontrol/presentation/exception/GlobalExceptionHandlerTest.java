package com.ayd.parkcontrol.presentation.exception;

import com.ayd.parkcontrol.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleUserNotFoundException_shouldReturn404() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("User not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleInvalidCredentialsException_shouldReturn401() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentialsException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleAccountLockedException_shouldReturn403() {
        // Arrange
        AccountLockedException exception = new AccountLockedException("Account is locked");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountLockedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    void handleInvalidTwoFactorCodeException_shouldReturn401() {
        // Arrange
        InvalidTwoFactorCodeException exception = new InvalidTwoFactorCodeException("Invalid 2FA code");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTwoFactorCodeException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleTokenExpiredException_shouldReturn401() {
        // Arrange
        TokenExpiredException exception = new TokenExpiredException("Token expired");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenExpiredException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Token expired", response.getBody().getMessage());
    }

    @Test
    void handlePasswordMismatchException_shouldReturn400() {
        // Arrange
        PasswordMismatchException exception = new PasswordMismatchException("Passwords do not match");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePasswordMismatchException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void handleDuplicateEmailException_shouldReturn409() {
        // Arrange
        DuplicateEmailException exception = new DuplicateEmailException("Email already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEmailException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void handleRoleNotFoundException_shouldReturn404() {
        // Arrange
        RoleNotFoundException exception = new RoleNotFoundException("Role not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRoleNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleBranchNotFoundException_shouldReturn404() {
        // Arrange
        BranchNotFoundException exception = new BranchNotFoundException("Branch not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBranchNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleDuplicateBranchNameException_shouldReturn409() {
        // Arrange
        DuplicateBranchNameException exception = new DuplicateBranchNameException("Branch name already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateBranchNameException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleSubscriptionPlanNotFoundException_shouldReturn404() {
        // Arrange
        SubscriptionPlanNotFoundException exception = new SubscriptionPlanNotFoundException("Plan not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSubscriptionPlanNotFoundException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleSubscriptionNotFoundException_shouldReturn404() {
        // Arrange
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException("Subscription not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSubscriptionNotFoundException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleDuplicateLicensePlateException_shouldReturn409() {
        // Arrange
        DuplicateLicensePlateException exception = new DuplicateLicensePlateException("License plate already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateLicensePlateException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleDuplicatePlanTypeException_shouldReturn409() {
        // Arrange
        DuplicatePlanTypeException exception = new DuplicatePlanTypeException("Plan type already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicatePlanTypeException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleInvalidDiscountHierarchyException_shouldReturn422() {
        // Arrange
        InvalidDiscountHierarchyException exception = new InvalidDiscountHierarchyException(
                "Invalid discount hierarchy");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidDiscountHierarchyException(exception,
                webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Unprocessable Entity", response.getBody().getError());
    }

    @Test
    void handleBusinessRuleException_shouldReturn422() {
        // Arrange
        BusinessRuleException exception = new BusinessRuleException("Business rule violated");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessRuleException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Unprocessable Entity", response.getBody().getError());
    }

    @Test
    void handleAuthenticationException_shouldReturn401() {
        // Arrange
        AuthenticationException exception = mock(AuthenticationException.class);
        when(exception.getMessage()).thenReturn("Authentication failed");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Authentication failed", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400WithValidationErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("user", "email", "Email is required");
        FieldError fieldError2 = new FieldError("user", "name", "Name is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertEquals("Email is required", response.getBody().getValidationErrors().get("email"));
        assertEquals("Name is required", response.getBody().getValidationErrors().get("name"));
    }

    @Test
    void handleGlobalException_shouldReturn500() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
