package com.util.api.reportes.citi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CitiDTO {

    private CitiDTO() {
    }

    public static class VentaDTO {

        public int codigo;
        public Date fecha;

        public int tipoComprobante;
        public String letra;
        public int puntoVenta;
        public long numero;

        public String clienteCUIT;
        public String razonSocial;

        public BigDecimal neto = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;

        public List<AlicuotaDTO> alicuotas = new ArrayList<>();
    }

    public static class CompraDTO {

        public int codigo;
        public Date fecha;

        public int tipoComprobante;
        public String letra;
        public int puntoVenta;
        public long numero;

        public int tipoDocumento = 80;
        public String proveedorCUIT;
        public String razonSocial;

        public BigDecimal total = BigDecimal.ZERO;
        public BigDecimal noGravado = BigDecimal.ZERO;
        public BigDecimal exento = BigDecimal.ZERO;

        public BigDecimal percepcionIVA = BigDecimal.ZERO;
        public BigDecimal percepcionIIBB = BigDecimal.ZERO;
        public BigDecimal impuestosInternos = BigDecimal.ZERO;
        public BigDecimal impuestosMunicipales = BigDecimal.ZERO;
        public BigDecimal otrosImpuestos = BigDecimal.ZERO;

        public BigDecimal netoGravado = BigDecimal.ZERO;

        public List<AlicuotaDTO> alicuotas = new ArrayList<>();
    }

    public static class AlicuotaDTO {

        public String codigo;

        public BigDecimal porcentaje = BigDecimal.ZERO;
        public BigDecimal baseImponible = BigDecimal.ZERO;
        public BigDecimal monto = BigDecimal.ZERO;
    }
}