package com.asp.integration.application.service;

import com.asp.integration.domain.exception.OperationNotSupportedException;
import com.asp.integration.domain.model.ProviderId;
import com.asp.integration.shared.constants.OperationTypes;
import com.asp.integration.shared.constants.ResponseMessages;
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
            throw new OperationNotSupportedException(ResponseMessages.OPERACION_NO_SOPORTADA_PREFIX + operationType);
        }
        return route;
    }

    private List<OperationRoute> routes() {
        return List.of(
                route(OperationTypes.CAUDEX_CONSULTA_CUENTA_VISTA, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_CONSULTA_SALDOS_VISTA, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_CONSULTA_HISTORICO_VISTA, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_CONSULTA_SALDOS_CREDITO, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_ALTA_RELACION_CLIENTE, ProviderId.CAUDEX),
                route(OperationTypes.CAUDEX_ONBOARDING_ALTA, ProviderId.CAUDEX)
        );
    }

    private OperationRoute route(String operationType, ProviderId providerId) {
        return new OperationRoute(operationType, providerId);
    }
}
