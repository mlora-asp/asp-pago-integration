package com.asp.integration.adapter.inbound.rest.mapper;

import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import org.springframework.stereotype.Component;

/**
 * @autor: HJMB
 */
@Component
public class CanonicalMapper {

    public CanonicalRequest toCanonical(OperacionRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return CanonicalRequest.builder()
                .operationType(dto.getTipoOperacion())
                .referencia(dto.getReferencia())
                .monto(dto.getMonto())
                .moneda(dto.getMoneda())
                .canal(dto.getCanal())
                .datos(dto.getDatos())
                .build();
    }
}
