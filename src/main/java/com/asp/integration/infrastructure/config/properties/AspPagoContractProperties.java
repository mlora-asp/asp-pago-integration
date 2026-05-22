package com.asp.integration.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Propiedades del contrato ASP Pago.
 *
 * @autor: HJMB
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "contracts.asp-pago")
public class AspPagoContractProperties {

    @Positive
    private int successCode;

    @NotBlank
    private String defaultCurrency;

    @NotBlank
    private String defaultChannel;

    @NotBlank
    private String onboardingReferencePrefix;

    @Positive
    private int defaultChannelCode;

    @Positive
    private int defaultInstrumentCode;

    @Positive
    private int defaultAccountTypeCode;

    @NotEmpty
    private Map<String, Integer> channelCodes;

    @NotEmpty
    private Map<String, Integer> instrumentCodes;
}
