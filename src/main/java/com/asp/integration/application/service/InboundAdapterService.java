package com.asp.integration.application.service;

import com.asp.integration.domain.model.canonical.CanonicalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Identifica la familia contractual de la operación
 * y asigna el proveedor destino en el CanonicalRequest.
 *
 * Para agregar un nuevo tipo de operación:
 *   1. Agregar un case aquí con el proveedor correspondiente.
 *   2. Implementar un nuevo ProviderGateway para ese proveedor.
 *   Ningún contrato de entrada se toca.
 *
 *
 * @autor: HJMB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundAdapterService {

    private final OperationRoutingCatalog routingCatalog;

    public void enriquecer(CanonicalRequest request) {
        String provider = routingCatalog.resolve(request.getOperationType()).providerId().name();
        request.setTargetProvider(provider);
        log.debug("[{}] operationType={} → proveedor={}", request.getCorrelationId(), request.getOperationType(), provider);
    }
}
