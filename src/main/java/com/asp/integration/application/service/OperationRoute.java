package com.asp.integration.application.service;

import com.asp.integration.domain.model.ProviderId;

/**
 * @autor: HJMB
 */
public record OperationRoute(String operationType, ProviderId providerId) {
    public OperationRoute {
        operationType = operationType.toUpperCase();
    }
}
