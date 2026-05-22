package com.asp.integration.adapter.inbound.rest.mapper.asppago;

import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoBeneficiarioResponseDto;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.DatosBasicosPF;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.DatosBasicosPM;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.OnboardingRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoResponseDto;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.infrastructure.config.properties.AspPagoContractProperties;
import com.asp.integration.shared.constants.OperationTypes;
import com.asp.integration.shared.constants.ResponseCodes;
import com.asp.integration.shared.constants.ResponseMessages;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte entre contratos ASP Pago y modelos internos.
 *
 * @autor: HJMB
 */
@Component
@RequiredArgsConstructor
public class AspPagoContractMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final AspPagoContractProperties properties;

    public OperacionRequestDto toOperation(String operationType, String referencia, String canal, Object request) {
        Map<String, Object> datos = objectMapper.convertValue(request, MAP_TYPE);
        normalizeOperationData(operationType, datos);
        return OperacionRequestDto.builder()
                .tipoOperacion(operationType)
                .referencia(referencia)
                .canal(canal)
                .monto(resolveMonto(operationType, datos))
                .moneda(properties.getDefaultCurrency())
                .datos(datos)
                .build();
    }

    public AspPagoResponseDto toResponse(CanonicalResponse canonical) {
        int status = canonical.getHttpStatus() != null ? canonical.getHttpStatus() : HttpStatus.OK.value();
        return AspPagoResponseDto.builder()
                .code(status)
                .message(canonical.getMensaje())
                .error(status >= 400 ? canonical.getCodigoResultado() : null)
                .data(canonical.getResultado())
                .build();
    }

    public AspPagoBeneficiarioResponseDto toBeneficiarioResponse(CanonicalResponse canonical) {
        int status = canonical.getHttpStatus() != null ? canonical.getHttpStatus() : HttpStatus.OK.value();
        boolean error = status >= 400 || !ResponseCodes.SUCCESS.equalsIgnoreCase(canonical.getCodigoResultado());
        return AspPagoBeneficiarioResponseDto.builder()
                .code(status)
                .message(canonical.getMensaje())
                .error(error ? canonical.getCodigoResultado() : null)
                .data(canonical.getResultado())
                .build();
    }

    public AspPagoResponseDto toOnboardingResponse(CanonicalResponse canonical) {
        int status = canonical.getHttpStatus() != null ? canonical.getHttpStatus() : HttpStatus.OK.value();
        boolean success = status < 400 && ResponseCodes.SUCCESS.equalsIgnoreCase(canonical.getCodigoResultado());
        return AspPagoResponseDto.builder()
                .code(success ? properties.getSuccessCode() : status)
                .message(canonical.getMensaje())
                .error(success ? null : canonical.getCodigoResultado())
                .data(resolveOnboardingData(canonical.getResultado()))
                .build();
    }

    public String onboardingReferencia(String correlationId) {
        return properties.getOnboardingReferencePrefix() + correlationId;
    }

    public String onboardingCanal(OnboardingRequest request) {
        if (request == null) {
            return properties.getDefaultChannel();
        }

        DatosBasicosPF basicosPF = request.getDatosBasicosPF();
        if (basicosPF != null && basicosPF.getClaveCanalOperacion() != null && !basicosPF.getClaveCanalOperacion().isBlank()) {
            return basicosPF.getClaveCanalOperacion();
        }

        DatosBasicosPM basicosPM = request.getDatosBasicosPM();
        if (basicosPM != null && basicosPM.getClaveCanalOperacion() != null && !basicosPM.getClaveCanalOperacion().isBlank()) {
            return basicosPM.getClaveCanalOperacion();
        }

        return properties.getDefaultChannel();
    }

    public AspPagoResponseDto unsupported(String mensaje) {
        return AspPagoResponseDto.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(mensaje)
                .error(ResponseCodes.ERROR_INTERNO)
                .data(null)
                .build();
    }

    public AspPagoResponseDto dummy(String operationType, String mensaje) {
        return AspPagoResponseDto.builder()
                .code(HttpStatus.OK.value())
                .message(mensaje)
                .error(null)
                .data(dummyData(operationType))
                .build();
    }

    public AspPagoBeneficiarioResponseDto dummyBeneficiario() {
        return AspPagoBeneficiarioResponseDto.builder()
                .code(HttpStatus.OK.value())
                .message(ResponseMessages.BENEFICIARIO_REGISTRADO)
                .error(null)
                .data(null)
                .build();
    }

    private Object dummyData(String operationType) {
        return switch (operationType) {
            case OperationTypes.CAUDEX_CONSULTA_SALDOS_VISTA -> mapOfNullable(
                    "sdoTotal", 4621.50,
                    "sdoDisponible", 4621.50,
                    "sdoCapital", 4621.50,
                    "sdoSBCretenido", 0.0,
                    "sdoFomentoAhorro", 0.0,
                    "sdoInteres", 0.0,
                    "comisionesPenditentes", 0.0,
                    "sdoSBCliberarHoy", 0.0,
                    "sdoEnreciprocidad", null,
                    "sdoRetenido", null
            );
            case OperationTypes.CAUDEX_CONSULTA_CUENTA_VISTA -> Map.ofEntries(
                    Map.entry("numeroMoneda", 1),
                    Map.entry("descripcionMoneda", "PESOS"),
                    Map.entry("numeroProducto", 101),
                    Map.entry("descripcionProducto", "CUENTA VISTA"),
                    Map.entry("numeroSucursal", 1),
                    Map.entry("nombreSucursal", "SUCURSAL MATRIZ"),
                    Map.entry("idPromotor", "PROM001"),
                    Map.entry("primerNombrePromotor", "JUAN"),
                    Map.entry("apellidoPaternoPromotor", "PEREZ"),
                    Map.entry("fechaApertura", "2025-01-01T10:00:00"),
                    Map.entry("indCuentaRestringida", false),
                    Map.entry("indPagaInteres", true),
                    Map.entry("intPeriodicidadPago", 30),
                    Map.entry("descripcionPeriodicidadPago", "MENSUAL")
            );
            case OperationTypes.CAUDEX_CONSULTA_HISTORICO_VISTA -> java.util.List.of(
                    Map.of(
                            "fechaOperacion", "2025-06-20T12:00:00",
                            "transaccionExterna", 1001,
                            "conceptoTransaccion", "DEPOSITO",
                            "saldoAntes", 4000.00,
                            "montoTransaccion", 621.50,
                            "saldoDespues", 4621.50,
                            "numeroConsecutivo", 1,
                            "folioOperacion", 987654321L
                    )
            );
            case OperationTypes.CAUDEX_CONSULTA_SALDOS_CREDITO -> Map.ofEntries(
                    Map.entry("sdoTotal", 15000.00),
                    Map.entry("numPagosVencidos", 0),
                    Map.entry("sdoPagosVencidos", 0.0),
                    Map.entry("sdoCapitalTotal", 12000.00),
                    Map.entry("sdoCapitalVigente", 12000.00),
                    Map.entry("sdoCapitalVencido", 0.0),
                    Map.entry("sdoInteresTotal", 3000.00),
                    Map.entry("sdoInteresVigente", 3000.00),
                    Map.entry("sdoInteresVencido", 0.0),
                    Map.entry("indArrendamiento", false),
                    Map.entry("fechaAlta", "2025-01-01T10:00:00"),
                    Map.entry("idUsuarioAlta", "USRCONFIG")
            );
            case OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA -> Map.of(
                    "folioOperacion", "987654321",
                    "fechaAplicacion", "2026-04-29",
                    "statusTransaccion", "1",
                    "dtoMoneda", Map.of(
                            "numeroMoneda", 1,
                            "descripcionCortaMoneda", properties.getDefaultCurrency()
                    )
            );
            case OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA -> Map.of(
                    "folioOperacion", "987654322",
                    "fechaAplicacion", "2026-04-29",
                    "statusTransaccion", "1",
                    "dtoMoneda", Map.of(
                            "numeroMoneda", 1,
                            "descripcionCortaMoneda", properties.getDefaultCurrency()
                    )
            );
            case OperationTypes.CAUDEX_CONSULTA_PERFIL_TRANSACCIONAL -> Map.of(
                    "perfiles", java.util.List.of(Map.ofEntries(
                            Map.entry("numeroCliente", 102733),
                            Map.entry("numeroConsecutivo", 1),
                            Map.entry("numeroProducto", 101),
                            Map.entry("descripcionProducto", "CUENTA VISTA"),
                            Map.entry("numeroTipoCredito", 1),
                            Map.entry("monedaPago", properties.getDefaultCurrency()),
                            Map.entry("descripcionMoneda", "PESOS"),
                            Map.entry("montoSolicitado", 10000.00),
                            Map.entry("plazoMeses", 12),
                            Map.entry("periodicidadOperaciones", 30),
                            Map.entry("descripcionPeriodicidad", "MENSUAL"),
                            Map.entry("claveInstrumentoMonetario", "T"),
                            Map.entry("descripcionInstrumentoMonetario", "TRANSFERENCIA"),
                            Map.entry("numeroOperaciones", 10),
                            Map.entry("limiteOperaciones", 20),
                            Map.entry("usoCuenta", 1),
                            Map.entry("descripcionUsoCuenta", "NOMINA"),
                            Map.entry("numeroDestinoCredito", 1),
                            Map.entry("descripcionDestinoCredito", "CONSUMO"),
                            Map.entry("ubicacionProyecto", 9),
                            Map.entry("descripcionEstado", "CIUDAD DE MEXICO"),
                            Map.entry("numeroOrigenContrato", 1),
                            Map.entry("descripcionOrigenContrato", "APP"),
                            Map.entry("fechaVigencia", "2026-12-31"),
                            Map.entry("fechaPermiteMtto", "2026-12-31"),
                            Map.entry("status", 1),
                            Map.entry("descripcionStatus", "ACTIVO")
                    ))
            );
            case OperationTypes.PAGO_CREDITO_DUMMY -> Map.of(
                    "folioOperacion", "PC-987654321",
                    "fechaAplicacion", "2026-04-29",
                    "statusTransaccion", 1
            );
            case OperationTypes.LOGIN_DUMMY -> Map.of(
                    "accessToken", "dummy-access-token",
                    "refreshToken", "dummy-refresh-token",
                    "tokenType", "Bearer",
                    "expiresIn", 3600,
                    "usuario", "USRCONFIG"
            );
            case OperationTypes.LOGOUT_DUMMY -> Map.of(
                    "sesionCerrada", true,
                    "fechaCierre", "2026-04-29T12:00:00"
            );
            case OperationTypes.PASSWORD_OTP_DUMMY -> Map.of(
                    "expiracion", "2026-04-29T12:05:00",
                    "reintentosRestantes", 3,
                    "canalEnvio", "SMS"
            );
            case OperationTypes.PASSWORD_RESET_DUMMY -> Map.of(
                    "passwordActualizado", true,
                    "requiereLogin", true,
                    "fechaCambio", "2026-04-29T12:00:00"
            );
            case OperationTypes.ONBOARDING_DUMMY -> Map.of("numeroCliente", 111386);
            default -> null;
        };
    }

    private Map<String, Object> mapOfNullable(Object... entries) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            values.put(String.valueOf(entries[index]), entries[index + 1]);
        }
        return values;
    }

    private void normalizeOperationData(String operationType, Map<String, Object> datos) {
        if (OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA.equals(operationType)) {
            copyIfAbsent(datos, "numeroCuenta", "numeroCuentaOrigen");
            copyIfAbsent(datos, "monto", "montoTransaccion");
            datos.computeIfPresent("claveCanalOperacion", (key, value) -> channelCode(value.toString()));
            datos.computeIfPresent("instrumentoMonetario", (key, value) -> instrumentCode(value.toString()));
            datos.computeIfPresent("tipoCuentaEje", (key, value) -> accountTypeCode(value.toString()));
            return;
        }

        if (OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA.equals(operationType)) {
            copyIfAbsent(datos, "montoTransaccion", "monto");
            return;
        }

        if (OperationTypes.CAUDEX_ALTA_RELACION_CLIENTE.equals(operationType)) {
            Object relacion = datos.get("beneficiarioRelacion");
            if (relacion instanceof Map<?, ?> map) {
                Map<String, Object> flattened = new LinkedHashMap<>();
                map.forEach((key, value) -> flattened.put(String.valueOf(key), value));
                datos.clear();
                datos.putAll(flattened);
            }
        }
    }

    private BigDecimal resolveMonto(String operationType, Map<String, Object> datos) {
        Object raw = switch (operationType) {
            case OperationTypes.CAUDEX_DEPOSITO_CUENTA_VISTA -> datos.get("monto");
            case OperationTypes.CAUDEX_RETIRO_CUENTA_VISTA -> datos.get("montoTransaccion");
            default -> null;
        };

        if (raw instanceof BigDecimal value) {
            return value;
        }
        if (raw instanceof Number value) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        if (raw != null) {
            try {
                return new BigDecimal(raw.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void copyIfAbsent(Map<String, Object> datos, String target, String source) {
        if (!datos.containsKey(target) && datos.containsKey(source)) {
            datos.put(target, datos.get(source));
        }
    }

    private Integer channelCode(String value) {
        return properties.getChannelCodes().getOrDefault(value, properties.getDefaultChannelCode());
    }

    private Integer instrumentCode(String value) {
        return properties.getInstrumentCodes().getOrDefault(value, properties.getDefaultInstrumentCode());
    }

    private Integer accountTypeCode(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return properties.getDefaultAccountTypeCode();
        }
    }

    @SuppressWarnings("unchecked")
    private Object resolveOnboardingData(Object resultado) {
        if (resultado instanceof Map<?, ?> rawMap) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            rawMap.forEach((key, value) -> normalized.put(String.valueOf(key), value));

            if (normalized.containsKey("numeroCliente")) {
                return Map.of("numeroCliente", normalized.get("numeroCliente"));
            }
            if (normalized.containsKey("numCliente")) {
                return Map.of("numeroCliente", normalized.get("numCliente"));
            }
            return normalized;
        }

        return resultado;
    }
}
