package com.util.api.reportes;

import java.math.BigDecimal;
import java.util.Date;

public final class ReporteDTO {

    private ReporteDTO() {
    }

    public static class DeudaClienteDTO {

        public int codigoCliente;
        public String razonSocial;
        public String telefonoFijo;
        public String telefonoMovil;
        public BigDecimal saldo = BigDecimal.ZERO;
    }

    public static class ProductoSinMovimientoDTO {

        public String codigo;
        public String descripcion;
        public String proveedor;
        public BigDecimal stock = BigDecimal.ZERO;
        public BigDecimal stockMinimo = BigDecimal.ZERO;
    }

    public static class VentaProductoDTO {

        public String codigo;
        public String descripcion;
        public BigDecimal cantidad = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;
    }

    public static class ProductoRentableDTO {

        public String codigo;
        public String descripcion;
        public BigDecimal cantidad = BigDecimal.ZERO;
        public BigDecimal totalVenta = BigDecimal.ZERO;
        public BigDecimal totalCosto = BigDecimal.ZERO;
        public BigDecimal ganancia = BigDecimal.ZERO;
    }

    public static class EvolucionVentaDTO {

        public Date fecha;
        public int cantidadTickets;
        public BigDecimal subtotal = BigDecimal.ZERO;
        public BigDecimal bonificacion = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;
    }
}