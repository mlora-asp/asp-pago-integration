package com.asp.integration.application.service;

import com.asp.integration.adapter.outbound.provider.CaudexClient;
import com.asp.integration.domain.exception.ProviderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderResolverTest {

    private ProviderResolver resolver;

    @BeforeEach
    void setUp() {
        CaudexClient caudex = mock(CaudexClient.class);

        when(caudex.providerName()).thenReturn("CAUDEX");

        resolver = new ProviderResolver(List.of(caudex));
    }

    @Test
    void deberiaResolverCaudex() {
        var client = resolver.resolve("CAUDEX");
        assertThat(client).isNotNull();
        assertThat(client.providerName()).isEqualTo("CAUDEX");
    }

    @Test
    void deberiLanzarExcepcionParaProveedorDesconocido() {
        assertThatThrownBy(() -> resolver.resolve("INEXISTENTE"))
                .isInstanceOf(ProviderNotFoundException.class)
                .hasMessageContaining("INEXISTENTE");
    }

    @Test
    void deberiaResolverIgnorandoMayusculas() {
        var client = resolver.resolve("caudex");
        assertThat(client.providerName()).isEqualTo("CAUDEX");
    }
}
