package com.asp.integration.infrastructure.security;

/**
 * Contexto autenticado propagado por Kong hacia el Bridge.
 *
 * @autor: HJMB
 */
public record GatewayRequestContext(
        String authenticatedClient,
        String authenticatedUser,
        String authenticatedScopes,
        String systemOrigin
) {}
