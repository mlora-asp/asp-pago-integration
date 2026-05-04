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
import com.asp.integration.shared.constants.ApiPaths;
import com.asp.integration.shared.constants.OpenApiTexts;
import com.asp.integration.shared.constants.OperationTypes;
import com.asp.integration.shared.constants.ResponseCodes;
import com.asp.integration.shared.constants.ResponseMessages;
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
@Tag(name = OpenApiTexts.TAG_ASP_PAGO, description = OpenApiTexts.TAG_ASP_PAGO_DESCRIPTION)
public class AspPagoContractController {

    private final AspPagoContractMapper mapper;
    private final PayloadCryptoPort payloadCryptoPort;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(ApiPaths.BENEFICIARIO_ALTA)
    @Operation(summary = OpenApiTexts.SUMMARY_BENEFICIARIO_ALTA)
    public Mono<ResponseEntity<AspPagoBeneficiarioResponseDto>> altaBeneficiario(
            @Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, AltaBeneficiarioRequest.class);
        return Mono.just(ResponseEntity.ok(encryptData(mapper.dummyBeneficiario())));
    }

    @PostMapping(ApiPaths.AUTH_LOGIN)
    @Operation(summary = OpenApiTexts.SUMMARY_LOGIN)
    public ResponseEntity<AspPagoResponseDto> login(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, LoginRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy(OperationTypes.LOGIN_DUMMY, ResponseMessages.LOGIN_EXITOSO)));
    }

    @PostMapping(ApiPaths.AUTH_LOGOUT)
    @Operation(summary = OpenApiTexts.SUMMARY_LOGOUT)
    public ResponseEntity<AspPagoResponseDto> logout(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, LogoutRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy(OperationTypes.LOGOUT_DUMMY, ResponseMessages.LOGOUT_EXITOSO)));
    }

    @PostMapping(ApiPaths.AUTH_PASSWORD_OTP)
    @Operation(summary = OpenApiTexts.SUMMARY_PASSWORD_OTP)
    public ResponseEntity<AspPagoResponseDto> passwordOtp(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, PasswordOtpRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy(OperationTypes.PASSWORD_OTP_DUMMY, ResponseMessages.OTP_GENERADO)));
    }

    @PostMapping(ApiPaths.AUTH_PASSWORD_RESET)
    @Operation(summary = OpenApiTexts.SUMMARY_PASSWORD_RESET)
    public ResponseEntity<AspPagoResponseDto> passwordReset(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, PasswordResetRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy(OperationTypes.PASSWORD_RESET_DUMMY, ResponseMessages.PASSWORD_ACTUALIZADO)));
    }

    @PostMapping(ApiPaths.ONBOARDING)
    @Operation(summary = OpenApiTexts.SUMMARY_ONBOARDING)
    public ResponseEntity<AspPagoResponseDto> onboarding(@Valid @RequestBody EncryptedRequestDto encryptedRequest) {
        decryptAndValidate(encryptedRequest, OnboardingRequest.class);
        return ResponseEntity.ok(encryptData(mapper.dummy(OperationTypes.ONBOARDING_DUMMY, ResponseMessages.ONBOARDING_EXITOSO)));
    }

    private <T> T decryptAndValidate(EncryptedRequestDto encryptedRequest, Class<T> requestType) {
        String plainJson = payloadCryptoPort.decryptRequest(encryptedRequest.getData());
        try {
            T request = objectMapper.readValue(plainJson, requestType);
            validate(request);
            return request;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException(
                    ResponseCodes.ERROR_VALIDACION,
                    ResponseMessages.PAYLOAD_CONTRATO_INVALIDO,
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
        throw new BadRequestException(ResponseCodes.ERROR_VALIDACION,
                ResponseMessages.CAMPOS_INVALIDOS_PREFIX + message);
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
            throw new BadRequestException(ResponseCodes.ERROR_ENCRIPTADO_RESPUESTA,
                    ResponseMessages.SERIALIZAR_DATA_RESPUESTA_ERROR, ex);
        }
    }
}
