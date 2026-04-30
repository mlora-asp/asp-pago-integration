package com.asp.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.asp.integration.infrastructure.config.properties.CaudexProperties;
import com.asp.integration.infrastructure.config.properties.CryptoProperties;
import com.asp.integration.infrastructure.config.properties.IngressSecurityProperties;

/**
 * Punto de entrada del Bridge de Integración ASP.
 *
 * Anti-Corruption Layer entre los canales ASP Pago y Caudex.
 * La autenticación OAuth2 hacia Caudex es gestionada internamente.
 * El Bridge valida que las peticiones de negocio entren únicamente a través de Kong.
 *
 *
 * @autor: HJMB
 */
@SpringBootApplication
@EnableConfigurationProperties({
        CaudexProperties.class,
        IngressSecurityProperties.class,
        CryptoProperties.class
})
public class AspPagoIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AspPagoIntegrationApplication.class, args);
    }
}
