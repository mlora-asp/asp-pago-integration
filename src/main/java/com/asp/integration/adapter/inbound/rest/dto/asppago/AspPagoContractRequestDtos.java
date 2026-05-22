package com.asp.integration.adapter.inbound.rest.dto.asppago;

import com.asp.integration.adapter.inbound.rest.validation.asppago.ValidOnboardingRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTOs de contrato expuestos por el adapter inbound de ASP Pago.
 *
 * @autor: HJMB
 */
public final class AspPagoContractRequestDtos {

    private static final String ISO_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String ISO_DATETIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}(T|\\s)\\d{2}:\\d{2}(:\\d{2})?$";

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
    @ValidOnboardingRequest
    @Schema(name = "OnboardingRequest")
    public static class OnboardingRequest {

        @NotNull
        @Min(1)
        @Max(3)
        @Schema(description = "Tipo de persona. 1=PF, 2=PM, 3=PFAE", example = "1")
        private Integer numTipoPersona;

        @Valid
        private DatosBasicosPF datosBasicosPF;

        @Valid
        private DatosBasicosPM datosBasicosPM;

        @Valid
        private DatosComplementariosPF datosComplementariosPF;

        @Valid
        private DatosComplementariosPFAE datosComplementariosPFAE;

        @Valid
        private DatosComplementariosPM datosComplementariosPM;

        @NotNull
        @Valid
        private DatosDomicilio datosDomicilio;
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

    @Data
    @Schema(name = "Telefono")
    public static class TelefonoDto {

        @NotNull
        private Integer numeroTipoTelefono;

        @NotBlank
        @Size(max = 3)
        private String claveLada;

        @NotBlank
        @Size(max = 8)
        private String telefono;

        @Size(max = 5)
        private String extension;

        @NotNull
        private Integer numeroPais;
    }

    @Data
    @Schema(name = "DatosBasicosPF")
    public static class DatosBasicosPF {

        @NotNull
        private Integer numeroInstitucion;

        @NotNull
        @Min(1)
        @Max(3)
        private Integer numTipoPersona;

        @NotNull
        private Integer numSectorEconomico;

        @NotNull
        private Integer retieneISR;

        @NotNull
        private Integer retieneIDE;

        @NotNull
        private Integer pagaIVA;

        @Size(max = 4)
        private String claveCanalOperacion;

        private Integer numTitulo;

        @NotBlank
        @Size(max = 60)
        private String nombre1;

        @Size(max = 60)
        private String nombre2;

        @Size(max = 100)
        private String apaterno;

        @NotBlank
        @Size(max = 60)
        private String amaterno;

        @NotBlank
        @Size(max = 60)
        private String claveSexo;

        @NotNull
        private Integer numNacionalidad;

        @NotBlank
        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaNacConstitucion;

        @NotBlank
        @Size(max = 18)
        private String curp;

        @Size(max = 18)
        private String rfc;

        @NotNull
        private Integer clienteRestringido;

        @NotNull
        private Integer indSocio;

        @NotEmpty
        @Valid
        private List<TelefonoDto> listTelefonos;

        @Size(max = 100)
        private String correoElectronico;

        @NotBlank
        @Size(max = 10)
        private String idPromotor;

        @NotNull
        private Integer numSucursal;

        @NotNull
        private Integer paisNacimiento;

        @NotNull
        private Integer entidadNacimiento;

        @NotNull
        private Integer status;

        @NotBlank
        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaAltaCliente;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;

        private Long numeroGrupo;
        private Integer numeroCiclo;

        @Size(max = 200)
        private String parStrPaginaWeb;

        private List<String> huellas;
    }

    @Data
    @Schema(name = "DatosBasicosPM")
    public static class DatosBasicosPM {

        @NotNull
        private Integer numeroInstitucion;

        @NotNull
        private Integer numTipoPersona;

        @Size(max = 4)
        private String claveCanalOperacion;

        @NotNull
        private Integer numSectorEconomico;

        @NotNull
        private Integer retieneISR;

        @NotNull
        private Integer retieneIDE;

        @NotNull
        private Integer pagaIVA;

        @NotBlank
        @Size(max = 120)
        private String descRazonSocial;

        @NotNull
        private Integer numNacionalidad;

        @NotNull
        private Integer numTipoSociedad;

        @NotBlank
        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaNacConstitucion;

        @Size(max = 18)
        private String rfc;

        @NotNull
        private Integer clienteRestringido;

        @NotNull
        private Integer indSocio;

        @NotEmpty
        @Valid
        private List<TelefonoDto> listTelefonos;

        @Size(max = 100)
        private String correoElectronico;

        @NotBlank
        @Size(max = 10)
        private String idPromotor;

        @NotNull
        private Integer numSucursal;

        @NotNull
        private Integer paisNacimiento;

        @NotNull
        private Integer entidadNacimiento;

        @NotNull
        private Integer status;

        @NotBlank
        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaAltaCliente;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;

        private Long numeroGrupo;
        private Integer numeroCiclo;

        @Size(max = 200)
        private String parStrPaginaWeb;

        private List<String> huellas;
    }

    @Data
    @Schema(name = "DatosComplementariosPF")
    public static class DatosComplementariosPF {

        @NotNull
        private Integer numeroInstitucion;

        private Integer numEstadoCivil;
        private Integer numRegMtr;

        @Size(max = 100)
        private String nombreConyuge;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaMatrimonio;

        private Integer numDependientes;
        private Integer numHijos;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaNacHijoMayor;

        @Size(max = 10)
        private String claveActividadEconomica;

        private Integer numProfesion;
        private Integer numOcupacion;
        private Integer numTipoIdentif;

        @Size(max = 20)
        private String numIdentif;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaVenctoIdentif;

        @Size(max = 50)
        private String fiel;

        private Integer requiereFactura;
        private Integer coincideNombreCFDI;

        @Size(max = 150)
        private String nombreReceptorCFDI;

        @Size(max = 3)
        private String regimenFiscal;

        @Size(max = 4)
        private String claveUsoCFDI;

        @NotNull
        private Integer status;

        private Integer indEmpleado;

        @Size(max = 15)
        private String numeroEmpleado;

        @Size(max = 2)
        private String relacionadoArt73;

        private Integer exclusividadEmpresa;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;
    }

    @Data
    @Schema(name = "DatosComplementariosPFAE")
    public static class DatosComplementariosPFAE {

        @NotNull
        private Integer numeroInstitucion;

        private Integer numEstadoCivil;
        private Integer numRegMtr;

        @Size(max = 100)
        private String nombreConyuge;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaMatrimonio;

        private Integer numDependientes;
        private Integer numHijos;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaNacHijoMayor;

        @Size(max = 10)
        private String claveActividadEconomica;

        private Integer numTipoIdentif;

        @Size(max = 20)
        private String numIdentif;

        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaVenctoIdentif;

        @Size(max = 10)
        private String claveFira;

        @Size(max = 10)
        private String clvSCIAN;

        @Size(max = 5)
        private String paisFiscal;

        @Size(max = 100)
        private String numIdentFiscal;

        private Integer numeroOcupacion;
        private Integer numeroProfesion;

        @Size(max = 50)
        private String fiel;

        private Integer exclusividadEmpresa;
        private Integer requiereFactura;
        private Integer coincideNombreCFDI;

        @Size(max = 150)
        private String nombreReceptorCFDI;

        @Size(max = 3)
        private String regimenFiscal;

        @Size(max = 4)
        private String claveUsoCFDI;

        @NotNull
        private Integer status;

        @Size(max = 2)
        private String relacionadoArt73;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;
    }

    @Data
    @Schema(name = "DatosComplementariosPM")
    public static class DatosComplementariosPM {

        @NotNull
        private Integer numeroInstitucion;

        private Long numCliente;
        private Long numEscritura;

        @Size(max = 10)
        private String numActividadEconomica;

        private Integer numCobertura;
        private Integer numOficinas;
        private Integer numEmpleados;
        private Integer numClientes;
        private Integer numProveedores;

        @Size(max = 10)
        private String numVolumen;

        @Pattern(regexp = ISO_DATE_REGEX + "|" + ISO_DATETIME_REGEX)
        private String fechaInscripcion;

        @Size(max = 500)
        private String notario;

        @Size(max = 8)
        private String numLocalidad;

        @Size(max = 100)
        private String datosRegistro;

        @Size(max = 30)
        private String folioMercantil;

        @Size(max = 10)
        private String numActEcoCNBV;

        @Size(max = 10)
        private String numActEcoFIRA;

        @Size(max = 10)
        private String claveSCIAN;

        @Size(max = 5)
        private String paisFiscal;

        @Size(max = 100)
        private String numeroIdFiscal;

        private Integer indProveedor;
        private Integer tipoProveedor;
        private Integer indAcreditado;
        private Integer tipoAcreditado;
        private Integer tipoPersJuridica;
        private Integer metodologiaCalif;

        @Size(max = 3)
        private String claveCalifCartera;

        @Size(max = 2)
        private String relacionadoArt73;

        @Size(max = 50)
        private String fiel;

        private Integer numeroNotario;

        @Pattern(regexp = ISO_DATE_REGEX + "|" + ISO_DATETIME_REGEX)
        private String fechaEscritura;

        private Integer exclusividadEmpresa;
        private Integer requiereFactura;
        private Integer coincideNombreCFDI;

        @Size(max = 150)
        private String nombreReceptorCFDI;

        @Size(max = 3)
        private String regimenFiscal;

        @Size(max = 4)
        private String claveUsoCFDI;

        @NotNull
        private Integer status;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;
    }

    @Data
    @Schema(name = "DatosDomicilio")
    public static class DatosDomicilio {

        @NotNull
        private Integer numeroInstitucion;

        @NotNull
        private Integer numTipoDomicilio;

        @NotNull
        private Integer indDomicilioPrincipal;

        @NotBlank
        @Size(max = 60)
        private String descCalle;

        @NotBlank
        @Size(max = 20)
        private String numExterior;

        @Size(max = 6)
        private String numInterior;

        @Size(max = 60)
        private String descCalle1;

        @Size(max = 60)
        private String descCalle2;

        @Size(max = 120)
        private String descRefHubicacion;

        @NotNull
        private Integer codPostal;

        @Size(max = 5)
        private String codPostalFiscal;

        @NotNull
        private Integer numeroAsentamiento;

        @Size(max = 60)
        private String descOtroAsentamiento;

        @NotNull
        private Integer numTipoAsentamiento;

        @Size(max = 8)
        private String numLocalidadBanxico;

        private Integer numeroCiudad;
        private Integer numeroMunicipio;
        private Integer numeroEstado;
        private Integer numeroPais;

        @NotBlank
        @Pattern(regexp = ISO_DATE_REGEX)
        private String fechaRadicacion;

        @NotNull
        private Integer status;

        private Integer numConsecutivo;

        @NotBlank
        @Size(max = 10)
        private String idUsuarioAlta;
    }
}
