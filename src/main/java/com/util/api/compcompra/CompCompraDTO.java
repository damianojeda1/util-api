package com.util.api.compcompra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CompCompraDTO {

    private CompCompraDTO() {
    }

    public static class ExisteResponse {

        public boolean existe;

        public ExisteResponse() {
        }

        public ExisteResponse(boolean existe) {
            this.existe = existe;
        }
    }

    public static class InsertResponse {

        public int id;
        public boolean ok;
        public String mensaje;

        public InsertResponse() {
        }

        public InsertResponse(
                int id,
                boolean ok,
                String mensaje
        ) {
            this.id = id;
            this.ok = ok;
            this.mensaje = mensaje;
        }
    }

    public static class OperacionResponse {

        public boolean ok;
        public String mensaje;

        public OperacionResponse() {
        }

        public OperacionResponse(
                boolean ok,
                String mensaje
        ) {
            this.ok = ok;
            this.mensaje = mensaje;
        }
    }

    public static class ComprobanteRequest {

        public int codigoProveedor;
        public int codigoTipoComprobante;

        public Date fechaComprobante;
        public Date periodoIVA;

        public int sistemaComprobante;

        public String letra;
        public int centro;
        public int numero;

        public double netoGravado;
        public double totalIVA;

        public double conceptosNoGravados;
        public double exentos;

        public double percepcionIIBB;
        public double percepcionIVA;
        public double percepcionImpuestosInternos;
        public double percepcionMunicipal;
        public double percepcionOtros;

        public double retencionIVA;
        public double retencionGanancias;

        public double total;

        public Date fechaCarga;
        public int codigoUsuario;
        public String observacion;

        public List<ItemRequest> items =
                new ArrayList<>();

        public ComprobanteRequest() {
        }
    }

    public static class ItemRequest {

        public int numeroRenglon;
        public int codigoTipoComprobante;

        public String codigoArticulo;
        public int codigoProveedorArticulo;

        public String descripcion;

        public double cantidad;
        public double precioBase;

        public double porcentajeBonificacion;
        public double precioBonificado;

        public double alicuotaIVA;
        public double totalFinal;

        public ItemRequest() {
        }
    }

    public static class PagoRequest {

        public int codigoComprobante;
        public int codigoTipoComprobante;

        public int codigoCaja;

        public int codigoProveedor;
        public String nombreProveedor;

        public int codigoUsuario;
        public String nombreUsuario;

        public double total;
        public String observacion;

        public List<MedioPagoRequest> pagos =
                new ArrayList<>();

        public PagoRequest() {
        }
    }

    public static class MedioPagoRequest {

        public int idMedioPago;
        public int tipoLegacy;

        public double importe;

        public String descripcion;
        public String referencia;

        public MedioPagoRequest() {
        }
    }

    public static class PagoDTO {

        public int codigo;
        public Date fecha;

        public int tipo;
        public double total;

        public int idVendedor;
        public String nombreVendedor;

        public int idProveedor;
        public String nombreProveedor;

        public int idMovimientoCaja;
        public int estado;

        public String observacion;

        public PagoDTO() {
        }
    }

    public static class MovimientoCuentaCorrienteDTO {

        public int origen;
        public int codigoTipo;
        public int codigo;
        public int idMovimientoCaja;

        public Date fecha;
        public String descripcion;

        public double debe;
        public double haber;
        public double saldo;

        public MovimientoCuentaCorrienteDTO() {
        }
    }

    public static class CuentaCorrienteDTO {

        public List<MovimientoCuentaCorrienteDTO> movimientos =
                new ArrayList<>();

        public double totalDebe;
        public double totalHaber;
        public double saldo;

        public CuentaCorrienteDTO() {
        }
    }

    public static class ComprobanteDTO {

        public int id;

        public int codigoProveedor;
        public String nombreProveedor;

        public int codigoTipoComprobante;

        public Date fechaComprobante;
        public Date periodoIVA;

        public int sistemaComprobante;

        public String letra;
        public String centro;
        public String numero;

        public double netoGravado;
        public double conceptosNoGravados;
        public double exentos;

        public double percepcionIIBB;
        public double percepcionIVA;
        public double percepcionImpuestosInternos;
        public double percepcionMunicipal;
        public double percepcionOtros;

        public double retencionIVA;
        public double retencionGanancias;

        public double total;

        public Date fechaRegistracion;
        public int codigoUsuario;

        public String observacion;

        public List<RenglonDTO> renglones =
                new ArrayList<>();

        public ComprobanteDTO() {
        }
    }

    public static class RenglonDTO {

        public int idComprobante;
        public int numeroRenglon;

        public int codigoTipoComprobante;

        public String codigoArticulo;
        public int codigoProveedorArticulo;

        public String descripcion;

        public double cantidad;
        public double precioBase;
        public double porcentajeBonificacion;
        public double precioBonificado;
        public double tasaIVA;
        public double totalFinal;

        public RenglonDTO() {
        }
    }

    public static class LibroIvaCompraDTO {

        private String fecha;
        private String descripcion;
        private String cuit;
        private String proveedor;

        private double neto;
        private double iva105;
        private double iva21;
        private double conceptosNoGravados;
        private double exentos;
        private double percepcionIibb;
        private double percepcionIva;
        private double percepcionInternos;
        private double percepcionMunicipales;
        private double otrosImpuestos;
        private double retencionIva;
        private double retencionGanancias;
        private double total;

        public LibroIvaCompraDTO() {
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getCuit() {
            return cuit;
        }

        public void setCuit(String cuit) {
            this.cuit = cuit;
        }

        public String getProveedor() {
            return proveedor;
        }

        public void setProveedor(String proveedor) {
            this.proveedor = proveedor;
        }

        public double getNeto() {
            return neto;
        }

        public void setNeto(double neto) {
            this.neto = neto;
        }

        public double getIva105() {
            return iva105;
        }

        public void setIva105(double iva105) {
            this.iva105 = iva105;
        }

        public double getIva21() {
            return iva21;
        }

        public void setIva21(double iva21) {
            this.iva21 = iva21;
        }

        public double getConceptosNoGravados() {
            return conceptosNoGravados;
        }

        public void setConceptosNoGravados(
                double conceptosNoGravados
        ) {
            this.conceptosNoGravados =
                    conceptosNoGravados;
        }

        public double getExentos() {
            return exentos;
        }

        public void setExentos(double exentos) {
            this.exentos = exentos;
        }

        public double getPercepcionIibb() {
            return percepcionIibb;
        }

        public void setPercepcionIibb(
                double percepcionIibb
        ) {
            this.percepcionIibb = percepcionIibb;
        }

        public double getPercepcionIva() {
            return percepcionIva;
        }

        public void setPercepcionIva(
                double percepcionIva
        ) {
            this.percepcionIva = percepcionIva;
        }

        public double getPercepcionInternos() {
            return percepcionInternos;
        }

        public void setPercepcionInternos(
                double percepcionInternos
        ) {
            this.percepcionInternos =
                    percepcionInternos;
        }

        public double getPercepcionMunicipales() {
            return percepcionMunicipales;
        }

        public void setPercepcionMunicipales(
                double percepcionMunicipales
        ) {
            this.percepcionMunicipales =
                    percepcionMunicipales;
        }

        public double getOtrosImpuestos() {
            return otrosImpuestos;
        }

        public void setOtrosImpuestos(
                double otrosImpuestos
        ) {
            this.otrosImpuestos = otrosImpuestos;
        }

        public double getRetencionIva() {
            return retencionIva;
        }

        public void setRetencionIva(
                double retencionIva
        ) {
            this.retencionIva = retencionIva;
        }

        public double getRetencionGanancias() {
            return retencionGanancias;
        }

        public void setRetencionGanancias(
                double retencionGanancias
        ) {
            this.retencionGanancias =
                    retencionGanancias;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}