package com.asp.integration.adapter.outbound.provider.mapper;

import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexAltaCuentaVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaBuroCreditoRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaCatalogoRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaClienteRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaCuentaVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaDomicilioDetalleRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaHistoricoVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaMovimientosCreditoRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaSaldosCreditoRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexConsultaSaldosVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexDepositoCuentaVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexEnvioCorreoRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexEnvioSmsRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexKycRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexResponseDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexRetiroCuentaVistaRequestDto;
import com.asp.integration.adapter.outbound.provider.dto.caudex.CaudexValidacionCurpRequestDto;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.shared.constants.ResponseCodes;
import com.asp.integration.shared.constants.ResponseMessages;

/**
 * Mapper de contratos internos hacia Caudex.
 *
 * @autor: HJMB
 */
@Mapper(componentModel = "spring")
public interface CaudexMapper {

    // ── Consulta cliente (por número, CURP o RFC) ───────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numCliente",        expression = "java(getLongFromDatos(request.getDatos(), \"numCliente\"))")
    @Mapping(target = "curp",              expression = "java(getStringFromDatos(request.getDatos(), \"curp\"))")
    @Mapping(target = "rfc",               expression = "java(getStringFromDatos(request.getDatos(), \"rfc\"))")
    CaudexConsultaClienteRequestDto toConsultaClienteRequest(CanonicalRequest request);

    // ── Alta cuenta vista ────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numCliente",        expression = "java(getStringFromDatos(request.getDatos(), \"numCliente\"))")
    @Mapping(target = "numMoneda",         expression = "java(getStringFromDatos(request.getDatos(), \"numMoneda\"))")
    @Mapping(target = "numProducto",       expression = "java(getStringFromDatos(request.getDatos(), \"numProducto\"))")
    @Mapping(target = "retieneImptoInteres", expression = "java(getStringFromDatos(request.getDatos(), \"retieneImptoInteres\"))")
    @Mapping(target = "numImptoInteres",   expression = "java(getStringFromDatos(request.getDatos(), \"numImptoInteres\"))")
    @Mapping(target = "cobraComisiones",   expression = "java(getStringFromDatos(request.getDatos(), \"cobraComisiones\"))")
    @Mapping(target = "planComisiones",    expression = "java(getStringFromDatos(request.getDatos(), \"planComisiones\"))")
    @Mapping(target = "numSucursal",       expression = "java(getStringFromDatos(request.getDatos(), \"numSucursal\"))")
    @Mapping(target = "idPromotor",        expression = "java(getStringFromDatos(request.getDatos(), \"idPromotor\"))")
    @Mapping(target = "fechaApertura",     expression = "java(getStringFromDatos(request.getDatos(), \"fechaApertura\"))")
    @Mapping(target = "indCuentaRestringida", expression = "java(getStringFromDatos(request.getDatos(), \"indCuentaRestringida\"))")
    @Mapping(target = "intPagaInteres",    expression = "java(getStringFromDatos(request.getDatos(), \"intPagaInteres\"))")
    @Mapping(target = "intPeriodicidadPago", expression = "java(getStringFromDatos(request.getDatos(), \"intPeriodicidadPago\"))")
    @Mapping(target = "intDiaPago1",       expression = "java(getStringFromDatos(request.getDatos(), \"intDiaPago1\"))")
    @Mapping(target = "fechaProximoPagoInt", expression = "java(getStringFromDatos(request.getDatos(), \"fechaProximoPagoInt\"))")
    @Mapping(target = "intPagoInhabil",    expression = "java(getStringFromDatos(request.getDatos(), \"intPagoInhabil\"))")
    @Mapping(target = "intBaseCalculo",    expression = "java(getStringFromDatos(request.getDatos(), \"intBaseCalculo\"))")
    @Mapping(target = "sdoMinCalculoInt",  expression = "java(getStringFromDatos(request.getDatos(), \"sdoMinCalculoInt\"))")
    @Mapping(target = "sdoMaxCalculoInt",  expression = "java(getStringFromDatos(request.getDatos(), \"sdoMaxCalculoInt\"))")
    @Mapping(target = "intTipoTasa",       expression = "java(getStringFromDatos(request.getDatos(), \"intTipoTasa\"))")
    @Mapping(target = "intTasa",           expression = "java(getStringFromDatos(request.getDatos(), \"intTasa\"))")
    @Mapping(target = "intNumMatrizTasa",  expression = "java(getStringFromDatos(request.getDatos(), \"intNumMatrizTasa\"))")
    @Mapping(target = "indPlanRetiros",    expression = "java(getStringFromDatos(request.getDatos(), \"indPlanRetiros\"))")
    @Mapping(target = "numPlanRetiros",    expression = "java(getIntFromDatos(request.getDatos(), \"numPlanRetiros\", 0))")
    @Mapping(target = "fechaUltimaActualizacion", expression = "java(getStringFromDatos(request.getDatos(), \"fechaUltimaActualizacion\"))")
    @Mapping(target = "fechaProximoCobro", expression = "java(getStringFromDatos(request.getDatos(), \"fechaProximoCobro\"))")
    @Mapping(target = "regimenCuenta",     expression = "java(getStringFromDatos(request.getDatos(), \"regimenCuenta\"))")
    @Mapping(target = "nivelCuenta",       expression = "java(getStringFromDatos(request.getDatos(), \"nivelCuenta\"))")
    @Mapping(target = "idUsuario",         expression = "java(getStringFromDatos(request.getDatos(), \"idUsuario\"))")
    @Mapping(target = "nip",               expression = "java(getStringFromDatos(request.getDatos(), \"nip\"))")
    CaudexAltaCuentaVistaRequestDto toAltaCuentaVistaRequest(CanonicalRequest request);

    // ── Consulta cuenta vista ────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    CaudexConsultaCuentaVistaRequestDto toConsultaCuentaVistaRequest(CanonicalRequest request);

    // ── KYC ─────────────────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCliente",     expression = "java(getLongFromDatos(request.getDatos(), \"numeroCliente\"))")
    @Mapping(target = "indPoliticamenteExpuesta", expression = "java(getIntFromDatos(request.getDatos(), \"indPoliticamenteExpuesta\", 0))")
    @Mapping(target = "cargoPoliticamenteExpuesta", expression = "java(getStringFromDatos(request.getDatos(), \"cargoPoliticamenteExpuesta\"))")
    @Mapping(target = "periodoPoliticamenteExpuesta", expression = "java(getStringFromDatos(request.getDatos(), \"periodoPoliticamenteExpuesta\"))")
    @Mapping(target = "indRelPersonaPoliticamente", expression = "java(getIntFromDatos(request.getDatos(), \"indRelPersonaPoliticamente\", 0))")
    @Mapping(target = "indPersonaPoliticamenteClienteInstitucion", expression = "java(getStringFromDatos(request.getDatos(), \"indPersonaPoliticamenteClienteInstitucion\"))")
    @Mapping(target = "numeroClienteRelacionadoPersonaPoliticamente", expression = "java(getLongFromDatos(request.getDatos(), \"numeroClienteRelacionadoPersonaPoliticamente\"))")
    @Mapping(target = "indRelacionadoSociedadMercantil", expression = "java(getIntFromDatos(request.getDatos(), \"indRelacionadoSociedadMercantil\", 0))")
    @Mapping(target = "indImportaciones",  expression = "java(getIntFromDatos(request.getDatos(), \"indImportaciones\", 0))")
    @Mapping(target = "indExportaciones",  expression = "java(getIntFromDatos(request.getDatos(), \"indExportaciones\", 0))")
    @Mapping(target = "indAcreditadoOtraInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"indAcreditadoOtraInstitucion\", 0))")
    @Mapping(target = "indResideExtranjero", expression = "java(getIntFromDatos(request.getDatos(), \"indResideExtranjero\", 0))")
    @Mapping(target = "origenIngresos",    expression = "java(getIntFromDatos(request.getDatos(), \"origenIngresos\", 0))")
    @Mapping(target = "origenIngresosOtros", expression = "java(getStringFromDatos(request.getDatos(), \"origenIngresosOtros\"))")
    @Mapping(target = "origenRecursos",    expression = "java(getIntFromDatos(request.getDatos(), \"origenRecursos\", 0))")
    @Mapping(target = "nombreTercero",     expression = "java(getStringFromDatos(request.getDatos(), \"nombreTercero\"))")
    @Mapping(target = "claveInstrumentoMonetario", expression = "java(getStringFromDatos(request.getDatos(), \"claveInstrumentoMonetario\"))")
    @Mapping(target = "numeroMonedaIngresos", expression = "java(getIntFromDatos(request.getDatos(), \"numeroMonedaIngresos\", 1))")
    @Mapping(target = "indActuaporTerceros", expression = "java(getStringFromDatos(request.getDatos(), \"indActuaporTerceros\"))")
    @Mapping(target = "claveOrigenCliente", expression = "java(getIntFromDatos(request.getDatos(), \"claveOrigenCliente\", 1))")
    @Mapping(target = "idUsuario",         expression = "java(getStringFromDatos(request.getDatos(), \"idUsuario\"))")
    @Mapping(target = "macAddress",        expression = "java(getStringFromDatos(request.getDatos(), \"macAddress\"))")
    @Mapping(target = "ClaveFuncion",      expression = "java(getStringFromDatos(request.getDatos(), \"claveFuncion\"))")
    @Mapping(target = "nombrePrograma",    expression = "java(getStringFromDatos(request.getDatos(), \"nombrePrograma\"))")
    @Mapping(target = "statusCliente",     expression = "java(getIntFromDatos(request.getDatos(), \"statusCliente\", 1))")
    @Mapping(target = "fechaAlta",         expression = "java(getStringFromDatos(request.getDatos(), \"fechaAlta\"))")
    @Mapping(target = "montoIngresosAnuales", ignore = true)
    @Mapping(target = "montotMensualImportaciones", ignore = true)
    @Mapping(target = "montoMensualExportaciones", ignore = true)
    @Mapping(target = "numeroPaisOrigen", ignore = true)
    @Mapping(target = "numeroPaisDestino", ignore = true)
    @Mapping(target = "numeroTipoRelacion", ignore = true)
    @Mapping(target = "numeroRelacion", ignore = true)
    @Mapping(target = "descripcionSociedadMercantil", ignore = true)
    CaudexKycRequestDto toKycRequest(CanonicalRequest request);

    // ── Consulta saldos vista (PRIORITY — asp-pago-management end-to-end) ───

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    CaudexConsultaSaldosVistaRequestDto toConsultaSaldosVistaRequest(CanonicalRequest request);

    // ── Depósito cuenta vista ────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion",      expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",           expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "monto",                  expression = "java(getBigDecimalFromDatos(request.getDatos(), \"monto\"))")
    @Mapping(target = "referenciaNumerica",     expression = "java(getLongFromDatos(request.getDatos(), \"referenciaNumerica\"))")
    @Mapping(target = "referenciaAlfanumerica", expression = "java(getStringFromDatos(request.getDatos(), \"referenciaAlfanumerica\"))")
    @Mapping(target = "idUsuario",              expression = "java(getStringFromDatos(request.getDatos(), \"idUsuario\"))")
    @Mapping(target = "sistemaOperacion",       expression = "java(getStringFromDatos(request.getDatos(), \"sistemaOperacion\"))")
    CaudexDepositoCuentaVistaRequestDto toDepositoCuentaVistaRequest(CanonicalRequest request);

    // ── Retiro cuenta vista ──────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion",      expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",           expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "monto",                  expression = "java(getBigDecimalFromDatos(request.getDatos(), \"monto\"))")
    @Mapping(target = "referenciaNumerica",     expression = "java(getLongFromDatos(request.getDatos(), \"referenciaNumerica\"))")
    @Mapping(target = "referenciaAlfanumerica", expression = "java(getStringFromDatos(request.getDatos(), \"referenciaAlfanumerica\"))")
    @Mapping(target = "numeroCuentaEje",        expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuentaEje\"))")
    @Mapping(target = "claveCanalOperacion",    expression = "java(getIntFromDatos(request.getDatos(), \"claveCanalOperacion\", 1))")
    @Mapping(target = "instrumentoMonetario",   expression = "java(getIntFromDatos(request.getDatos(), \"instrumentoMonetario\", 1))")
    @Mapping(target = "idUsuario",              expression = "java(getStringFromDatos(request.getDatos(), \"idUsuario\"))")
    @Mapping(target = "sistemaOperacion",       expression = "java(getStringFromDatos(request.getDatos(), \"sistemaOperacion\"))")
    @Mapping(target = "tipoCuentaEje",          expression = "java(getIntFromDatos(request.getDatos(), \"tipoCuentaEje\", 1))")
    @Mapping(target = "fechaAplicacion",        expression = "java(getStringFromDatos(request.getDatos(), \"fechaAplicacion\"))")
    @Mapping(target = "montoTransaccion",       expression = "java(getBigDecimalFromDatos(request.getDatos(), \"montoTransaccion\"))")
    CaudexRetiroCuentaVistaRequestDto toRetiroCuentaVistaRequest(CanonicalRequest request);

    // ── Consulta histórico vista ─────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "fechaInicio",       expression = "java(getStringFromDatos(request.getDatos(), \"fechaInicio\"))")
    @Mapping(target = "fechaFin",          expression = "java(getStringFromDatos(request.getDatos(), \"fechaFin\"))")
    CaudexConsultaHistoricoVistaRequestDto toConsultaHistoricoVistaRequest(CanonicalRequest request);

    // ── Buró de crédito ──────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getStringFromDatos(request.getDatos(), \"numeroInstitucion\"))")
    @Mapping(target = "numCliente",        expression = "java(getStringFromDatos(request.getDatos(), \"numCliente\"))")
    @Mapping(target = "macAddress",        expression = "java(getStringFromDatos(request.getDatos(), \"macAddress\"))")
    @Mapping(target = "fechaConsulta",     expression = "java(getStringFromDatos(request.getDatos(), \"fechaConsulta\"))")
    @Mapping(target = "idUsuario",         expression = "java(getIntFromDatos(request.getDatos(), \"idUsuario\", 0))")
    CaudexConsultaBuroCreditoRequestDto toConsultaBuroCreditoRequest(CanonicalRequest request);

    // ── Envío SMS ────────────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCliente",     expression = "java(getLongFromDatos(request.getDatos(), \"numeroCliente\"))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "fechaMensaje",      expression = "java(getStringFromDatos(request.getDatos(), \"fechaMensaje\"))")
    @Mapping(target = "mensaje",           expression = "java(getStringFromDatos(request.getDatos(), \"mensaje\"))")
    @Mapping(target = "numeroTelefonico",  expression = "java(getLongFromDatos(request.getDatos(), \"numeroTelefonico\"))")
    CaudexEnvioSmsRequestDto toEnvioSmsRequest(CanonicalRequest request);

    // ── Envío correo electrónico ─────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "fecha",             expression = "java(getStringFromDatos(request.getDatos(), \"fecha\"))")
    @Mapping(target = "numeroCliente",     expression = "java(getLongFromDatos(request.getDatos(), \"numeroCliente\"))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "encabezado",        expression = "java(getStringFromDatos(request.getDatos(), \"encabezado\"))")
    @Mapping(target = "cuerpo",            expression = "java(getStringFromDatos(request.getDatos(), \"cuerpo\"))")
    @Mapping(target = "correoReceptor",    expression = "java(getStringFromDatos(request.getDatos(), \"correoReceptor\"))")
    CaudexEnvioCorreoRequestDto toEnvioCorreoRequest(CanonicalRequest request);

    // ── Validación CURP ──────────────────────────────────────────────────────

    @Mapping(target = "nombre",            expression = "java(getStringFromDatos(request.getDatos(), \"nombre\"))")
    @Mapping(target = "apellidoPaterno",   expression = "java(getStringFromDatos(request.getDatos(), \"apellidoPaterno\"))")
    @Mapping(target = "apellidoMaterno",   expression = "java(getStringFromDatos(request.getDatos(), \"apellidoMaterno\"))")
    @Mapping(target = "fecha",             expression = "java(getStringFromDatos(request.getDatos(), \"fecha\"))")
    @Mapping(target = "sexo",              expression = "java(getStringFromDatos(request.getDatos(), \"sexo\"))")
    @Mapping(target = "estadoNacimiento",  expression = "java(getStringFromDatos(request.getDatos(), \"estadoNacimiento\"))")
    CaudexValidacionCurpRequestDto toValidacionCurpRequest(CanonicalRequest request);

    // ── Saldos crédito ───────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getStringFromDatos(request.getDatos(), \"numeroInstitucion\"))")
    @Mapping(target = "numeroCuenta",      expression = "java(getStringFromDatos(request.getDatos(), \"numeroCuenta\"))")
    CaudexConsultaSaldosCreditoRequestDto toConsultaSaldosCreditoRequest(CanonicalRequest request);

    // ── Movimientos crédito ──────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCuenta",      expression = "java(getLongFromDatos(request.getDatos(), \"numeroCuenta\"))")
    @Mapping(target = "fechaInicio",       expression = "java(getStringFromDatos(request.getDatos(), \"fechaInicio\"))")
    @Mapping(target = "fechaFin",          expression = "java(getStringFromDatos(request.getDatos(), \"fechaFin\"))")
    CaudexConsultaMovimientosCreditoRequestDto toConsultaMovimientosCreditoRequest(CanonicalRequest request);

    // ── Catálogo ─────────────────────────────────────────────────────────────

    @Mapping(target = "numeroInstitucion", expression = "java(getStringFromDatos(request.getDatos(), \"numeroInstitucion\"))")
    @Mapping(target = "claveCatalogo",     expression = "java(getIntFromDatos(request.getDatos(), \"claveCatalogo\", 1))")
    CaudexConsultaCatalogoRequestDto toConsultaCatalogoRequest(CanonicalRequest request);

    // ── Domicilio detalle (con consecutivo) ──────────────────────────────────

    @Mapping(target = "numeroInstitucion",    expression = "java(getIntFromDatos(request.getDatos(), \"numeroInstitucion\", 1))")
    @Mapping(target = "numeroCliente",        expression = "java(getLongFromDatos(request.getDatos(), \"numeroCliente\"))")
    @Mapping(target = "consecutivoDomicilio", expression = "java(getIntFromDatos(request.getDatos(), \"consecutivoDomicilio\", 1))")
    CaudexConsultaDomicilioDetalleRequestDto toConsultaDomicilioDetalleRequest(CanonicalRequest request);

    // ── Response → CanonicalResponse ────────────────────────────────────────

    @Mapping(target = "correlationId",   source = "correlationId")
    @Mapping(target = "codigoResultado", expression = "java(resolveCodigoResultado(response))")
    @Mapping(target = "mensaje",         expression = "java(resolveMensaje(response))")
    @Mapping(target = "httpStatus", source = "response.httpStatus")
    @Mapping(target = "proveedor",       expression = "java(com.asp.integration.shared.constants.ProviderConstants.CAUDEX)")
    @Mapping(target = "resultado",       expression = "java(resolveResultado(response))")
    @Mapping(target = "timestamp",       expression = "java(java.time.Instant.now())")
    CanonicalResponse toCanonicalResponse(CaudexResponseDto response, String correlationId);

    // ── Helpers de extracción de datos ──────────────────────────────────────

    default String getStringFromDatos(Map<String, Object> datos, String key) {
        if (datos == null) return null;
        Object value = datos.get(key);
        return value != null ? value.toString() : null;
    }

    default Integer getIntFromDatos(Map<String, Object> datos, String key, int defaultValue) {
        if (datos == null) return defaultValue;
        Object value = datos.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return defaultValue; }
    }

    default Long getLongFromDatos(Map<String, Object> datos, String key) {
        if (datos == null) return null;
        Object value = datos.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }

    default java.math.BigDecimal getBigDecimalFromDatos(Map<String, Object> datos, String key) {
        if (datos == null) return null;
        Object value = datos.get(key);
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal decimal) return decimal;
        if (value instanceof Number number) return java.math.BigDecimal.valueOf(number.doubleValue());
        try { return new java.math.BigDecimal(value.toString()); } catch (NumberFormatException e) { return null; }
    }

    default String resolveCodigoResultado(CaudexResponseDto response) {
        if (response.getHttpStatus() != null && response.getHttpStatus() >= 400) {
            return ResponseCodes.ERROR_PROVEEDOR;
        }
        return ResponseCodes.SUCCESS;
    }

    default String resolveMensaje(CaudexResponseDto response) {
        if (response.getMensaje() != null) return response.getMensaje();
        if (response.getNumCliente() != null) return ResponseMessages.CLIENTE_PROCESADO_PREFIX + response.getNumCliente();
        if (response.getNumeroCuenta() != null) return ResponseMessages.CUENTA_PROCESADA_PREFIX + response.getNumeroCuenta();
        return ResponseMessages.OPERACION_EXITOSA;
    }

    @SuppressWarnings("unchecked")
    default Object resolveResultado(CaudexResponseDto response) {
        if (response.getDatos() instanceof Map<?, ?> rawMap) {
            Map<String, Object> normalized = new java.util.LinkedHashMap<>();
            rawMap.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            if (response.getNumCliente() != null && !normalized.containsKey("numeroCliente")) {
                normalized.put("numeroCliente", response.getNumCliente());
            }
            if (response.getNumeroCuenta() != null && !normalized.containsKey("numeroCuenta")) {
                normalized.put("numeroCuenta", response.getNumeroCuenta());
            }
            return normalized;
        }

        if (response.getDatos() != null) {
            return response.getDatos();
        }

        if (response.getNumCliente() != null) {
            return Map.of("numeroCliente", response.getNumCliente());
        }

        if (response.getNumeroCuenta() != null) {
            return Map.of("numeroCuenta", response.getNumeroCuenta());
        }

        return null;
    }
}
