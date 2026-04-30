package com.asp.integration.application.command;

import com.asp.integration.adapter.inbound.rest.dto.OperacionRequestDto;

/**
 * Command de entrada del caso de uso.
 *
 * Mantiene fuera de la capa de aplicación los detalles HTTP como headers,
 * pero conserva la identidad autenticada que Kong y el BFF ya resolvieron.
 *
 *
 * @autor: HJMB
 */
public record ProcessOperationCommand(
        OperacionRequestDto request,
        String sistemaOrigen,
        String correlationId,
        String authenticatedClient,
        String authenticatedUser,
        String authenticatedScopes
) {}
