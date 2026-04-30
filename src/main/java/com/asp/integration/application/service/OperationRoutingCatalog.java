package com.asp.integration.application.service;

import com.asp.integration.domain.exception.OperationNotSupportedException;
import com.asp.integration.domain.model.ProviderId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @autor: HJMB
 */
@Component
public class OperationRoutingCatalog {

    private final Map<String, OperationRoute> routesByOperation;

    public OperationRoutingCatalog() {
        this.routesByOperation = routes().stream()
                .collect(Collectors.toUnmodifiableMap(OperationRoute::operationType, Function.identity()));
    }

    public OperationRoute resolve(String operationType) {
        OperationRoute route = routesByOperation.get(operationType.toUpperCase());
        if (route == null) {
            throw new OperationNotSupportedException("Tipo de operación no soportado: " + operationType);
        }
        return route;
    }

    private List<OperationRoute> routes() {
        return List.of(
                route("CAUDEX_CONSULTA_CUENTA_VISTA", ProviderId.CAUDEX),
                route("CAUDEX_CONSULTA_SALDOS_VISTA", ProviderId.CAUDEX),
                route("CAUDEX_DEPOSITO_CUENTA_VISTA", ProviderId.CAUDEX),
                route("CAUDEX_RETIRO_CUENTA_VISTA", ProviderId.CAUDEX),
                route("CAUDEX_CONSULTA_HISTORICO_VISTA", ProviderId.CAUDEX),
                route("CAUDEX_CONSULTA_SALDOS_CREDITO", ProviderId.CAUDEX),
                route("CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL", ProviderId.CAUDEX),
                route("CAUDEX_ALTA_RELACION_CLIENTE", ProviderId.CAUDEX)
        );
    }

    private OperationRoute route(String operationType, ProviderId providerId) {
        return new OperationRoute(operationType, providerId);
    }
}
