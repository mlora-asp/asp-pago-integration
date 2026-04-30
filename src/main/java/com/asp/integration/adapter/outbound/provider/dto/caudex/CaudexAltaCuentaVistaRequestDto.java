package com.asp.integration.adapter.outbound.provider.dto.caudex;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request de alta de cuenta vista (depósito a la vista) en Caudex.
 * Endpoint: POST /api/depositos-vista/altaCuenta
 * Este es el endpoint principal de la colección ASP_DEPOSITOS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaudexAltaCuentaVistaRequestDto {

    private Integer numeroInstitucion;
    private String numCliente;
    private String numMoneda;
    private String numProducto;
    private String retieneImptoInteres;
    private String numImptoInteres;
    private String cobraComisiones;
    private String planComisiones;
    private String numSucursal;
    private String idPromotor;
    private String fechaApertura;
    private String indCuentaRestringida;
    private String intPagaInteres;
    private String intPeriodicidadPago;
    private String intDiaPago1;
    private String fechaProximoPagoInt;
    private String intPagoInhabil;
    private String intBaseCalculo;
    private String sdoMinCalculoInt;
    private String sdoMaxCalculoInt;
    private String intTipoTasa;
    private String intTasa;
    private String intNumMatrizTasa;
    private String indPlanRetiros;
    private Integer numPlanRetiros;
    private String fechaUltimaActualizacion;
    private String fechaProximoCobro;
    private String regimenCuenta;
    private String nivelCuenta;
    private String idUsuario;
    private String nip;
}
