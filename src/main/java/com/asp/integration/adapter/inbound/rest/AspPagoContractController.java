package com.asp.integration.adapter.inbound.rest;

import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoBeneficiarioResponseDto;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.AltaBeneficiarioRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.EncryptedRequestDto;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.LoginRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.LogoutRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.OnboardingRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.PasswordOtpRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.PasswordResetRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoResponseDto;
import com.asp.integration.adapter.inbound.rest.mapper.asppago.AspPagoContractMapper;
import com.asp.integration.application.port.inbound.PayloadCryptoPort;
import com.asp.integration.domain.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "ASP Pago", description = "Contratos oficiales definidos en Nuevo ASP Pago")
public class AspPagoContractController {

    private final AspPagoContractMapper mapper;
    private final PayloadCryptoPort payloadCryptoPort;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping("/beneficiario/alta")
    @Operation(summary = "Alta completa de beneficiario (flujo transaccional)")
    public Mono<ResponseEntity<AspPagoBeneficiarioResponseDto>> altaBeneficiario(
            @Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, AltaBeneficiarioRequest.class);
        return Mono.just(ResponseEntity.ok(encryptData(mapper.dummyBeneficiario())));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Login móvil")
    public ResponseEntity<AspPagoResponseDto> login(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, LoginRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy("LOGIN_DUMMY", "Login realizado exitosamente")));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout")
    public ResponseEntity<AspPagoResponseDto> logout(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, LogoutRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy("LOGOUT_DUMMY", "Logout realizado exitosamente")));
    }

    @PostMapping("/auth/password/otp")
    @Operation(summary = "Solicitar OTP para restablecer contraseña")
    public ResponseEntity<AspPagoResponseDto> passwordOtp(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, PasswordOtpRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy("PASSWORD_OTP_DUMMY", "OTP generado exitosamente")));
    }

    @PostMapping("/auth/password/reset")
    @Operation(summary = "Validar OTP y restablecer contraseña")
    public ResponseEntity<AspPagoResponseDto> passwordReset(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, PasswordResetRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy("PASSWORD_RESET_DUMMY", "Password actualizado exitosamente")));
    }

    @PostMapping("/onboarding")
    @Operation(summary = "Onboarding completo")
    public ResponseEntity<AspPagoResponseDto> onboarding(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, OnboardingRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy("ONBOARDING_DUMMY", "Onboarding procesado exitosamente")));
    }

    private <T> T decryptAndValidate(EncryptedRequestDto encryptedRequest, Class<T> requestType) {
        String plainJson = payloadCryptoPort.decryptRequest(encryptedRequest.getData());
        try {
            T request = objectMapper.readValue(plainJson, requestType);
            validate(request);
            return request;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(
                    "ERROR_VALIDACION",
                    "El payload desencriptado no corresponde al contrato esperado",
                    ex);
        }
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }

        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        throw new BadRequestException("ERROR_VALIDACION", "Campos inválidos en la solicitud: " + message);
    }

    private AspPagoResponseDto encryptData(AspPagoResponseDto response) {
        response.setData(encryptDataValue(response.getData()));
        return response;
    }

    private AspPagoBeneficiarioResponseDto encryptData(AspPagoBeneficiarioResponseDto response) {
        response.setData(encryptDataValue(response.getData()));
        return response;
    }

    private Object encryptDataValue(Object data) {
        if (data == null) {
            return null;
        }

        try {
            String plainData = objectMapper.writeValueAsString(data);
            return payloadCryptoPort.encryptResponse(plainData);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("ERROR_ENCRIPTADO_RESPUESTA", "No fue posible serializar data de respuesta", ex);
        }
    }
}
