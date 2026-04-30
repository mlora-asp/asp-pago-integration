package com.asp.integration.application.port.outbound;

import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import reactor.core.publisher.Mono;

/**
 * Contrato que todo Outbound Adapter debe implementar.
 * El Bridge solo conoce esta interfaz, no la implementación concreta.
 *
 *
 * @autor: HJMB
 */
public interface ProviderGateway {

    /**
     * Ejecuta la operación en el proveedor externo.
     * @param request modelo canónico
     * @return respuesta canónica (ya normalizada)
     */
    Mono<CanonicalResponse> execute(CanonicalRequest request);

    /**
     * Identificador del proveedor que implementa este client.
     */
    String providerName();
}
