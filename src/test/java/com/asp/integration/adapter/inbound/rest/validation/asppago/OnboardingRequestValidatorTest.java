package com.asp.integration.adapter.inbound.rest.validation.asppago;

import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.DatosBasicosPF;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.DatosComplementariosPF;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.DatosDomicilio;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.OnboardingRequest;
import com.asp.integration.adapter.inbound.rest.dto.asppago.AspPagoContractRequestDtos.TelefonoDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del validador de onboarding.
 *
 * @autor: HJMB
 */
class OnboardingRequestValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldIgnoreConditionalCatalogRulesForPersonaFisica() {
        OnboardingRequest request = buildValidPersonaFisica();
        request.getDatosComplementariosPF().setNumEstadoCivil(2);
        request.getDatosComplementariosPF().setNumRegMtr(null);
        request.getDatosComplementariosPF().setNombreConyuge(null);
        request.getDatosComplementariosPF().setFechaMatrimonio(null);
        request.getDatosComplementariosPF().setNumHijos(2);
        request.getDatosComplementariosPF().setFechaNacHijoMayor(null);
        request.getDatosComplementariosPF().setRequiereFactura(1);
        request.getDatosComplementariosPF().setCoincideNombreCFDI(null);
        request.getDatosComplementariosPF().setNombreReceptorCFDI(null);
        request.getDatosComplementariosPF().setRegimenFiscal(null);
        request.getDatosComplementariosPF().setClaveUsoCFDI(null);

        Set<ConstraintViolation<OnboardingRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptValidPersonaFisicaRequest() {
        OnboardingRequest request = buildValidPersonaFisica();

        Set<ConstraintViolation<OnboardingRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    private OnboardingRequest buildValidPersonaFisica() {
        TelefonoDto telefono = new TelefonoDto();
        telefono.setNumeroTipoTelefono(1);
        telefono.setClaveLada("55");
        telefono.setTelefono("12345678");
        telefono.setNumeroPais(1);

        DatosBasicosPF basicosPF = new DatosBasicosPF();
        basicosPF.setNumeroInstitucion(1);
        basicosPF.setNumTipoPersona(1);
        basicosPF.setNumSectorEconomico(10);
        basicosPF.setRetieneISR(0);
        basicosPF.setRetieneIDE(0);
        basicosPF.setPagaIVA(1);
        basicosPF.setNombre1("MICHELL");
        basicosPF.setAmaterno("ARVIZO");
        basicosPF.setClaveSexo("H");
        basicosPF.setNumNacionalidad(1);
        basicosPF.setFechaNacConstitucion("1993-08-30");
        basicosPF.setCurp("LOAM930830HDFRRC11");
        basicosPF.setClienteRestringido(0);
        basicosPF.setIndSocio(0);
        basicosPF.setListTelefonos(List.of(telefono));
        basicosPF.setIdPromotor("USRCONFIG");
        basicosPF.setNumSucursal(1);
        basicosPF.setPaisNacimiento(1);
        basicosPF.setEntidadNacimiento(9);
        basicosPF.setStatus(1);
        basicosPF.setFechaAltaCliente("2025-10-12");
        basicosPF.setIdUsuarioAlta("USRCONFIG");

        DatosComplementariosPF complementariosPF = new DatosComplementariosPF();
        complementariosPF.setNumeroInstitucion(1);
        complementariosPF.setNumEstadoCivil(1);
        complementariosPF.setNumDependientes(2);
        complementariosPF.setNumHijos(0);
        complementariosPF.setStatus(1);
        complementariosPF.setRequiereFactura(0);
        complementariosPF.setIdUsuarioAlta("USRCONFIG");

        DatosDomicilio domicilio = new DatosDomicilio();
        domicilio.setNumeroInstitucion(1);
        domicilio.setNumTipoDomicilio(1);
        domicilio.setIndDomicilioPrincipal(1);
        domicilio.setDescCalle("AV REFORMA");
        domicilio.setNumExterior("123");
        domicilio.setCodPostal(11560);
        domicilio.setNumeroAsentamiento(2785);
        domicilio.setNumTipoAsentamiento(9);
        domicilio.setFechaRadicacion("2024-01-15");
        domicilio.setStatus(1);
        domicilio.setIdUsuarioAlta("USRCONFIG");

        OnboardingRequest request = new OnboardingRequest();
        request.setNumTipoPersona(1);
        request.setDatosBasicosPF(basicosPF);
        request.setDatosComplementariosPF(complementariosPF);
        request.setDatosDomicilio(domicilio);
        return request;
    }
}
