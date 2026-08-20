package com.util.api.presupuesto;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PresupuestoRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // GUARDAR
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarPresupuestoResponse guardarPresupuesto(
            TicketDTO.GuardarPresupuestoRequest request
    ) {

        if (request == null) {
            return TicketDTO.GuardarPresupuestoResponse.error(
                    "Los datos del presupuesto son obligatorios"
            );
        }

        if (request.cliente <= 0) {
            return TicketDTO.GuardarPresupuestoResponse.error(
                    "El cliente es obligatorio"
            );
        }

        if (request.items == null || request.items.isEmpty()) {
            return TicketDTO.GuardarPresupuestoResponse.error(
                    "El presupuesto no contiene artículos"
            );
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                if (request.idPresupuesto > 0) {
                    eliminarPresupuestoAnterior(
                            cn,
                            request.idPresupuesto
                    );
                }

                int codigoPresupuesto =
                        insertarCabecera(
                                cn,
                                request
                        );

                insertarItems(
                        cn,
                        codigoPresupuesto,
                        request.items
                );

                cn.commit();

                return TicketDTO.GuardarPresupuestoResponse.ok(
                        codigoPresupuesto
                );

            } catch (Exception ex) {

                rollback(cn);
                ex.printStackTrace();

                return TicketDTO.GuardarPresupuestoResponse.error(
                        "Error guardando presupuesto: "
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

            return TicketDTO.GuardarPresupuestoResponse.error(
                    "Error obteniendo conexión: "
                            + mensaje(ex)
            );
        }
    }

    private int insertarCabecera(
            Connection cn,
            TicketDTO.GuardarPresupuestoRequest request
    ) throws SQLException {

        String sql = """
                INSERT INTO util.ventaspresupuestocab (
                    fecha,
                    cliente,
                    usuario,
                    listaprecios,
                    total,
                    vencimiento,
                    estado,
                    observacion
                )
                VALUES (
                    NOW(),
                    ?,
                    ?,
                    ?,
                    ?,
                    NOW(),
                    0,
                    ?
                )
                RETURNING codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.cliente
            );

            ps.setInt(
                    2,
                    request.usuario
            );

            ps.setInt(
                    3,
                    request.listaPrecios
            );

            ps.setBigDecimal(
                    4,
                    decimal(request.total)
            );

            ps.setString(
                    5,
                    texto(request.observacion)
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el código "
                                    + "del presupuesto"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private void insertarItems(
            Connection cn,
            int codigoPresupuesto,
            List<TicketDTO.PresupuestoItemRequest> items
    ) throws SQLException {

        String sql = """
                INSERT INTO util.ventaspresupuestoreng (
                    ventaspresupuestocab,
                    codigoarticulo,
                    descripcionarticulo,
                    cantidad,
                    preciolista,
                    bonificacion,
                    precio,
                    iva,
                    proveedor
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            for (
                    TicketDTO.PresupuestoItemRequest item
                    : items
            ) {

                if (item == null) {
                    continue;
                }

                ps.setInt(
                        1,
                        codigoPresupuesto
                );

                ps.setString(
                        2,
                        texto(item.codigoArticulo)
                );

                ps.setString(
                        3,
                        texto(item.descripcionArticulo)
                );

                ps.setBigDecimal(
                        4,
                        decimal(item.cantidad)
                );

                ps.setBigDecimal(
                        5,
                        decimal(item.precioLista)
                );

                ps.setBigDecimal(
                        6,
                        decimal(item.bonificacion)
                );

                ps.setBigDecimal(
                        7,
                        decimal(item.precio)
                );

                ps.setBigDecimal(
                        8,
                        decimal(item.iva)
                );

                ps.setInt(
                        9,
                        item.proveedor
                );

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER PRESUPUESTO
    // -------------------------------------------------------------------------

    public TicketDTO.PresupuestoDTO obtenerPresupuesto(
            int codigoPresupuesto
    ) throws SQLException {

        if (codigoPresupuesto <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    pres.codigo,
                    pres.fecha,
                    pres.cliente,
                    cli.razonsocial AS nombrecliente,
                    pres.usuario,
                    pres.listaprecios,
                    pres.total,
                    pres.vencimiento,
                    pres.estado,
                    pres.observacion
                FROM util.ventaspresupuestocab pres
                LEFT JOIN util.cliente cli
                       ON cli.codigo = pres.cliente
                WHERE pres.codigo = ?
                """;

        try (Connection cn = dataSource.getConnection()) {

            TicketDTO.PresupuestoDTO dto;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setInt(
                        1,
                        codigoPresupuesto
                );

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return null;
                    }

                    dto = new TicketDTO.PresupuestoDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.cliente =
                            rs.getInt("cliente");

                    dto.nombreCliente =
                            rs.getString("nombrecliente");

                    dto.usuario =
                            rs.getInt("usuario");

                    dto.listaPrecios =
                            rs.getInt("listaprecios");

                    dto.total =
                            rs.getDouble("total");

                    dto.vencimiento =
                            rs.getTimestamp("vencimiento");

                    dto.estado =
                            rs.getInt("estado");

                    dto.observacion =
                            rs.getString("observacion");
                }
            }

            dto.items =
                    obtenerItems(
                            cn,
                            codigoPresupuesto
                    );

            return dto;
        }
    }

    private List<TicketDTO.PresupuestoItemDTO> obtenerItems(
            Connection cn,
            int codigoPresupuesto
    ) throws SQLException {

        List<TicketDTO.PresupuestoItemDTO> result =
                new ArrayList<>();

        String sql = """
                SELECT
                    codigo,
                    ventaspresupuestocab,
                    codigoarticulo,
                    descripcionarticulo,
                    cantidad,
                    preciolista,
                    bonificacion,
                    precio,
                    iva,
                    COALESCE(proveedor, 0) AS proveedor
                FROM util.ventaspresupuestoreng
                WHERE ventaspresupuestocab = ?
                ORDER BY codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoPresupuesto
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.PresupuestoItemDTO dto =
                            new TicketDTO.PresupuestoItemDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.presupuesto =
                            rs.getInt(
                                    "ventaspresupuestocab"
                            );

                    dto.codigoArticulo =
                            rs.getString("codigoarticulo");

                    dto.descripcionArticulo =
                            rs.getString(
                                    "descripcionarticulo"
                            );

                    dto.cantidad =
                            rs.getDouble("cantidad");

                    dto.precioLista =
                            rs.getDouble("preciolista");

                    dto.bonificacion =
                            rs.getDouble("bonificacion");

                    dto.precio =
                            rs.getDouble("precio");

                    dto.iva =
                            rs.getDouble("iva");

                    dto.proveedor =
                            rs.getInt("proveedor");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // OBTENER CLIENTE
    // -------------------------------------------------------------------------

    public TicketDTO.ClientePresupuestoDTO obtenerCliente(
            int codigoPresupuesto
    ) throws SQLException {

        if (codigoPresupuesto <= 0) {
            return null;
        }

        String sql = """
                SELECT cliente
                FROM util.ventaspresupuestocab
                WHERE codigo = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoPresupuesto
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                TicketDTO.ClientePresupuestoDTO dto =
                        new TicketDTO.ClientePresupuestoDTO();

                dto.codigoCliente =
                        rs.getInt("cliente");

                return dto;
            }
        }
    }

    // -------------------------------------------------------------------------
    // LISTAR
    // -------------------------------------------------------------------------

    public List<TicketDTO.ResumenPresupuestoDTO> listarPresupuestos(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<TicketDTO.ResumenPresupuestoDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    pres.codigo,
                    pres.fecha,
                    pres.cliente,
                    cli.razonsocial,
                    pres.usuario,
                    pres.listaprecios,
                    pres.total,
                    pres.vencimiento,
                    pres.estado,
                    pres.observacion
                FROM util.ventaspresupuestocab pres
                INNER JOIN util.cliente cli
                        ON cli.codigo = pres.cliente
                WHERE CAST(pres.fecha AS DATE) >= ?
                  AND CAST(pres.fecha AS DATE) <= ?
                ORDER BY
                    pres.fecha DESC,
                    pres.codigo DESC
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

                    TicketDTO.ResumenPresupuestoDTO dto =
                            new TicketDTO.ResumenPresupuestoDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.cliente =
                            rs.getInt("cliente");

                    dto.razonSocial =
                            rs.getString("razonsocial");

                    dto.usuario =
                            rs.getInt("usuario");

                    dto.listaPrecios =
                            rs.getInt("listaprecios");

                    dto.total =
                            rs.getDouble("total");

                    dto.vencimiento =
                            rs.getTimestamp("vencimiento");

                    dto.estado =
                            rs.getInt("estado");

                    dto.observacion =
                            rs.getString("observacion");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // ELIMINAR
    // -------------------------------------------------------------------------

    public boolean eliminarPresupuesto(
            int codigoPresupuesto
    ) {

        if (codigoPresupuesto <= 0) {
            return false;
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                eliminarPresupuestoAnterior(
                        cn,
                        codigoPresupuesto
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

    private void eliminarPresupuestoAnterior(
            Connection cn,
            int codigoPresupuesto
    ) throws SQLException {

        ejecutarDelete(
                cn,
                """
                DELETE FROM util.ventaspresupuestoreng
                WHERE ventaspresupuestocab = ?
                """,
                codigoPresupuesto
        );

        ejecutarDelete(
                cn,
                """
                DELETE FROM util.ventaspresupuestocab
                WHERE codigo = ?
                """,
                codigoPresupuesto
        );
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