package com.asp.integration.adapter.inbound.rest.validation.asppago;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida la estructura general del onboarding.
 *
 * @autor: HJMB
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnboardingRequestValidator.class)
public @interface ValidOnboardingRequest {

    String message() default "La combinación de bloques del onboarding no corresponde al tipo de persona";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
