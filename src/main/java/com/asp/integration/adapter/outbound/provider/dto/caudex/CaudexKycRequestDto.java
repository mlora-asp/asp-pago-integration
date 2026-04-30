package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request de alta/modificación del perfil KYC en Caudex.
 * Endpoints:
 *   POST /api/conoce-cliente/alta
 *   PUT  /api/conoce-cliente/modificacion
 *   POST /api/conoce-cliente/consultaDatosPersona
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexKycRequestDto {

    private Integer numeroInstitucion;
    private Long numeroCliente;
    private Integer indPoliticamenteExpuesta;
    private String cargoPoliticamenteExpuesta;
    private String periodoPoliticamenteExpuesta;
    private Integer indRelPersonaPoliticamente;
    private String indPersonaPoliticamenteClienteInstitucion;
    private Long numeroClienteRelacionadoPersonaPoliticamente;
    private Integer numeroTipoRelacion;
    private Integer numeroRelacion;
    private Integer indRelacionadoSociedadMercantil;
    private String descripcionSociedadMercantil;
    private Integer indImportaciones;
    private BigDecimal montotMensualImportaciones;
    private Integer numeroPaisOrigen;
    private Integer indExportaciones;
    private BigDecimal montoMensualExportaciones;
    private Integer numeroPaisDestino;
    private Integer indAcreditadoOtraInstitucion;
    private Integer indResideExtranjero;
    private Integer origenIngresos;
    private String origenIngresosOtros;
    private Integer origenRecursos;
    private String nombreTercero;
    private String claveInstrumentoMonetario;
    private Integer numeroMonedaIngresos;
    private BigDecimal montoIngresosAnuales;
    private String indActuaporTerceros;
    private Integer claveOrigenCliente;
    private String idUsuario;
    private String macAddress;
    private String ClaveFuncion;
    private String nombrePrograma;
    private Integer statusCliente;
    private String fechaAlta;
}
