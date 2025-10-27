package com.ayd.parkcontrol.presentation.filter;

import com.ayd.parkcontrol.infrastructure.ratelimit.RateLimitConfig;
import com.ayd.parkcontrol.infrastructure.ratelimit.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro HTTP para aplicar rate limiting a endpoints críticos.
 * 
 * Se ejecuta con prioridad HIGHEST para interceptar requests antes de
 * que lleguen a los controladores.
 * 
 * Endpoints protegidos:
 * - POST /api/v1/auth/login: 5 intentos / 15 min
 * - POST /api/v1/auth/verify-2fa: 3 intentos / 5 min
 * - POST /api/v1/auth/forgot-password: 3 intentos / 1 hora
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    /**
     * Configuración de límites de tasa por endpoint.
     * 
     * Reglas de Negocio:
     * - Login: 5 intentos cada 15 minutos (900 segundos)
     * - Verificación 2FA: 3 intentos cada 5 minutos (300 segundos)
     * - Recuperación de contraseña: 3 intentos cada hora (3600 segundos)
     */
    private static final Map<String, RateLimitConfig> RATE_LIMITS = Map.of(
            "/api/v1/auth/login", new RateLimitConfig(5, 900),
            "/api/v1/auth/verify-2fa", new RateLimitConfig(3, 300),
            "/api/v1/auth/forgot-password", new RateLimitConfig(3, 3600));

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Solo aplicar a endpoints POST configurados
        if ("POST".equals(method) && RATE_LIMITS.containsKey(uri)) {
            RateLimitConfig config = RATE_LIMITS.get(uri);
            String clientIp = getClientIp(request);

            boolean exceeded = rateLimitService.isRateLimitExceeded(
                    uri,
                    clientIp,
                    config.getMaxAttempts(),
                    config.getWindowSeconds());

            if (exceeded) {
                long remainingTime = rateLimitService.getRemainingLockTime(uri, clientIp);

                log.warn("Rate limit excedido para IP={}, endpoint={}, reintente en {} segundos",
                        clientIp, uri, remainingTime);

                sendRateLimitResponse(response, remainingTime);
                return; // No continuar con el filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Envía una respuesta HTTP 429 (Too Many Requests) al cliente.
     * 
     * @param response          respuesta HTTP
     * @param retryAfterSeconds segundos hasta que pueda reintentar
     * @throws IOException si hay error al escribir la respuesta
     */
    private void sendRateLimitResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Too Many Requests");
        errorBody.put("message", "Demasiados intentos. Por favor, intente nuevamente más tarde.");
        errorBody.put("retryAfterSeconds", retryAfterSeconds);
        errorBody.put("status", HttpStatus.TOO_MANY_REQUESTS.value());

        String jsonResponse = objectMapper.writeValueAsString(errorBody);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Obtiene la dirección IP real del cliente.
     * 
     * Considera headers de proxies/load balancers:
     * - X-Forwarded-For
     * - X-Real-IP
     * - Proxy-Client-IP
     * 
     * @param request request HTTP
     * @return dirección IP del cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Si X-Forwarded-For contiene múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}
