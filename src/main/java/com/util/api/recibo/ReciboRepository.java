package com.util.api.recibo;

import com.util.api.ticket.TicketDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReciboRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // GUARDAR RECIBO
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarReciboResponse guardarRecibo(
            TicketDTO.GuardarReciboRequest request
    ) {

        String error = validarRecibo(request);

        if (error != null) {
            return TicketDTO.GuardarReciboResponse.error(error);
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                TicketDTO.GuardarReciboResponse response =
                        guardarRecibo(
                                cn,
                                request
                        );

                cn.commit();

                return response;

            } catch (Exception ex) {

                rollback(cn);

                ex.printStackTrace();

                return TicketDTO.GuardarReciboResponse.error(
                        "Error guardando recibo: "
                                + mensaje(ex)
                );

            } finally {

                restaurarAutoCommit(
                        cn,
                        autoCommitOriginal
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            return TicketDTO.GuardarReciboResponse.error(
                    "Error obteniendo conexión: "
                            + mensaje(ex)
            );
        }
    }

    public TicketDTO.GuardarReciboResponse guardarRecibo(
            Connection cn,
            TicketDTO.GuardarReciboRequest request
    ) throws SQLException {

        String error = validarRecibo(request);

        if (error != null) {
            throw new SQLException(error);
        }

        double total =
                request.tipo == 1
                        ? request.total
                        : -Math.abs(request.total);

        boolean mayorista =
                obtenerClienteMayorista(
                        cn,
                        request.cliente
                );

        int codigoMovimientoCaja =
                insertarMovimientoRecibo(
                        cn,
                        request.caja,
                        total
                );

        int codigoRecibo =
                insertarRecibo(
                        cn,
                        request,
                        codigoMovimientoCaja,
                        total,
                        mayorista
                );

        insertarPagosRecibo(
                cn,
                request,
                codigoRecibo,
                codigoMovimientoCaja
        );

        return TicketDTO.GuardarReciboResponse.ok(
                codigoRecibo,
                codigoMovimientoCaja
        );
    }

    private boolean obtenerClienteMayorista(
            Connection cn,
            int codigoCliente
    ) throws SQLException {

        String sql = """
            SELECT mayorista
            FROM util.cliente
            WHERE codigo = ?
            """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, codigoCliente);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                return rs.getBoolean("mayorista");
            }
        }
    }

    private int insertarMovimientoRecibo(
            Connection cn,
            int caja,
            double total
    ) throws SQLException {

        String sql = """
                INSERT INTO util.movimientocaja (
                    idcaja,
                    importe,
                    estado
                )
                VALUES (?, ?, 10)
                RETURNING codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    caja
            );

            ps.setBigDecimal(
                    2,
                    decimal(total)
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo crear el movimiento de caja"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private int insertarRecibo(
            Connection cn,
            TicketDTO.GuardarReciboRequest request,
            int codigoMovimientoCaja,
            double total,
            boolean mayorista
    ) throws SQLException {

        String sql = """
            INSERT INTO util.recibo (
                tipo,
                fecha,
                total,
                idvendedor,
                nombrevendedor,
                idcliente,
                nombrecliente,
                idmovcaja,
                estado,
                observacion,
                mayorista
            )
            VALUES (
                ?,
                NOW(),
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                10,
                ?,
                ?
            )
            RETURNING codigo
            """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, request.tipo);
            ps.setBigDecimal(2, decimal(total));
            ps.setInt(3, request.vendedor);
            ps.setString(4, texto(request.nombreVendedor));
            ps.setInt(5, request.cliente);
            ps.setString(6, texto(request.nombreCliente));
            ps.setInt(7, codigoMovimientoCaja);
            ps.setString(8, texto(request.observacion));
            ps.setBoolean(9, mayorista);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el código del recibo"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private void insertarPagosRecibo(
            Connection cn,
            TicketDTO.GuardarReciboRequest request,
            int codigoRecibo,
            int codigoMovimientoCaja
    ) throws SQLException {

        if (request.pagos == null
                || request.pagos.isEmpty()) {

            return;
        }

        String sql = """
                INSERT INTO util.pago (
                    tipo,
                    idmovcaja,
                    importe,
                    id_medio_pago
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            for (TicketDTO.PagoRequest pago : request.pagos) {

                if (pago == null) {
                    continue;
                }

                ps.setInt(
                        1,
                        pago.tipoLegacy
                );

                ps.setInt(
                        2,
                        codigoMovimientoCaja
                );

                ps.setBigDecimal(
                        3,
                        decimal(pago.importe)
                );

                if (pago.idMedioPago == null) {

                    ps.setNull(
                            4,
                            Types.INTEGER
                    );

                } else {

                    ps.setInt(
                            4,
                            pago.idMedioPago
                    );
                }

                ps.addBatch();

                if (pago.idMedioPago != null) {

                    insertarComisionMedioPago(
                            cn,
                            request.caja,
                            codigoRecibo,
                            pago.idMedioPago,
                            pago.importe
                    );
                }
            }

            ps.executeBatch();
        }
    }

    private String validarRecibo(
            TicketDTO.GuardarReciboRequest request
    ) {

        if (request == null) {
            return "Los datos del recibo son obligatorios";
        }

        if (request.caja <= 0) {
            return "No hay una caja abierta";
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // COMISIÓN DEL MEDIO DE PAGO
    // -------------------------------------------------------------------------

    private void insertarComisionMedioPago(
            Connection cn,
            int caja,
            int codigoRecibo,
            int idMedioPago,
            double importePago
    ) throws SQLException {

        String sql = """
                SELECT
                    descripcion,
                    porcentaje_comision
                FROM util.medio_pago
                WHERE id = ?
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    idMedioPago
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return;
                }

                double porcentaje =
                        rs.getDouble(
                                "porcentaje_comision"
                        );

                if (porcentaje <= 0) {
                    return;
                }

                double importeComision =
                        redondear(
                                importePago
                                        * porcentaje
                                        / 100d,
                                2
                        );

                if (importeComision <= 0) {
                    return;
                }

                String observacion =
                        "Comision "
                                + texto(
                                rs.getString(
                                        "descripcion"
                                )
                        )
                                + " "
                                + porcentaje
                                + "% - Recibo "
                                + codigoRecibo;

                insertarMovimientoComision(
                        cn,
                        caja,
                        importeComision,
                        observacion
                );
            }
        }
    }

    private void insertarMovimientoComision(
            Connection cn,
            int caja,
            double importeComision,
            String observacion
    ) throws SQLException {

        String sql = """
                INSERT INTO util.movimientocaja (
                    idcaja,
                    importe,
                    estado,
                    tipomovcaja,
                    observacion
                )
                VALUES (?, ?, 10, 20, ?)
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    caja
            );

            ps.setBigDecimal(
                    2,
                    decimal(
                            -Math.abs(importeComision)
                    )
            );

            ps.setString(
                    3,
                    texto(observacion)
            );

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER RECIBO
    // -------------------------------------------------------------------------

    public TicketDTO.DetalleReciboDTO obtenerRecibo(
            int codigoRecibo
    ) throws SQLException {

        if (codigoRecibo <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    codigo,
                    fecha,
                    total,
                    idvendedor,
                    nombrevendedor,
                    idcliente,
                    nombrecliente,
                    idmovcaja,
                    estado,
                    observacion,
                    tipo
                FROM util.recibo
                WHERE codigo = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoRecibo
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                TicketDTO.DetalleReciboDTO dto =
                        new TicketDTO.DetalleReciboDTO();

                dto.codigo =
                        rs.getInt("codigo");

                dto.fecha =
                        rs.getTimestamp("fecha");

                dto.total =
                        rs.getDouble("total");

                dto.idVendedor =
                        rs.getInt("idvendedor");

                dto.nombreVendedor =
                        rs.getString("nombrevendedor");

                dto.idCliente =
                        rs.getInt("idcliente");

                dto.nombreCliente =
                        rs.getString("nombrecliente");

                dto.idMovimientoCaja =
                        rs.getInt("idmovcaja");

                dto.estado =
                        rs.getInt("estado");

                dto.observacion =
                        rs.getString("observacion");

                dto.tipo =
                        rs.getInt("tipo");

                return dto;
            }
        }
    }

    // -------------------------------------------------------------------------
    // LISTAR RECIBOS
    // -------------------------------------------------------------------------

    public List<TicketDTO.ResumenReciboDTO> listarRecibos(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<TicketDTO.ResumenReciboDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    tipo,
                    codigo,
                    fecha,
                    total,
                    idcliente,
                    nombrecliente,
                    estado,
                    observacion,
                    idmovcaja
                FROM util.recibo
                WHERE CAST(fecha AS DATE) >= ?
                  AND CAST(fecha AS DATE) <= ?
                ORDER BY fecha DESC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setDate(
                    1,
                    Date.valueOf(desde)
            );

            ps.setDate(
                    2,
                    Date.valueOf(hasta)
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.ResumenReciboDTO dto =
                            new TicketDTO.ResumenReciboDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.tipo =
                            rs.getInt("tipo");

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.idMovimientoCaja =
                            rs.getInt("idmovcaja");

                    dto.idCliente =
                            rs.getInt("idcliente");

                    dto.nombreCliente =
                            rs.getString("nombrecliente");

                    dto.total =
                            rs.getDouble("total");

                    dto.observacion =
                            rs.getString("observacion");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // ANULAR RECIBO
    // -------------------------------------------------------------------------

    public boolean anularRecibo(
            int codigoRecibo,
            int codigoMovimientoCaja
    ) {

        if (codigoRecibo <= 0
                || codigoMovimientoCaja <= 0) {

            return false;
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                ejecutarDelete(
                        cn,
                        """
                        DELETE FROM util.pago
                        WHERE idmovcaja = ?
                        """,
                        codigoMovimientoCaja
                );

                ejecutarDelete(
                        cn,
                        """
                        DELETE FROM util.recibo
                        WHERE codigo = ?
                        """,
                        codigoRecibo
                );

                ejecutarDelete(
                        cn,
                        """
                        DELETE FROM util.movimientocaja
                        WHERE codigo = ?
                        """,
                        codigoMovimientoCaja
                );

                cn.commit();

                return true;

            } catch (Exception ex) {

                rollback(cn);
                ex.printStackTrace();

                return false;

            } finally {

                restaurarAutoCommit(
                        cn,
                        autoCommitOriginal
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    private void ejecutarDelete(
            Connection cn,
            String sql,
            int codigo
    ) throws SQLException {

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigo
            );

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private boolean rangoValido(
            LocalDate desde,
            LocalDate hasta
    ) {

        return desde != null
                && hasta != null
                && !desde.isAfter(hasta);
    }

    private BigDecimal decimal(
            double valor
    ) {

        return BigDecimal.valueOf(valor);
    }

    private String texto(
            String valor
    ) {

        return valor == null
                ? ""
                : valor;
    }

    private double redondear(
            double valor,
            int decimales
    ) {

        double factor =
                Math.pow(
                        10,
                        decimales
                );

        return Math.round(
                valor * factor
        ) / factor;
    }

    private void rollback(
            Connection cn
    ) {

        if (cn == null) {
            return;
        }

        try {
            cn.rollback();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void restaurarAutoCommit(
            Connection cn,
            boolean autoCommitOriginal
    ) {

        if (cn == null) {
            return;
        }

        try {
            cn.setAutoCommit(
                    autoCommitOriginal
            );

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String mensaje(
            Exception ex
    ) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "error inesperado";
        }

        return ex.getMessage();
    }
}