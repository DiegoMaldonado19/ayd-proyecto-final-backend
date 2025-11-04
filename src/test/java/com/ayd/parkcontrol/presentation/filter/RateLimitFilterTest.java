package com.ayd.parkcontrol.presentation.filter;

import com.ayd.parkcontrol.infrastructure.ratelimit.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doFilterInternal_WithNonRateLimitedEndpoint_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/tickets");
        when(request.getMethod()).thenReturn("GET");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitService, never()).isRateLimitExceeded(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void doFilterInternal_WithLoginEndpointUnderLimit_ShouldContinueChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(rateLimitService.isRateLimitExceeded("/api/v1/auth/login", "192.168.1.1", 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded("/api/v1/auth/login", "192.168.1.1", 50, 900);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_WithLoginEndpointExceedingLimit_ShouldReturn429() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String clientIp = "192.168.1.1";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(rateLimitService.isRateLimitExceeded(endpoint, clientIp, 50, 900))
                .thenReturn(true);
        when(rateLimitService.getRemainingLockTime(endpoint, clientIp))
                .thenReturn(600L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Too Many Requests\"}");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setHeader("Retry-After", "600");
        verify(filterChain, never()).doFilter(request, response);
        verify(rateLimitService).getRemainingLockTime(endpoint, clientIp);
    }

    @Test
    void doFilterInternal_WithVerify2FAEndpointExceedingLimit_ShouldReturn429() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/verify-2fa";
        String clientIp = "10.0.0.5";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(rateLimitService.isRateLimitExceeded(endpoint, clientIp, 30, 300))
                .thenReturn(true);
        when(rateLimitService.getRemainingLockTime(endpoint, clientIp))
                .thenReturn(180L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Too Many Requests\"}");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("Retry-After", "180");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithForgotPasswordEndpointExceedingLimit_ShouldReturn429() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/forgot-password";
        String clientIp = "172.16.0.10";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(rateLimitService.isRateLimitExceeded(endpoint, clientIp, 20, 3600))
                .thenReturn(true);
        when(rateLimitService.getRemainingLockTime(endpoint, clientIp))
                .thenReturn(3000L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Too Many Requests\"}");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("Retry-After", "3000");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithXForwardedForHeader_ShouldUseProxiedIp() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String proxiedIp = "203.0.113.5";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(proxiedIp);
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(rateLimitService.isRateLimitExceeded(endpoint, proxiedIp, 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, proxiedIp, 50, 900);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMultipleXForwardedForIps_ShouldUseFirstIp() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String firstIp = "203.0.113.5";
        String xForwardedFor = firstIp + ", 10.0.0.1, 192.168.1.1";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(rateLimitService.isRateLimitExceeded(endpoint, firstIp, 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, firstIp, 50, 900);
    }

    @Test
    void doFilterInternal_WithXRealIpHeader_ShouldUseRealIp() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String realIp = "198.51.100.7";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(realIp);
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(rateLimitService.isRateLimitExceeded(endpoint, realIp, 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, realIp, 50, 900);
    }

    @Test
    void doFilterInternal_WithProxyClientIpHeader_ShouldUseProxyClientIp() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String proxyClientIp = "198.51.100.8";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(proxyClientIp);
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(rateLimitService.isRateLimitExceeded(endpoint, proxyClientIp, 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, proxyClientIp, 50, 900);
    }

    @Test
    void doFilterInternal_WithUnknownXForwardedFor_ShouldFallbackToRemoteAddr() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String remoteAddr = "192.168.1.100";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);
        when(rateLimitService.isRateLimitExceeded(endpoint, remoteAddr, 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, remoteAddr, 50, 900);
    }

    @Test
    void doFilterInternal_WithGETMethodOnLoginEndpoint_ShouldNotApplyRateLimit() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getMethod()).thenReturn("GET");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService, never()).isRateLimitExceeded(anyString(), anyString(), anyInt(), anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNullRemoteAddr_ShouldUseUnknown() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);
        when(rateLimitService.isRateLimitExceeded(endpoint, "unknown", 50, 900))
                .thenReturn(false);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitService).isRateLimitExceeded(endpoint, "unknown", 50, 900);
    }

    @Test
    void doFilterInternal_WithRateLimitExceeded_ShouldIncludeRetryAfterInResponse() throws ServletException, IOException {
        // Arrange
        String endpoint = "/api/v1/auth/login";
        String clientIp = "192.168.1.1";
        long retryAfter = 450L;
        
        when(request.getRequestURI()).thenReturn(endpoint);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(rateLimitService.isRateLimitExceeded(endpoint, clientIp, 50, 900))
                .thenReturn(true);
        when(rateLimitService.getRemainingLockTime(endpoint, clientIp))
                .thenReturn(retryAfter);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"retryAfterSeconds\":" + retryAfter + "}");

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setHeader("Retry-After", String.valueOf(retryAfter));
        verify(objectMapper).writeValueAsString(argThat(map -> 
            map instanceof java.util.Map && 
            ((java.util.Map<?, ?>)map).get("retryAfterSeconds").equals(retryAfter)
        ));
    }
}
