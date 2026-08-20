package com.util.api.cliente.cuentacorriente;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CuentaCorrienteDTO {

    private CuentaCorrienteDTO() {
    }

    public static class ResumenDTO {

        public List<MovimientoDTO> movimientos =
                new ArrayList<>();

        public double totalVendido;
        public double totalCobrado;
        public double saldo;

        public ResumenDTO() {
        }
    }

    public static class MovimientoDTO {

        public Date fecha;

        public int codigo;

        public String tipo;
        public String descripcion;
        public String observacion;

        public double debe;
        public double haber;
        public double saldo;

        public MovimientoDTO() {
        }
    }
}