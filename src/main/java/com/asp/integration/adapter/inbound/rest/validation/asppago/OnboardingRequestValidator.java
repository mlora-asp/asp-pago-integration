package com.asp.integration.adapter.inbound.rest.validation.asppago;

import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.OnboardingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de estructura para onboarding.
 *
 * @autor: HJMB
 */
public class OnboardingRequestValidator implements ConstraintValidator<ValidOnboardingRequest, OnboardingRequest> {

    private static final int PERSONA_FISICA = 1;
    private static final int PERSONA_MORAL = 2;
    private static final int PERSONA_FISICA_ACTIVIDAD_EMPRESARIAL = 3;
    @Override
    public boolean isValid(OnboardingRequest value, ConstraintValidatorContext context) {
        if (value == null || value.getNumTipoPersona() == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        return switch (value.getNumTipoPersona()) {
            case PERSONA_FISICA -> validatePersonaFisica(value, context);
            case PERSONA_MORAL -> validatePersonaMoral(value, context);
            case PERSONA_FISICA_ACTIVIDAD_EMPRESARIAL -> validatePersonaFisicaActividadEmpresarial(value, context);
            default -> addViolation(context, "numTipoPersona debe ser 1 (PF), 2 (PM) o 3 (PFAE)", "numTipoPersona");
        };
    }

    private boolean validatePersonaFisica(OnboardingRequest value, ConstraintValidatorContext context) {
        boolean valid = true;
        valid &= require(value.getDatosBasicosPF() != null, context,
                "datosBasicosPF es obligatorio para Persona Física", "datosBasicosPF");
        valid &= require(value.getDatosComplementariosPF() != null, context,
                "datosComplementariosPF es obligatorio para Persona Física", "datosComplementariosPF");
        valid &= forbid(value.getDatosBasicosPM() == null, context,
                "datosBasicosPM no aplica para Persona Física", "datosBasicosPM");
        valid &= forbid(value.getDatosComplementariosPFAE() == null, context,
                "datosComplementariosPFAE no aplica para Persona Física", "datosComplementariosPFAE");
        valid &= forbid(value.getDatosComplementariosPM() == null, context,
                "datosComplementariosPM no aplica para Persona Física", "datosComplementariosPM");
        valid &= require(value.getDatosBasicosPF() == null
                        || value.getDatosBasicosPF().getNumTipoPersona() == null
                        || PERSONA_FISICA == value.getDatosBasicosPF().getNumTipoPersona(),
                context,
                "datosBasicosPF.numTipoPersona debe ser 1 para Persona Física",
                "datosBasicosPF.numTipoPersona");
        return valid;
    }

    private boolean validatePersonaMoral(OnboardingRequest value, ConstraintValidatorContext context) {
        boolean valid = true;
        valid &= require(value.getDatosBasicosPM() != null, context,
                "datosBasicosPM es obligatorio para Persona Moral", "datosBasicosPM");
        valid &= require(value.getDatosComplementariosPM() != null, context,
                "datosComplementariosPM es obligatorio para Persona Moral", "datosComplementariosPM");
        valid &= forbid(value.getDatosBasicosPF() == null, context,
                "datosBasicosPF no aplica para Persona Moral", "datosBasicosPF");
        valid &= forbid(value.getDatosComplementariosPF() == null, context,
                "datosComplementariosPF no aplica para Persona Moral", "datosComplementariosPF");
        valid &= forbid(value.getDatosComplementariosPFAE() == null, context,
                "datosComplementariosPFAE no aplica para Persona Moral", "datosComplementariosPFAE");
        valid &= require(value.getDatosBasicosPM() == null
                        || value.getDatosBasicosPM().getNumTipoPersona() == null
                        || PERSONA_MORAL == value.getDatosBasicosPM().getNumTipoPersona(),
                context,
                "datosBasicosPM.numTipoPersona debe ser 2 para Persona Moral",
                "datosBasicosPM.numTipoPersona");
        return valid;
    }

    private boolean validatePersonaFisicaActividadEmpresarial(OnboardingRequest value,
                                                               ConstraintValidatorContext context) {
        boolean valid = true;
        valid &= require(value.getDatosBasicosPF() != null, context,
                "datosBasicosPF es obligatorio para Persona Física con Actividad Empresarial", "datosBasicosPF");
        valid &= require(value.getDatosComplementariosPFAE() != null, context,
                "datosComplementariosPFAE es obligatorio para Persona Física con Actividad Empresarial",
                "datosComplementariosPFAE");
        valid &= forbid(value.getDatosBasicosPM() == null, context,
                "datosBasicosPM no aplica para Persona Física con Actividad Empresarial", "datosBasicosPM");
        valid &= forbid(value.getDatosComplementariosPF() == null, context,
                "datosComplementariosPF no aplica para Persona Física con Actividad Empresarial",
                "datosComplementariosPF");
        valid &= forbid(value.getDatosComplementariosPM() == null, context,
                "datosComplementariosPM no aplica para Persona Física con Actividad Empresarial",
                "datosComplementariosPM");
        valid &= require(value.getDatosBasicosPF() == null
                        || value.getDatosBasicosPF().getNumTipoPersona() == null
                        || PERSONA_FISICA_ACTIVIDAD_EMPRESARIAL == value.getDatosBasicosPF().getNumTipoPersona(),
                context,
                "datosBasicosPF.numTipoPersona debe ser 3 para Persona Física con Actividad Empresarial",
                "datosBasicosPF.numTipoPersona");
        return valid;
    }

    private boolean require(boolean condition, ConstraintValidatorContext context, String message, String property) {
        return condition || addViolation(context, message, property);
    }

    private boolean forbid(boolean condition, ConstraintValidatorContext context, String message, String property) {
        return condition || addViolation(context, message, property);
    }

    private boolean addViolation(ConstraintValidatorContext context, String message, String property) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
        return false;
    }
}
