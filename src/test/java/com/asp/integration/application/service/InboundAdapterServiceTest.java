package com.asp.integration.application.service;

import com.asp.integration.domain.exception.OperationNotSupportedException;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundAdapterServiceTest {

    private final InboundAdapterService service = new InboundAdapterService(new OperationRoutingCatalog());

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
        "CAUDEX_CONSULTA_CUENTA_VISTA,          CAUDEX",
        "CAUDEX_CONSULTA_SALDOS_VISTA,          CAUDEX",
        "CAUDEX_DEPOSITO_CUENTA_VISTA,          CAUDEX",
        "CAUDEX_RETIRO_CUENTA_VISTA,            CAUDEX",
        "CAUDEX_CONSULTA_HISTORICO_VISTA,       CAUDEX",
        "CAUDEX_CONSULTA_SALDOS_CREDITO,        CAUDEX",
        "CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL,  CAUDEX",
        "CAUDEX_ALTA_RELACION_CLIENTE,          CAUDEX"
    })
    void deberiaResolverProveedorCorrecto(String operationType, String expectedProvider) {
        CanonicalRequest req = CanonicalRequest.builder()
                .correlationId("test-123")
                .operationType(operationType)
                .build();

        service.enriquecer(req);

        assertThat(req.getTargetProvider()).isEqualTo(expectedProvider);
    }

    @Test
    void deberiLanzarExcepcionParaOperacionDesconocida() {
        CanonicalRequest req = CanonicalRequest.builder()
                .correlationId("test-999")
                .operationType("OPERACION_INEXISTENTE")
                .build();

        assertThatThrownBy(() -> service.enriquecer(req))
                .isInstanceOf(OperationNotSupportedException.class)
                .hasMessageContaining("OPERACION_INEXISTENTE");
    }
}
