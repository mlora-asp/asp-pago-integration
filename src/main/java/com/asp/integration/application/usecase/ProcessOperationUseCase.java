package com.asp.integration.application.usecase;

import com.asp.integration.infrastructure.audit.AuditLogger;
import com.asp.integration.adapter.inbound.rest.mapper.CanonicalMapper;
import com.asp.integration.application.command.ProcessOperationCommand;
import com.asp.integration.application.service.InboundAdapterService;
import com.asp.integration.application.service.ProviderResolver;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Orquesta el flujo completo:
 *   ProcessOperationCommand -> CanonicalRequest -> ProviderResolver -> ProviderGateway -> CanonicalResponse
 *
 *
 * @autor: HJMB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessOperationUseCase {

    private final CanonicalMapper canonicalMapper;
    private final InboundAdapterService inboundAdapter;
    private final ProviderResolver providerResolver;
    private final AuditLogger auditLogger;

    public Mono<CanonicalResponse> process(ProcessOperationCommand command) {

        log.info("[{}] Iniciando operación tipo={} origen={}",
                command.correlationId(), command.request().getTipoOperacion(), command.sistemaOrigen());

        CanonicalRequest canonical = canonicalMapper.toCanonical(command.request());
        canonical.setCorrelationId(command.correlationId());
        canonical.setSistemaOrigen(command.sistemaOrigen());
        canonical.setAuthenticatedClient(command.authenticatedClient());
        canonical.setAuthenticatedUser(command.authenticatedUser());
        canonical.setAuthenticatedScopes(command.authenticatedScopes());
        canonical.setTimestamp(Instant.now());

        inboundAdapter.enriquecer(canonical);

        var client = providerResolver.resolve(canonical.getTargetProvider());

        return client.execute(canonical)
                .doOnSuccess(resp -> auditLogger.logSuccess(canonical, resp))
                .doOnError(err -> auditLogger.logError(canonical, err));
    }
}
