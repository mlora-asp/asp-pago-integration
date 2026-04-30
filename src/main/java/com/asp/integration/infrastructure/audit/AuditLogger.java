package com.asp.integration.infrastructure.audit;

import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Registra cada transacción en el log estructurado JSON.
 * En producción este log puede ser capturado por Cloud Logging,
 * ELK Stack o cualquier sistema de observabilidad.
 *
 * @autor: HJMB
 */
@Slf4j
@Component
public class AuditLogger {

    public void logSuccess(CanonicalRequest req, CanonicalResponse resp) {
        MDC.put("correlationId", req.getCorrelationId());
        MDC.put("operationType", req.getOperationType());
        MDC.put("proveedor", resp.getProveedor());
        MDC.put("codigoResultado", resp.getCodigoResultado());
        MDC.put("sistemaOrigen", req.getSistemaOrigen());
        MDC.put("authenticatedClient", req.getAuthenticatedClient());
        if (req.getAuthenticatedUser() != null) {
            MDC.put("authenticatedUser", req.getAuthenticatedUser());
        }
        MDC.put("evento", "INTEGRACION_OK");

        log.info("[AUDIT] correlationId={} op={} proveedor={} resultado={} origen={} cliente={} usuario={}",
                req.getCorrelationId(),
                req.getOperationType(),
                resp.getProveedor(),
                resp.getCodigoResultado(),
                req.getSistemaOrigen(),
                req.getAuthenticatedClient(),
                req.getAuthenticatedUser());

        MDC.clear();
    }

    public void logError(CanonicalRequest req, Throwable error) {
        MDC.put("correlationId", req.getCorrelationId());
        MDC.put("operationType", req.getOperationType());
        MDC.put("sistemaOrigen", req.getSistemaOrigen());
        MDC.put("targetProvider", req.getTargetProvider());
        MDC.put("authenticatedClient", req.getAuthenticatedClient());
        if (req.getAuthenticatedUser() != null) {
            MDC.put("authenticatedUser", req.getAuthenticatedUser());
        }
        MDC.put("evento", "INTEGRACION_ERROR");

        log.error("[AUDIT] correlationId={} op={} proveedor={} cliente={} usuario={} error={}",
                req.getCorrelationId(),
                req.getOperationType(),
                req.getTargetProvider(),
                req.getAuthenticatedClient(),
                req.getAuthenticatedUser(),
                error.getMessage());

        MDC.clear();
    }
}
