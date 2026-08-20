package com.util.api.reportes.citi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CitiRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // VENTAS
    // -------------------------------------------------------------------------

    public List<CitiDTO.VentaDTO> obtenerVentas(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<CitiDTO.VentaDTO> result = new ArrayList<>();

        if (desde == null || hasta == null) {
            return result;
        }

        try (Connection cn = dataSource.getConnection()) {

            Map<Integer, CitiDTO.VentaDTO> ventas =
                    obtenerCabecerasVentas(
                            cn,
                            desde,
                            hasta
                    );

            cargarAlicuotasVentas(
                    cn,
                    desde,
                    hasta,
                    ventas
            );

            result.addAll(ventas.values());
        }

        return result;
    }

    private Map<Integer, CitiDTO.VentaDTO> obtenerCabecerasVentas(
            Connection cn,
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        Map<Integer, CitiDTO.VentaDTO> result =
                new LinkedHashMap<>();

        String sql = """
                SELECT
                    codigo,
                    fecha,
                    tipocomprobante,
                    letra,
                    puntoventa,
                    numero,
                    clientecuit,
                    razonsocial,
                    neto,
                    total
                FROM util.comprobantefiscal
                WHERE CAST(fecha AS DATE) >= ?
                  AND CAST(fecha AS DATE) <= ?
                  AND estado = 1
                ORDER BY fecha, codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CitiDTO.VentaDTO dto =
                            new CitiDTO.VentaDTO();

                    dto.codigo = rs.getInt("codigo");
                    dto.fecha = rs.getTimestamp("fecha");

                    dto.tipoComprobante =
                            rs.getInt("tipocomprobante");

                    dto.letra = rs.getString("letra");
                    dto.puntoVenta = rs.getInt("puntoventa");
                    dto.numero = rs.getLong("numero");

                    dto.clienteCUIT =
                            rs.getString("clientecuit");

                    dto.razonSocial =
                            rs.getString("razonsocial");

                    dto.neto = valor(rs, "neto");
                    dto.total = valor(rs, "total");

                    result.put(dto.codigo, dto);
                }
            }
        }

        return result;
    }

    private void cargarAlicuotasVentas(
            Connection cn,
            LocalDate desde,
            LocalDate hasta,
            Map<Integer, CitiDTO.VentaDTO> ventas
    ) throws SQLException {

        if (ventas.isEmpty()) {
            return;
        }

        String sql = """
                SELECT
                    ci.comprobante,
                    ci.baseimponible,
                    ci.monto,
                    ci.porcentaje,
                    imp.codigo
                FROM util.comprobanteimpuesto ci
                INNER JOIN util.impuesto imp
                        ON imp.codigo = ci.impuesto
                INNER JOIN util.comprobantefiscal cf
                        ON cf.codigo = ci.comprobante
                WHERE CAST(cf.fecha AS DATE) >= ?
                  AND CAST(cf.fecha AS DATE) <= ?
                  AND cf.estado = 1
                ORDER BY ci.comprobante, imp.codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CitiDTO.VentaDTO venta =
                            ventas.get(
                                    rs.getInt("comprobante")
                            );

                    if (venta == null) {
                        continue;
                    }

                    CitiDTO.AlicuotaDTO alicuota =
                            new CitiDTO.AlicuotaDTO();

                    alicuota.codigo =
                            rs.getString("codigo");

                    alicuota.baseImponible =
                            valor(rs, "baseimponible");

                    alicuota.monto =
                            valor(rs, "monto");

                    alicuota.porcentaje =
                            valor(rs, "porcentaje");

                    venta.alicuotas.add(alicuota);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // COMPRAS
    // -------------------------------------------------------------------------

    public List<CitiDTO.CompraDTO> obtenerCompras(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<CitiDTO.CompraDTO> result = new ArrayList<>();

        if (desde == null || hasta == null) {
            return result;
        }

        try (Connection cn = dataSource.getConnection()) {

            Map<Integer, CitiDTO.CompraDTO> compras =
                    obtenerCabecerasCompras(
                            cn,
                            desde,
                            hasta
                    );

            cargarAlicuotasCompras(
                    cn,
                    desde,
                    hasta,
                    compras
            );

            result.addAll(compras.values());
        }

        return result;
    }

    private Map<Integer, CitiDTO.CompraDTO> obtenerCabecerasCompras(
            Connection cn,
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        Map<Integer, CitiDTO.CompraDTO> result =
                new LinkedHashMap<>();

        String sql = """
                SELECT
                    cab.id,
                    cab.fechacomprobante,
                    cab.codigocompcompra,
                    cab.letra,
                    cab.centro,
                    cab.numero,
                    cab.total,
                    cab.conceptosnogravados,
                    cab.exentos,
                    cab.periva,
                    cab.periibb,
                    cab.perimpinternos,
                    cab.perimpmunicipales,
                    cab.perotrosimp,
                    cab.netogravado,
                    prov.razonsocial,
                    prov.codigoidentificacion
                FROM util.comprobantecompracab cab
                INNER JOIN util.proveedor prov
                        ON prov.codigo = cab.nroproveedor
                WHERE CAST(cab.fechacomprobante AS DATE) >= ?
                  AND CAST(cab.fechacomprobante AS DATE) <= ?
                ORDER BY cab.fechacomprobante, cab.id
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CitiDTO.CompraDTO dto =
                            new CitiDTO.CompraDTO();

                    dto.codigo = rs.getInt("id");

                    dto.fecha =
                            rs.getTimestamp("fechacomprobante");

                    dto.tipoComprobante =
                            rs.getInt("codigocompcompra");

                    dto.letra = rs.getString("letra");
                    dto.puntoVenta = rs.getInt("centro");
                    dto.numero = rs.getLong("numero");

                    dto.tipoDocumento = 80;

                    dto.proveedorCUIT =
                            rs.getString("codigoidentificacion");

                    dto.razonSocial =
                            rs.getString("razonsocial");

                    dto.total =
                            valor(rs, "total");

                    dto.noGravado =
                            valor(rs, "conceptosnogravados");

                    dto.exento =
                            valor(rs, "exentos");

                    dto.percepcionIVA =
                            valor(rs, "periva");

                    dto.percepcionIIBB =
                            valor(rs, "periibb");

                    dto.impuestosInternos =
                            valor(rs, "perimpinternos");

                    dto.impuestosMunicipales =
                            valor(rs, "perimpmunicipales");

                    dto.otrosImpuestos =
                            valor(rs, "perotrosimp");

                    dto.netoGravado =
                            valor(rs, "netogravado");

                    result.put(dto.codigo, dto);
                }
            }
        }

        return result;
    }

    private void cargarAlicuotasCompras(
            Connection cn,
            LocalDate desde,
            LocalDate hasta,
            Map<Integer, CitiDTO.CompraDTO> compras
    ) throws SQLException {

        if (compras.isEmpty()) {
            return;
        }

        String sql = """
                SELECT
                    reng.idcomprobante,
                    reng.tasaiva,
                    imp.codigo,
                    SUM(
                        reng.preciobonificado * reng.cantidad
                    ) AS baseimponible,
                    SUM(
                        (reng.preciobonificado * reng.cantidad)
                        * (reng.tasaiva / 100)
                    ) AS monto
                FROM util.comprobantecomprareng reng
                INNER JOIN util.comprobantecompracab cab
                        ON cab.id = reng.idcomprobante
                INNER JOIN util.impuesto imp
                        ON imp.alicuota = reng.tasaiva
                WHERE CAST(cab.fechacomprobante AS DATE) >= ?
                  AND CAST(cab.fechacomprobante AS DATE) <= ?
                  AND reng.tasaiva > 0
                GROUP BY
                    reng.idcomprobante,
                    reng.tasaiva,
                    imp.codigo
                ORDER BY
                    reng.idcomprobante,
                    reng.tasaiva
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CitiDTO.CompraDTO compra =
                            compras.get(
                                    rs.getInt("idcomprobante")
                            );

                    if (compra == null) {
                        continue;
                    }

                    CitiDTO.AlicuotaDTO alicuota =
                            new CitiDTO.AlicuotaDTO();

                    alicuota.codigo =
                            rs.getString("codigo");

                    alicuota.porcentaje =
                            valor(rs, "tasaiva");

                    alicuota.baseImponible =
                            valor(rs, "baseimponible");

                    alicuota.monto =
                            valor(rs, "monto");

                    compra.alicuotas.add(alicuota);
                }
            }
        }
    }

    private BigDecimal valor(
            ResultSet rs,
            String columna
    ) throws SQLException {

        BigDecimal value =
                rs.getBigDecimal(columna);

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}