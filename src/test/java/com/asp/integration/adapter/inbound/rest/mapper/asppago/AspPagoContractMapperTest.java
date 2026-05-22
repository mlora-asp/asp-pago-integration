package com.asp.integration.adapter.inbound.rest.mapper.asppago;

import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoResponseDto;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.infrastructure.config.properties.AspPagoContractProperties;
import com.asp.integration.shared.constants.ResponseCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del mapper ASP Pago.
 *
 * @autor: HJMB
 */
class AspPagoContractMapperTest {

    private final AspPagoContractMapper mapper = new AspPagoContractMapper(
            new ObjectMapper(),
            contractProperties()
    );

    @Test
    void shouldNormalizeNumeroClienteForOnboardingResponse() {
        CanonicalResponse canonical = CanonicalResponse.builder()
                .codigoResultado(ResponseCodes.SUCCESS)
                .mensaje("Operación realizada satisfactoriamente")
                .httpStatus(200)
                .resultado(Map.of("numCliente", 111386, "detalle", "OK"))
                .build();

        AspPagoResponseDto response = mapper.toOnboardingResponse(canonical);

        assertThat(response.getCode()).isEqualTo(9999);
        assertThat(response.getError()).isNull();
        assertThat(response.getData()).isEqualTo(Map.of("numeroCliente", 111386));
    }

    private static AspPagoContractProperties contractProperties() {
        AspPagoContractProperties properties = new AspPagoContractProperties();
        properties.setSuccessCode(9999);
        properties.setDefaultCurrency("MXN");
        properties.setDefaultChannel("API");
        properties.setOnboardingReferencePrefix("ONBOARDING-");
        properties.setDefaultChannelCode(1);
        properties.setDefaultInstrumentCode(1);
        properties.setDefaultAccountTypeCode(1);
        properties.setChannelCodes(Map.of("VP", 1, "VF", 1, "APP", 2, "WEB", 3, "API", 4));
        properties.setInstrumentCodes(Map.of("E", 1, "T", 2, "C", 3));
        return properties;
    }
}
