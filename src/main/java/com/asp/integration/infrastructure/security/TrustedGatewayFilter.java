package com.asp.integration.infrastructure.security;

import com.asp.integration.infrastructure.config.properties.IngressSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Valida que las peticiones de negocio entren al Bridge únicamente a través de Kong.
 *
 * El filtro exige:
 *   1. Un secreto compartido Kong -> Bridge
 *   2. Un client id interno autenticado
 *   3. Identidad del usuario final para endpoints de negocio
 *
 * Si cualquiera de estos elementos falta o es inválido, la solicitud se rechaza
 * antes de tocar el controlador.
 *
 * @autor: HJMB
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class TrustedGatewayFilter extends OncePerRequestFilter {

    public static final String REQUEST_CONTEXT_ATTRIBUTE = GatewayRequestContext.class.getName();

    private static final String AUTHENTICATED_CLIENT_MDC_KEY = "authenticatedClient";
    private static final String AUTHENTICATED_USER_MDC_KEY = "authenticatedUser";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String KONG_CONSUMER_USERNAME_HEADER = "X-Consumer-Username";
    private static final String KONG_CONSUMER_CUSTOM_ID_HEADER = "X-Consumer-Custom-ID";

    private final IngressSecurityProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }

        String path = request.getRequestURI();
        return properties.getPublicPathPrefixes().stream()
                .anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String gatewaySecret = request.getHeader(properties.getGatewaySecretHeader());
        if (!isExpectedSecret(gatewaySecret)) {
            reject(response, HttpStatus.UNAUTHORIZED,
                    "Solicitud rechazada: el Bridge solo acepta tráfico autenticado desde Kong");
            return;
        }

        JwtClaimsContext jwtClaimsContext = extractJwtClaimsContext(request.getHeader(AUTHORIZATION_HEADER));
        String authenticatedClient = firstNonBlank(
                request.getHeader(properties.getAuthenticatedClientHeader()),
                request.getHeader(KONG_CONSUMER_CUSTOM_ID_HEADER),
                request.getHeader(KONG_CONSUMER_USERNAME_HEADER),
                jwtClaimsContext.authenticatedClient()
        );
        if (!StringUtils.hasText(authenticatedClient)) {
            reject(response, HttpStatus.UNAUTHORIZED,
                    "Solicitud rechazada: falta el cliente autenticado validado por Kong");
            return;
        }

        boolean allowedClient = properties.getAllowedClientIds().isEmpty()
                || properties.getAllowedClientIds().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(authenticatedClient));
        if (!allowedClient) {
            reject(response, HttpStatus.FORBIDDEN,
                    "Solicitud rechazada: el cliente autenticado no está autorizado para este Bridge");
            return;
        }

        String systemOrigin = request.getHeader(properties.getSystemOriginHeader());
        if (!StringUtils.hasText(systemOrigin)) {
            reject(response, HttpStatus.UNAUTHORIZED,
                    "Solicitud rechazada: falta el sistema origen inyectado por Kong");
            return;
        }

        String authenticatedUser = firstNonBlank(
                request.getHeader(properties.getAuthenticatedUserHeader()),
                jwtClaimsContext.authenticatedUser()
        );
        if (properties.isRequireAuthenticatedUserForOperations() && !StringUtils.hasText(authenticatedUser)) {
            reject(response, HttpStatus.UNAUTHORIZED,
                    "Solicitud rechazada: falta la identidad del usuario final autenticado en el JWT validado por Kong");
            return;
        }

        String authenticatedScopes = firstNonBlank(
                request.getHeader(properties.getAuthenticatedScopesHeader()),
                jwtClaimsContext.authenticatedScopes()
        );
        GatewayRequestContext gatewayContext = new GatewayRequestContext(
                authenticatedClient,
                authenticatedUser,
                authenticatedScopes,
                systemOrigin
        );

        request.setAttribute(REQUEST_CONTEXT_ATTRIBUTE, gatewayContext);
        MDC.put(AUTHENTICATED_CLIENT_MDC_KEY, authenticatedClient);
        if (StringUtils.hasText(authenticatedUser)) {
            MDC.put(AUTHENTICATED_USER_MDC_KEY, authenticatedUser);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(AUTHENTICATED_CLIENT_MDC_KEY);
            MDC.remove(AUTHENTICATED_USER_MDC_KEY);
        }
    }

    private boolean isExpectedSecret(String gatewaySecret) {
        if (!StringUtils.hasText(gatewaySecret)) {
            return false;
        }

        return MessageDigest.isEqual(
                gatewaySecret.getBytes(StandardCharsets.UTF_8),
                properties.getGatewaySharedSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    private JwtClaimsContext extractJwtClaimsContext(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return JwtClaimsContext.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return JwtClaimsContext.empty();
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(decodedPayload, Map.class);
            return new JwtClaimsContext(
                    firstNonBlank(
                            stringClaim(claims, "client_id"),
                            stringClaim(claims, "azp"),
                            stringClaim(claims, "iss")
                    ),
                    firstNonBlank(
                            stringClaim(claims, "sub"),
                            stringClaim(claims, "preferred_username"),
                            stringClaim(claims, "user_name")
                    ),
                    firstNonBlank(
                            stringClaim(claims, "scope"),
                            joinClaim(claims.get("scp")),
                            joinClaim(claims.get("scopes"))
                    )
            );
        } catch (IllegalArgumentException | IOException ex) {
            log.warn("[INGRESS-SECURITY] No fue posible leer claims del JWT reenviado por Kong: {}", ex.getMessage());
            return JwtClaimsContext.empty();
        }
    }

    private String stringClaim(Map<String, Object> claims, String claimName) {
        Object value = claims.get(claimName);
        return value instanceof String text && StringUtils.hasText(text) ? text : null;
    }

    private String joinClaim(Object claimValue) {
        if (claimValue instanceof String text && StringUtils.hasText(text)) {
            return text;
        }

        if (claimValue instanceof List<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .filter(StringUtils::hasText)
                    .reduce((left, right) -> left + " " + right)
                    .orElse(null);
        }

        return null;
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        log.warn("[INGRESS-SECURITY] {}", message);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("codigoResultado", "ERROR_SEGURIDAD_INGRESS");
        body.put("mensaje", message);
        body.put("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getWriter(), body);
    }

    private record JwtClaimsContext(
            String authenticatedClient,
            String authenticatedUser,
            String authenticatedScopes
    ) {
        private static JwtClaimsContext empty() {
            return new JwtClaimsContext(null, null, null);
        }
    }
}
