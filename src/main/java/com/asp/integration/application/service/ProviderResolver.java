package com.asp.integration.application.service;

import com.asp.integration.application.port.outbound.ProviderGateway;
import com.asp.integration.domain.exception.ProviderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve qué ProviderGateway usar según el proveedor destino.
 * Para agregar un nuevo proveedor: implementar ProviderGateway y registrar Spring @Component.
 * Nada más cambia aquí.
 *
 *
 * @autor: HJMB
 */
@Slf4j
@Component
public class ProviderResolver {

    private final Map<String, ProviderGateway> clientsByName;

    /** Spring inyecta automáticamente todos los beans que implementen ProviderGateway */
    public ProviderResolver(List<ProviderGateway> clients) {
        this.clientsByName = clients.stream()
                .collect(Collectors.toMap(
                        c -> c.providerName().toUpperCase(),
                        Function.identity()
                ));
        log.info("ProviderResolver registró {} proveedores: {}", clientsByName.size(), clientsByName.keySet());
    }

    public ProviderGateway resolve(String providerName) {
        String key = providerName.toUpperCase();
        ProviderGateway client = clientsByName.get(key);
        if (client == null) {
            throw new ProviderNotFoundException("Proveedor no registrado: " + providerName);
        }
        return client;
    }
}
