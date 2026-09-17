package com.util.api.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class TicketDTO {

    private TicketDTO() {
    }

    // -------------------------------------------------------------------------
    // VENTA COMPLETA
    // -------------------------------------------------------------------------

    public static class GuardarVentaRequest {

        public GuardarTicketRequest ticket;
        public GuardarReciboRequest recibo;
        public boolean acopio;
    }

    public static class GuardarVentaResponse {

        public boolean ok;

        public int codigoTicket;
        public int codigoRecibo;
        public int codigoMovimientoCaja;

        public String mensaje;

        public static GuardarVentaResponse ok(
                int codigoTicket,
                int codigoRecibo,
                int codigoMovimientoCaja
        ) {
            GuardarVentaResponse response =
                    new GuardarVentaResponse();

            response.ok = true;
            response.codigoTicket = codigoTicket;
            response.codigoRecibo = codigoRecibo;
            response.codigoMovimientoCaja =
                    codigoMovimientoCaja;

            return response;
        }

        public static GuardarVentaResponse error(
                String mensaje
        ) {
            GuardarVentaResponse response =
                    new GuardarVentaResponse();

            response.ok = false;
            response.codigoTicket = -1;
            response.codigoRecibo = -1;
            response.codigoMovimientoCaja = -1;
            response.mensaje = mensaje;

            return response;
        }
    }

    // -------------------------------------------------------------------------
    // TICKET
    // -------------------------------------------------------------------------

    public static class GuardarTicketRequest {

        public int tipo;

        public int vendedor;
        public String nombreVendedor;

        public int cliente;
        public String nombreCliente;

        public double total;
        public String observacion;

        public boolean acopio;

        public List<ItemRequest> items = new ArrayList<>();
    }

    public static class ItemRequest {

        public int proveedor;
        public String codigoArticulo;
        public String descripcion;

        public double cantidad;
        public double costo;

        public double margen;
        public double margenPropio;

        public double neto;
        public double alicuota;
        public double importeIVA;
        public double precio;
        public double bonificacion;
    }

    public static class GuardarTicketResponse {

        public boolean ok;
        public int codigo;
        public String mensaje;

        public static GuardarTicketResponse ok(
                int codigo
        ) {

            GuardarTicketResponse response =
                    new GuardarTicketResponse();

            response.ok = true;
            response.codigo = codigo;

            return response;
        }

        public static GuardarTicketResponse error(
                String mensaje
        ) {

            GuardarTicketResponse response =
                    new GuardarTicketResponse();

            response.ok = false;
            response.codigo = -1;
            response.mensaje = mensaje;

            return response;
        }
    }

    public static class ResumenTicketDTO {

        public int codigo;
        public int tipo;
        public Date fecha;

        public int idCliente;
        public String nombreCliente;

        public double total;

        public String comprobanteFiscal;
        public Double netoComprobante;
        public Double ivaComprobante;
        public Double totalComprobante;
    }

    // -------------------------------------------------------------------------
    // RECIBO
    // -------------------------------------------------------------------------

    public static class GuardarReciboRequest {

        public int tipo;
        public int caja;

        public int vendedor;
        public String nombreVendedor;

        public int cliente;
        public String nombreCliente;

        public double total;
        public String observacion;

        public List<PagoRequest> pagos =
                new ArrayList<>();
    }

    public static class PagoRequest {

        public int tipoLegacy;
        public Integer idMedioPago;
        public double importe;
    }

    public static class GuardarReciboResponse {

        public boolean ok;
        public int codigo;
        public int codigoMovimientoCaja;
        public String mensaje;

        public static GuardarReciboResponse ok(
                int codigo,
                int codigoMovimientoCaja
        ) {

            GuardarReciboResponse response =
                    new GuardarReciboResponse();

            response.ok = true;
            response.codigo = codigo;
            response.codigoMovimientoCaja =
                    codigoMovimientoCaja;

            return response;
        }

        public static GuardarReciboResponse error(
                String mensaje
        ) {

            GuardarReciboResponse response =
                    new GuardarReciboResponse();

            response.ok = false;
            response.codigo = -1;
            response.codigoMovimientoCaja = -1;
            response.mensaje = mensaje;

            return response;
        }
    }

    public static class ResumenReciboDTO {

        public int codigo;
        public int tipo;
        public Date fecha;

        public int idMovimientoCaja;

        public int idCliente;
        public String nombreCliente;

        public double total;
        public String observacion;
    }

    public static class DetalleReciboDTO {

        public int codigo;
        public Date fecha;
        public double total;

        public int idVendedor;
        public String nombreVendedor;

        public int idCliente;
        public String nombreCliente;

        public int idMovimientoCaja;
        public int estado;
        public String observacion;
        public int tipo;
    }

    // -------------------------------------------------------------------------
    // PRESUPUESTO
    // -------------------------------------------------------------------------

    public static class GuardarPresupuestoRequest {

        /**
         * Si es mayor a cero, se reemplaza el presupuesto anterior.
         */
        public int idPresupuesto;

        public int cliente;
        public int usuario;
        public int listaPrecios;

        public double total;
        public String observacion;

        public List<PresupuestoItemRequest> items =
                new ArrayList<>();
    }

    public static class PresupuestoItemRequest {

        public String codigoArticulo;
        public String descripcionArticulo;

        public double cantidad;
        public double precioLista;
        public double bonificacion;
        public double precio;
        public double iva;

        public int proveedor;
    }

    public static class GuardarPresupuestoResponse {

        public boolean ok;
        public int codigo;
        public String mensaje;

        public static GuardarPresupuestoResponse ok(
                int codigo
        ) {

            GuardarPresupuestoResponse response =
                    new GuardarPresupuestoResponse();

            response.ok = true;
            response.codigo = codigo;

            return response;
        }

        public static GuardarPresupuestoResponse error(
                String mensaje
        ) {

            GuardarPresupuestoResponse response =
                    new GuardarPresupuestoResponse();

            response.ok = false;
            response.codigo = -1;
            response.mensaje = mensaje;

            return response;
        }
    }

    public static class PresupuestoDTO {

        public int codigo;
        public Date fecha;

        public int cliente;
        public String nombreCliente;

        public int usuario;
        public int listaPrecios;

        public double total;
        public Date vencimiento;

        public int estado;
        public String observacion;

        public List<PresupuestoItemDTO> items =
                new ArrayList<>();
    }

    public static class PresupuestoItemDTO {

        public int codigo;
        public int presupuesto;

        public String codigoArticulo;
        public String descripcionArticulo;

        public double cantidad;
        public double precioLista;
        public double bonificacion;
        public double precio;
        public double iva;

        public int proveedor;
    }

    public static class ResumenPresupuestoDTO {

        public int codigo;
        public Date fecha;

        public int cliente;
        public String razonSocial;

        public int usuario;
        public int listaPrecios;

        public double total;

        public Date vencimiento;
        public int estado;
        public String observacion;
    }

    public static class ClientePresupuestoDTO {

        public int codigoCliente;
    }

    // -------------------------------------------------------------------------
    // FACTURACIÓN
    // -------------------------------------------------------------------------

    public static class ClienteFiscalDTO {

        public int codigo;

        public String tipoIdentificacion;
        public String codigoIdentificacion;

        public String condicionIVA;
        public String razonSocial;

        public int localidad;
    }

    public static class TicketFacturacionDTO {

        public int codigo;
        public Date fecha;

        public int tipo;
        public double total;

        public int idVendedor;
        public String nombreVendedor;

        public int idCliente;
        public String nombreCliente;

        public int listaPrecios;
        public int estado;
        public String observacion;

        public ClienteFiscalDTO cliente;

        public List<ItemFacturacionDTO> items =
                new ArrayList<>();
    }

    public static class ItemFacturacionDTO {

        public String codigoArticulo;
        public String descripcion;

        public double cantidad;
        public double margen;

        public double neto;
        public double alicuota;
        public double importeIVA;
        public double precio;
        public double bonificacion;

        public String codigoIVA;
    }

    public static class TipoComprobanteDTO {

        public String codigo;
        public String descripcion;
        public String letra;
        public int impacto;
    }

    public static class GuardarComprobanteFiscalRequest {

        public int ticket;

        public String tipoComprobante;
        public String tipoComprobanteNombre;
        public String letra;

        public int puntoVenta;
        public int numero;

        public String descripcion;

        public String clienteCategoriaIVA;
        public String clienteCUIT;
        public int clienteLocalidad;
        public String razonSocial;

        public Date fecha;
        public Date fechaVencimiento;

        public double neto;
        public double noGravado;
        public double iva;
        public double total;

        public int moneda;

        public String cae;
        public Date caeVencimiento;
        public String codigoBarra;

        public String observacion;
        public int estado;

        public List<ComprobanteImpuestoRequest> impuestos =
                new ArrayList<>();
    }

    public static class ComprobanteImpuestoRequest {

        public String impuesto;
        public double baseImponible;
        public double monto;
        public double porcentaje;
    }

    public static class ComprobanteFiscalDTO {

        public int codigo;
        public int ticket;

        public String letra;

        public int puntoVenta;
        public long numero;

        public int tipoComprobante;
        public String tipoComprobanteNombre;

        public Date fecha;

        public int clienteCodigo;
        public String clienteRazonSocial;
        public String clienteDomicilio;
        public String clienteLocalidad;
        public String clienteCondicionIVA;

        public String clienteCuit;
        public int clienteCategoriaIVA;
        public String clienteIngresosBrutos;
        public String clienteTipoIdentificacion;

        public double neto;
        public double noGravado;
        public double iva;
        public double subtotal;
        public double total;

        public String observacion;

        public String cae;
        public Date caeVencimiento;
        public String codigoBarra;
    }

    public static class ComprobanteAsociadoDTO {

        public Date fecha;

        public String descripcion;
        public String tipoComprobante;

        public int puntoVenta;
        public long numero;

        public double neto;
        public double iva;
        public double total;
    }

    // -------------------------------------------------------------------------
    // VISOR
    // -------------------------------------------------------------------------

    public static class DetalleTicketDTO {

        public int codigo;
        public Date fecha;
        public double total;

        public int idVendedor;
        public String nombreVendedor;

        public int idCliente;
        public String nombreCliente;

        public int estado;
        public String observacion;
        public int tipo;

        public List<DetalleItemDTO> items =
                new ArrayList<>();
    }

    public static class DetalleItemDTO {

        public String codigoArticulo;
        public String descripcion;

        public double precio;
        public double cantidad;
        public double alicuota;
        public double bonificacion;
        public double totalCalculado;
    }

    // -------------------------------------------------------------------------
    // REPORTES
    // -------------------------------------------------------------------------

    public static class LibroIvaVentaDTO {

        private String fecha;
        private String descripcion;
        private String cae;
        private String condicion;
        private String cuit;
        private String cliente;

        private double neto;
        private double iva105;
        private double iva21;
        private double total;

        public String getFecha() {
            return fecha;
        }

        public void setFecha(
                String fecha
        ) {
            this.fecha = fecha;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(
                String descripcion
        ) {
            this.descripcion = descripcion;
        }

        public String getCae() {
            return cae;
        }

        public void setCae(
                String cae
        ) {
            this.cae = cae;
        }

        public String getCondicion() {
            return condicion;
        }

        public void setCondicion(
                String condicion
        ) {
            this.condicion = condicion;
        }

        public String getCuit() {
            return cuit;
        }

        public void setCuit(
                String cuit
        ) {
            this.cuit = cuit;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(
                String cliente
        ) {
            this.cliente = cliente;
        }

        public double getNeto() {
            return neto;
        }

        public void setNeto(
                double neto
        ) {
            this.neto = neto;
        }

        public double getIva105() {
            return iva105;
        }

        public void setIva105(
                double iva105
        ) {
            this.iva105 = iva105;
        }

        public double getIva21() {
            return iva21;
        }

        public void setIva21(
                double iva21
        ) {
            this.iva21 = iva21;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(
                double total
        ) {
            this.total = total;
        }
    }
}