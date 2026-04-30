package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request de alta de datos básicos para Persona Física en Caudex.
 * Endpoint: POST /api/clientes/datosBasicosPF/alta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexAltaClientePFRequestDto {

    private Integer numeroInstitucion;
    private Integer numTipoPersona;
    private Integer numSectorEconomico;
    private Integer retieneISR;
    private Integer retieneIDE;
    private Integer pagaIVA;
    private Integer numTitulo;
    private String nombre1;
    private String nombre2;
    private String apaterno;
    private String amaterno;
    private String claveSexo;
    private Integer numNacionalidad;
    private String fechaNacConstitucion;
    private String curp;
    private String rfc;
    private Integer clienteRestringido;
    private Integer indSocio;
    private List<TelefonoDto> listTelefonos;
    private String correoElectronico;
    private String idPromotor;
    private Integer numSucursal;
    private Integer paisNacimiento;
    private Integer entidadNacimiento;
    private Integer status;
    private String fechaAltaCliente;
    private String idUsuarioAlta;
    private String macAddressAlta;
    private Integer numeroGrupo;
    private Integer numeroCiclo;
    private String parStrPaginaWeb;
    private List<String> huellas;
    private String claveFuncion;
    private String nombrePrograma;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TelefonoDto {
        private Integer numeroTipoTelefono;
        private String claveLada;
        private String telefono;
        private String extension;
        private Integer numeroPais;
    }
}
