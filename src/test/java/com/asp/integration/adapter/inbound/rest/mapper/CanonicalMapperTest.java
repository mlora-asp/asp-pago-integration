package com.asp.integration.adapter.inbound.rest.mapper;

import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalMapperTest {

    private final CanonicalMapper mapper = new CanonicalMapper();

    @Test
    void deberiaMapearRequestDtoACanonical() {
        OperacionRequestDto dto = OperacionRequestDto.builder()
                .tipoOperacion("CONSULTA_CURP")
                .referencia("REF-001")
                .canal("API")
                .moneda("MXN")
                .monto(BigDecimal.valueOf(500.00))
                .datos(Map.of("curp", "GOAM821010HDFLLN01"))
                .build();

        CanonicalRequest canonical = mapper.toCanonical(dto);

        assertThat(canonical.getOperationType()).isEqualTo("CONSULTA_CURP");
        assertThat(canonical.getReferencia()).isEqualTo("REF-001");
        assertThat(canonical.getCanal()).isEqualTo("API");
        assertThat(canonical.getMoneda()).isEqualTo("MXN");
        assertThat(canonical.getMonto()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        // correlationId, targetProvider, timestamp se asignan en el service
        assertThat(canonical.getCorrelationId()).isNull();
    }

}
