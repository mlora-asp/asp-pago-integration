package com.asp.integration.adapter.outbound.provider.mapper;

import com.asp.integration.adapter.outbound.provider.dto.caudex.*;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaudexMapperTest {

    private final CaudexMapper mapper = Mappers.getMapper(CaudexMapper.class);

    @Test
    void toConsultaClienteRequest_byNumero_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-001")
                .datos(Map.of("numeroInstitucion", 1, "numCliente", 103781))
                .build();

        CaudexConsultaClienteRequestDto result = mapper.toConsultaClienteRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumCliente()).isEqualTo(103781L);
        assertThat(result.getCurp()).isNull();
        assertThat(result.getRfc()).isNull();
    }

    @Test
    void toConsultaClienteRequest_byCurp_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-002")
                .datos(Map.of("numeroInstitucion", 1, "curp", "PEMJ900514HDFLRN21"))
                .build();

        CaudexConsultaClienteRequestDto result = mapper.toConsultaClienteRequest(request);

        assertThat(result.getCurp()).isEqualTo("PEMJ900514HDFLRN21");
        assertThat(result.getNumCliente()).isNull();
    }

    @Test
    void toConsultaCuentaVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-003")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 1105))
                .build();

        CaudexConsultaCuentaVistaRequestDto result = mapper.toConsultaCuentaVistaRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumeroCuenta()).isEqualTo(1105L);
    }

    @Test
    void toAltaCuentaVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-004")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "numCliente", "101296",
                        "numMoneda", "1",
                        "numProducto", "7003",
                        "regimenCuenta", "I",
                        "nivelCuenta", "1",
                        "idUsuario", "USRCONFIG",
                        "nip", "1234"
                ))
                .build();

        CaudexAltaCuentaVistaRequestDto result = mapper.toAltaCuentaVistaRequest(request);

        assertThat(result.getNumCliente()).isEqualTo("101296");
        assertThat(result.getNumMoneda()).isEqualTo("1");
        assertThat(result.getNumProducto()).isEqualTo("7003");
        assertThat(result.getRegimenCuenta()).isEqualTo("I");
        assertThat(result.getNip()).isEqualTo("1234");
    }

    @Test
    void toCanonicalResponse_success_mapsCorrectly() {
        CaudexResponseDto caudexResponse = CaudexResponseDto.builder()
                .numCliente(103781L)
                .httpStatus(200)
                .build();

        CanonicalResponse result = mapper.toCanonicalResponse(caudexResponse, "corr-005");

        assertThat(result.getCorrelationId()).isEqualTo("corr-005");
        assertThat(result.getCodigoResultado()).isEqualTo("SUCCESS");
        assertThat(result.getProveedor()).isEqualTo("CAUDEX");
        assertThat(result.getMensaje()).contains("103781");
    }

    @Test
    void toCanonicalResponse_httpError_mapsToErrorProvider() {
        CaudexResponseDto caudexResponse = CaudexResponseDto.builder()
                .httpStatus(500)
                .mensaje("Error interno Caudex")
                .build();

        CanonicalResponse result = mapper.toCanonicalResponse(caudexResponse, "corr-006");

        assertThat(result.getCodigoResultado()).isEqualTo("ERROR_PROVEEDOR");
        assertThat(result.getMensaje()).isEqualTo("Error interno Caudex");
    }

    @Test
    void toConsultaClienteRequest_nullDatos_returnsDefaultInstitucion() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-007")
                .datos(null)
                .build();

        CaudexConsultaClienteRequestDto result = mapper.toConsultaClienteRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1); // default
        assertThat(result.getNumCliente()).isNull();
    }

    // ── PRIORITY: consultaSaldosVista ─────────────────────────────────────────

    @Test
    void toConsultaSaldosVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-saldos")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 10))
                .build();

        CaudexConsultaSaldosVistaRequestDto result = mapper.toConsultaSaldosVistaRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumeroCuenta()).isEqualTo(10L);
    }

    @Test
    void toConsultaSaldosVistaRequest_defaultInstitucion_whenMissing() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-saldos-default")
                .datos(Map.of("numeroCuenta", 10))
                .build();

        CaudexConsultaSaldosVistaRequestDto result = mapper.toConsultaSaldosVistaRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumeroCuenta()).isEqualTo(10L);
    }

    // ── Depósito cuenta vista ─────────────────────────────────────────────────

    @Test
    void toDepositoCuentaVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-dep")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "numeroCuenta", 1105,
                        "referenciaNumerica", 987654321L,
                        "referenciaAlfanumerica", "alfanumerica",
                        "idUsuario", "USRCONFIG",
                        "sistemaOperacion", "test"
                ))
                .build();

        CaudexDepositoCuentaVistaRequestDto result = mapper.toDepositoCuentaVistaRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumeroCuenta()).isEqualTo(1105L);
        assertThat(result.getReferenciaNumerica()).isEqualTo(987654321L);
        assertThat(result.getReferenciaAlfanumerica()).isEqualTo("alfanumerica");
        assertThat(result.getIdUsuario()).isEqualTo("USRCONFIG");
        assertThat(result.getSistemaOperacion()).isEqualTo("test");
    }

    // ── Retiro cuenta vista ───────────────────────────────────────────────────

    @Test
    void toRetiroCuentaVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-retiro")
                .datos(Map.of(
                        "numeroInstitucion",    1,
                        "numeroCuenta",         1105,
                        "numeroCuentaEje",      1234,
                        "claveCanalOperacion",  1,
                        "idUsuario",            "USRCONFIG",
                        "sistemaOperacion",     "test",
                        "tipoCuentaEje",        1,
                        "fechaAplicacion",      "2026-02-26"
                ))
                .build();

        CaudexRetiroCuentaVistaRequestDto result = mapper.toRetiroCuentaVistaRequest(request);

        assertThat(result.getNumeroCuenta()).isEqualTo(1105L);
        assertThat(result.getNumeroCuentaEje()).isEqualTo(1234L);
        assertThat(result.getClaveCanalOperacion()).isEqualTo(1);
        assertThat(result.getFechaAplicacion()).isEqualTo("2026-02-26");
    }

    // ── Consulta histórico vista ──────────────────────────────────────────────

    @Test
    void toConsultaHistoricoVistaRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-hist")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "numeroCuenta", 10,
                        "fechaInicio", "2025-01-01",
                        "fechaFin", "2025-06-20"
                ))
                .build();

        CaudexConsultaHistoricoVistaRequestDto result = mapper.toConsultaHistoricoVistaRequest(request);

        assertThat(result.getNumeroCuenta()).isEqualTo(10L);
        assertThat(result.getFechaInicio()).isEqualTo("2025-01-01");
        assertThat(result.getFechaFin()).isEqualTo("2025-06-20");
    }

    // ── Buró de crédito ───────────────────────────────────────────────────────

    @Test
    void toConsultaBuroCreditoRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-buro")
                .datos(Map.of(
                        "numeroInstitucion", "1",
                        "numCliente", "101315",
                        "macAddress", "",
                        "fechaConsulta", "2026-02-23",
                        "idUsuario", 0
                ))
                .build();

        CaudexConsultaBuroCreditoRequestDto result = mapper.toConsultaBuroCreditoRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo("1");
        assertThat(result.getNumCliente()).isEqualTo("101315");
        assertThat(result.getFechaConsulta()).isEqualTo("2026-02-23");
        assertThat(result.getIdUsuario()).isEqualTo(0);
    }

    // ── Envío SMS ─────────────────────────────────────────────────────────────

    @Test
    void toEnvioSmsRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-sms")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "numeroCliente", 202345L,
                        "fechaMensaje", "11/01/2026",
                        "mensaje", "Mensaje de prueba",
                        "numeroTelefonico", 5527119309L
                ))
                .build();

        CaudexEnvioSmsRequestDto result = mapper.toEnvioSmsRequest(request);

        assertThat(result.getMensaje()).isEqualTo("Mensaje de prueba");
        assertThat(result.getNumeroTelefonico()).isEqualTo(5527119309L);
        assertThat(result.getFechaMensaje()).isEqualTo("11/01/2026");
    }

    // ── Envío correo ──────────────────────────────────────────────────────────

    @Test
    void toEnvioCorreoRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-email")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "fecha", "09/01/2026",
                        "encabezado", "Asunto del correo",
                        "cuerpo", "<p>Mensaje HTML</p>",
                        "correoReceptor", "destinatario@example.com"
                ))
                .build();

        CaudexEnvioCorreoRequestDto result = mapper.toEnvioCorreoRequest(request);

        assertThat(result.getEncabezado()).isEqualTo("Asunto del correo");
        assertThat(result.getCuerpo()).isEqualTo("<p>Mensaje HTML</p>");
        assertThat(result.getCorreoReceptor()).isEqualTo("destinatario@example.com");
        assertThat(result.getNumeroCliente()).isNull();  // opcional, no enviado
    }

    // ── Validación CURP ───────────────────────────────────────────────────────

    @Test
    void toValidacionCurpRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-curp")
                .datos(Map.of(
                        "nombre", "Juan",
                        "apellidoPaterno", "García",
                        "apellidoMaterno", "López",
                        "fecha", "1990-05-15",
                        "sexo", "H",
                        "estadoNacimiento", "CDMX"
                ))
                .build();

        CaudexValidacionCurpRequestDto result = mapper.toValidacionCurpRequest(request);

        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellidoPaterno()).isEqualTo("García");
        assertThat(result.getSexo()).isEqualTo("H");
        assertThat(result.getEstadoNacimiento()).isEqualTo("CDMX");
    }

    // ── Saldos crédito ────────────────────────────────────────────────────────

    @Test
    void toConsultaSaldosCreditoRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-saldo-cred")
                .datos(Map.of("numeroInstitucion", "1", "numeroCuenta", "100011"))
                .build();

        CaudexConsultaSaldosCreditoRequestDto result = mapper.toConsultaSaldosCreditoRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo("1");
        assertThat(result.getNumeroCuenta()).isEqualTo("100011");
    }

    // ── Movimientos crédito ───────────────────────────────────────────────────

    @Test
    void toConsultaMovimientosCreditoRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-mov-cred")
                .datos(Map.of(
                        "numeroInstitucion", 1,
                        "numeroCuenta", 100011,
                        "fechaInicio", "2025-01-01",
                        "fechaFin", "2025-12-31"
                ))
                .build();

        CaudexConsultaMovimientosCreditoRequestDto result = mapper.toConsultaMovimientosCreditoRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo(1);
        assertThat(result.getNumeroCuenta()).isEqualTo(100011L);
        assertThat(result.getFechaInicio()).isEqualTo("2025-01-01");
        assertThat(result.getFechaFin()).isEqualTo("2025-12-31");
    }

    // ── Catálogo ──────────────────────────────────────────────────────────────

    @Test
    void toConsultaCatalogoRequest_mapsCorrectly() {
        CanonicalRequest request = CanonicalRequest.builder()
                .correlationId("corr-cat")
                .datos(Map.of("numeroInstitucion", "1", "claveCatalogo", 1))
                .build();

        CaudexConsultaCatalogoRequestDto result = mapper.toConsultaCatalogoRequest(request);

        assertThat(result.getNumeroInstitucion()).isEqualTo("1");
        assertThat(result.getClaveCatalogo()).isEqualTo(1);
    }
}
