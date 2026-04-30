package com.asp.integration.adapter.inbound.rest.dto.asppago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

public final class AspPagoContractRequestDtos {

    private AspPagoContractRequestDtos() {
    }

    @Data
    @Schema(name = "EncryptedRequest")
    public static class EncryptedRequestDto {
        @NotBlank
        private String data;
    }

    @Data
    @Schema(name = "LoginRequest")
    public static class LoginRequest {
        @NotBlank
        private String usuario;

        @NotBlank
        private String password;
    }

    @Data
    @Schema(name = "LogoutRequest")
    public static class LogoutRequest {
        @NotBlank
        private String usuario;

        private String token;
    }

    @Data
    @Schema(name = "PasswordOtpRequest")
    public static class PasswordOtpRequest {
        @NotBlank
        @Size(min = 5, max = 50)
        private String usuario;

        @NotBlank
        @Pattern(regexp = "^MOVIL$")
        private String canal;

        @NotBlank
        @Pattern(regexp = "^(SMS|EMAIL|WHATSAPP)$")
        private String tipoEnvio;

        @NotNull
        private Map<String, Object> dispositivo;
    }

    @Data
    @Schema(name = "PasswordResetRequest")
    public static class PasswordResetRequest {
        @NotBlank
        private String usuario;

        @NotBlank
        @Pattern(regexp = "^[0-9]{4,8}$")
        private String otp;

        @NotBlank
        @Size(min = 8, max = 64)
        private String nuevaPassword;

        @NotBlank
        private String confirmarPassword;

        @NotNull
        private Map<String, Object> dispositivo;
    }

    @Data
    @Schema(name = "OnboardingRequest")
    public static class OnboardingRequest {
        @NotNull
        private Map<String, Object> metadata;

        @NotNull
        private Map<String, Object> cliente;

        @NotNull
        private Map<String, Object> cuenta;

        private Map<String, Object> perfilTransaccional;
    }

    @Data
    @Schema(name = "AltaBeneficiarioRequest")
    public static class AltaBeneficiarioRequest {
        @NotNull
        private Map<String, Object> meta;

        @NotNull
        private Map<String, Object> cliente;

        @NotNull
        private Map<String, Object> beneficiarioRelacion;
    }
}
