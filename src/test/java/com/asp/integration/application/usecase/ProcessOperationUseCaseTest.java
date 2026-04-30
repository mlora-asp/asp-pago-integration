package com.asp.integration.application.usecase;

import com.asp.integration.application.command.ProcessOperationCommand;
import com.asp.integration.application.service.InboundAdapterService;
import com.asp.integration.application.service.ProviderResolver;
import com.asp.integration.infrastructure.audit.AuditLogger;
import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;
import com.asp.integration.adapter.inbound.rest.mapper.CanonicalMapper;
import com.asp.integration.domain.model.canonical.CanonicalRequest;
import com.asp.integration.domain.model.canonical.CanonicalResponse;
import com.asp.integration.application.port.outbound.ProviderGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOperationUseCaseTest {

    @Mock private CanonicalMapper canonicalMapper;
    @Mock private InboundAdapterService inboundAdapter;
    @Mock private ProviderResolver providerResolver;
    @Mock private AuditLogger auditLogger;
    @Mock private ProviderGateway providerClient;

    @InjectMocks private ProcessOperationUseCase processOperationUseCase;

    private OperacionRequestDto request;
    private CanonicalRequest canonical;

    @BeforeEach
    void setUp() {
        request = OperacionRequestDto.builder()
                .tipoOperacion("CAUDEX_CONSULTA_SALDOS_VISTA")
                .referencia("REF-001")
                .canal("API")
                .datos(Map.of("numeroInstitucion", 1, "numeroCuenta", 1105))
                .build();

        canonical = CanonicalRequest.builder()
                .operationType("CAUDEX_CONSULTA_SALDOS_VISTA")
                .referencia("REF-001")
                .targetProvider("CAUDEX")
                .build();
    }

    @Test
    void deberiaEjecutarOperacionExitosamente() {
        CanonicalResponse expectedResponse = CanonicalResponse.builder()
                .codigoResultado("SUCCESS")
                .proveedor("CAUDEX")
                .mensaje("Consulta generada satisfactoriamente")
                .build();

        when(canonicalMapper.toCanonical(request)).thenReturn(canonical);
        doNothing().when(inboundAdapter).enriquecer(any());
        when(providerResolver.resolve("CAUDEX")).thenReturn(providerClient);
        when(providerClient.execute(any())).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(processOperationUseCase.process(
                        new ProcessOperationCommand(
                                request,
                                "kong-jwt-gateway",
                                "corr-123",
                                "asp-pagos-auth-service",
                                "user-123",
                                "pagos:consulta")))
                .assertNext(response -> {
                    assertThat(response.getCodigoResultado()).isEqualTo("SUCCESS");
                    assertThat(response.getProveedor()).isEqualTo("CAUDEX");
                })
                .verifyComplete();

        verify(auditLogger).logSuccess(any(), any());
    }

    @Test
    void deberiaRegistrarErrorCuandoProveedorFalla() {
        when(canonicalMapper.toCanonical(request)).thenReturn(canonical);
        doNothing().when(inboundAdapter).enriquecer(any());
        when(providerResolver.resolve("CAUDEX")).thenReturn(providerClient);
        when(providerClient.execute(any())).thenReturn(Mono.error(new RuntimeException("Timeout")));

        StepVerifier.create(processOperationUseCase.process(
                        new ProcessOperationCommand(
                                request,
                                "kong-jwt-gateway",
                                "corr-456",
                                "asp-pagos-auth-service",
                                "user-456",
                                "pagos:consulta")))
                .expectError(RuntimeException.class)
                .verify();

        verify(auditLogger).logError(any(), any());
    }
}
