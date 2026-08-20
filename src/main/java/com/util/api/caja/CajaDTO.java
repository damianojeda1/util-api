package com.util.api.caja;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CajaDTO {

    public int codigo;

    public Date fechaApertura;
    public Date fechaCierre;

    public double importeApertura;
    public double importeAperturaVirtual;

    public Double importeCierre;
    public Double importeCierreVirtual;

    public int idVendedor;
    public String nombreVendedor;

    public int estado;

    public String observacionApertura;
    public String observacionCierre;

    public List<MovimientoDTO> movimientos = new ArrayList<>();
    public List<ResumenPagoDTO> resumenPagos = new ArrayList<>();

    public TotalesDTO totales = new TotalesDTO();

    public CajaDTO() {
    }

    public static class AperturaRequest {

        public double importeApertura;
        public double importeAperturaVirtual;

        public int idVendedor;
        public String nombreVendedor;

        public String observacion;

        public AperturaRequest() {
        }
    }

    public static class CierreRequest {

        public int codigoCaja;

        public double importeCierre;
        public double importeCierreVirtual;

        public String observacionCierre;

        public boolean crearArqueo;
        public double importeArqueo;

        public CierreRequest() {
        }
    }

    public static class MovimientoRequest {

        public int codigoCaja;
        public double importe;
        public int tipoMovimiento;
        public String observacion;

        public MovimientoRequest() {
        }
    }

    public static class MovimientoDTO {

        public int codigo;
        public int tipoMovimiento;

        public String fecha;
        public String detalle;
        public String origenPago;
        public String observacion;

        public double importe;

        public List<PagoDTO> pagosFisicos = new ArrayList<>();
        public List<PagoDTO> pagosVirtuales = new ArrayList<>();

        public MovimientoDTO() {
        }
    }

    public static class PagoDTO {

        public String descripcion;
        public double importe;
        public boolean fisico;

        public PagoDTO() {
        }

        public PagoDTO(
                String descripcion,
                double importe,
                boolean fisico
        ) {
            this.descripcion = descripcion;
            this.importe = importe;
            this.fisico = fisico;
        }
    }

    public static class ResumenPagoDTO {

        public String grupo;
        public String detalle;

        public Double ingreso;
        public Double egreso;

        public ResumenPagoDTO() {
        }

        public ResumenPagoDTO(
                String grupo,
                String detalle,
                Double ingreso,
                Double egreso
        ) {
            this.grupo = grupo;
            this.detalle = detalle;
            this.ingreso = ingreso;
            this.egreso = egreso;
        }
    }

    public static class TotalesDTO {

        public double cajaInicial;

        public double ingresosManuales;
        public double egresosManuales;
        public double arqueos;

        public double pagosFisicos;
        public double pagosVirtuales;

        public double egresosFisicos;
        public double egresosVirtuales;

        public double totalFisico;
        public double totalVirtual;
        public double totalVentas;

        public TotalesDTO() {
        }
    }

    public static class ImpresionCajaDTO {

        public int codigo;

        public Date fechaApertura;
        public Date fechaCierre;

        public double importeApertura;
        public double importeAperturaVirtual;

        public double importeCierre;
        public double importeCierreVirtual;

        public String nombreVendedor;

        public String observacionApertura;
        public String observacionCierre;

        public int estado;

        public TotalesDTO totales = new TotalesDTO();

        public List<ResumenMedioPagoDTO> mediosPago =
                new ArrayList<>();

        public ImpresionCajaDTO() {
        }
    }

    public static class ResumenMedioPagoDTO {

        public String descripcion;
        public boolean fisico;

        public int tipoMovimiento;
        public double total;

        public ResumenMedioPagoDTO() {
        }
    }

    public static class CerrarAbiertasUsuarioRequest {

        public int codigoUsuario;

        public CerrarAbiertasUsuarioRequest() {
        }
    }

    public static class OperacionResponse {

        public boolean exitoso;
        public String mensaje;

        public OperacionResponse() {
        }

        public OperacionResponse(
                boolean exitoso,
                String mensaje
        ) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }
    }
}
