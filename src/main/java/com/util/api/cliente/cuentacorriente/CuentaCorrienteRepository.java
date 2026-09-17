package com.util.api.cliente.cuentacorriente;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CuentaCorrienteRepository {

    @Inject
    DataSource dataSource;

    public CuentaCorrienteDTO.ResumenDTO obtenerResumen(
            int codigoCliente,
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        CuentaCorrienteDTO.ResumenDTO resumen =
                new CuentaCorrienteDTO.ResumenDTO();

        if (codigoCliente <= 1
                || desde == null
                || hasta == null
                || desde.isAfter(hasta)) {

            return resumen;
        }

        LocalDate fechaAnterior =
                desde.minusDays(1);

        String sql = """
                SELECT
                    comps.comp,
                    comps.tipo,
                    comps.codigo,
                    comps.fecha,
                    comps.total,
                    comps.idcliente,
                    comps.estado,
                    comps.acopio,
                    comps.observacion
                FROM (
                    SELECT
                        'Saldo Ant.' AS comp,
                        1 AS tipo,
                        0 AS codigo,
                        ?::date AS fecha,
                        COALESCE(
                            util.saldoHasta(?, ?::date),
                            0
                        ) AS total,
                        ? AS idcliente,
                        0 AS estado,
                        false AS acopio,
                        '' AS observacion

                    UNION

                    SELECT
                        'Ticket' AS comp,
                        tipo,
                        codigo,
                        fecha,
                        total,
                        idcliente,
                        estado,
                        acopio,
                        observacion
                    FROM util.ticket
                    WHERE idcliente = ?
                      AND CAST(fecha AS DATE) >= ?

                    UNION

                    SELECT
                        'Recibo' AS comp,
                        tipo,
                        codigo,
                        fecha,
                        total,
                        idcliente,
                        estado,
                        false AS acopio,
                        observacion
                    FROM util.recibo
                    WHERE idcliente = ?
                      AND CAST(fecha AS DATE) >= ?
                ) AS comps
                WHERE comps.idcliente = ?
                  AND CAST(comps.fecha AS DATE) <= ?
                ORDER BY comps.fecha ASC,
                         comps.codigo DESC
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            int i = 1;

            Date fechaAnteriorSQL =
                    Date.valueOf(fechaAnterior);

            Date desdeSQL =
                    Date.valueOf(desde);

            Date hastaSQL =
                    Date.valueOf(hasta);

            ps.setDate(i++, fechaAnteriorSQL);
            ps.setInt(i++, codigoCliente);
            ps.setDate(i++, fechaAnteriorSQL);
            ps.setInt(i++, codigoCliente);

            ps.setInt(i++, codigoCliente);
            ps.setDate(i++, desdeSQL);

            ps.setInt(i++, codigoCliente);
            ps.setDate(i++, desdeSQL);

            ps.setInt(i++, codigoCliente);
            ps.setDate(i, hastaSQL);

            try (ResultSet rs = ps.executeQuery()) {
                mapearResumen(
                        rs,
                        resumen
                );
            }
        }

        return resumen;
    }

    private void mapearResumen(
            ResultSet rs,
            CuentaCorrienteDTO.ResumenDTO resumen
    ) throws SQLException {

        List<CuentaCorrienteDTO.MovimientoDTO> movimientos =
                new ArrayList<>();

        double totalVendido = 0;
        double totalCobrado = 0;

        while (rs.next()) {
            CuentaCorrienteDTO.MovimientoDTO movimiento =
                    new CuentaCorrienteDTO.MovimientoDTO();

            String comprobante =
                    rs.getString("comp");

            int tipoOperacion =
                    rs.getInt("tipo");

            int codigo =
                    rs.getInt("codigo");

            double total =
                    rs.getDouble("total");

            movimiento.codigo = codigo;
            movimiento.fecha =
                    rs.getTimestamp("fecha");
            movimiento.acopio =
                    rs.getBoolean("acopio");
            movimiento.observacion =
                    rs.getString("observacion");

            if ("Ticket".equals(comprobante)) {
                if (tipoOperacion == 1) {
                    movimiento.tipo = "TICKET";

                    movimiento.descripcion =
                            "Ticket - "
                                    + completarConCeros(
                                    codigo,
                                    6
                            );

                    if (movimiento.acopio) {
                        movimiento.descripcion +=
                                " - ACOPIO";
                    }

                    movimiento.debe = total;
                    movimiento.haber = 0;

                } else {
                    movimiento.tipo = "DEVOLUCION";

                    movimiento.descripcion =
                            "Devolucion - "
                                    + completarConCeros(
                                    codigo,
                                    6
                            );

                    movimiento.debe = 0;
                    movimiento.haber = -total;
                }

                /*
                 * Los tickets normales suman.
                 * Las devoluciones se almacenan con total negativo.
                 */
                totalVendido += total;

            } else if ("Recibo".equals(comprobante)) {
                if (tipoOperacion == 1) {
                    movimiento.tipo = "RECIBO";

                    movimiento.descripcion =
                            "Recibo - "
                                    + completarConCeros(
                                    codigo,
                                    6
                            );

                    movimiento.debe = 0;
                    movimiento.haber = total;

                } else {
                    movimiento.tipo =
                            "PAGO_DEVOLUCION";

                    movimiento.descripcion =
                            "Pago Devolucion - "
                                    + completarConCeros(
                                    codigo,
                                    6
                            );

                    movimiento.debe = -total;
                    movimiento.haber = 0;
                }

                /*
                 * Los recibos normales suman a lo cobrado.
                 * Los pagos de devolución tienen total negativo.
                 */
                totalCobrado += total;

            } else {
                movimiento.tipo =
                        "SALDO_ANTERIOR";

                movimiento.descripcion =
                        comprobante;

                movimiento.debe = total;
                movimiento.haber = 0;

                totalVendido += total;
            }

            movimiento.saldo =
                    totalVendido
                            - totalCobrado;

            movimientos.add(movimiento);
        }

        resumen.movimientos =
                movimientos;

        resumen.totalVendido =
                totalVendido;

        resumen.totalCobrado =
                totalCobrado;

        resumen.saldo =
                totalVendido
                        - totalCobrado;
    }

    private String completarConCeros(
            int numero,
            int longitud
    ) {
        return String.format(
                "%0" + longitud + "d",
                numero
        );
    }
}