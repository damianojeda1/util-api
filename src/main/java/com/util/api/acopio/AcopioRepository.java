package com.util.api.acopio;

import com.util.api.ticket.TicketDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AcopioRepository {

    @Inject
    DataSource dataSource;

    public int guardarAcopio(
            Connection cn,
            int codigoTicket,
            TicketDTO.GuardarTicketRequest ticket
    ) throws SQLException {

        if (cn == null) {
            throw new SQLException(
                    "La conexión es obligatoria"
            );
        }

        if (ticket == null) {
            throw new SQLException(
                    "Los datos del ticket son obligatorios"
            );
        }

        if (ticket.cliente <= 0) {
            throw new SQLException(
                    "El cliente es obligatorio"
            );
        }

        if (ticket.items == null
                || ticket.items.isEmpty()) {

            throw new SQLException(
                    "El acopio no contiene artículos"
            );
        }

        int codigoAcopio =
                insertarAcopio(
                        cn,
                        codigoTicket,
                        ticket
                );

        insertarDetalles(
                cn,
                codigoAcopio,
                ticket.items
        );

        return codigoAcopio;
    }

    private int insertarAcopio(
            Connection cn,
            int codigoTicket,
            TicketDTO.GuardarTicketRequest ticket
    ) throws SQLException {

        String sql = """
                INSERT INTO util.acopio (
                    cliente,
                    fecha,
                    usuario,
                    observaciones,
                    estado,
                    ticket
                )
                VALUES (
                    ?,
                    NOW(),
                    ?,
                    ?,
                    'ACTIVO',
                    ?
                )
                RETURNING id
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    ticket.cliente
            );

            ps.setInt(
                    2,
                    ticket.vendedor
            );

            ps.setString(
                    3,
                    texto(ticket.observacion)
            );

            ps.setInt(
                    4,
                    codigoTicket
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el código del acopio"
                    );
                }

                return rs.getInt("id");
            }
        }
    }

    private void insertarDetalles(
            Connection cn,
            int codigoAcopio,
            List<TicketDTO.ItemRequest> items
    ) throws SQLException {

        String sql = """
                INSERT INTO util.acopiodetalle (
                    acopio,
                    articulo,
                    descripcion,
                    cantidad,
                    precio
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            for (TicketDTO.ItemRequest item : items) {

                if (item == null) {
                    continue;
                }

                String codigoArticulo =
                        texto(item.codigoArticulo).trim();

                if (codigoArticulo.isEmpty()
                        || "0".equals(codigoArticulo)) {
                    continue;
                }

                ps.setInt(
                        1,
                        codigoAcopio
                );

                ps.setString(
                        2,
                        codigoArticulo
                );

                ps.setString(
                        3,
                        texto(item.descripcion)
                );

                ps.setBigDecimal(
                        4,
                        decimal(item.cantidad)
                );

                ps.setBigDecimal(
                        5,
                        decimal(item.precio)
                );

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    public List<AcopioDTO.ResumenDTO> listarPorCliente(
            int codigoCliente
    ) throws SQLException {

        List<AcopioDTO.ResumenDTO> resultado =
                new java.util.ArrayList<>();

        if (codigoCliente <= 1) {
            return resultado;
        }

        String sql = """
            SELECT
                a.id,
                a.ticket,
                a.cliente,
                a.fecha,
                a.usuario,
                a.observaciones,
                a.estado,

                COALESCE(
                    SUM(ad.cantidad),
                    0
                ) AS cantidad_original,

                COALESCE((
                    SELECT SUM(ard.cantidad)
                    FROM util.acopioretirodetalle ard
                    INNER JOIN util.acopiodetalle ad2
                            ON ad2.id = ard.acopiodetalle
                    WHERE ad2.acopio = a.id
                ), 0) AS cantidad_retirada

            FROM util.acopio a

            LEFT JOIN util.acopiodetalle ad
                   ON ad.acopio = a.id

            WHERE a.cliente = ?

            GROUP BY
                a.id,
                a.ticket,
                a.cliente,
                a.fecha,
                a.usuario,
                a.observaciones,
                a.estado

            ORDER BY
                a.fecha DESC,
                a.id DESC
            """;

        try (
                Connection cn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoCliente
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    AcopioDTO.ResumenDTO dto =
                            new AcopioDTO.ResumenDTO();

                    dto.id =
                            rs.getInt("id");

                    dto.ticket =
                            rs.getInt("ticket");

                    dto.cliente =
                            rs.getInt("cliente");

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.usuario =
                            rs.getInt("usuario");

                    dto.observaciones =
                            rs.getString("observaciones");

                    dto.estado =
                            rs.getString("estado");

                    dto.cantidadOriginal =
                            rs.getDouble(
                                    "cantidad_original"
                            );

                    dto.cantidadRetirada =
                            rs.getDouble(
                                    "cantidad_retirada"
                            );

                    dto.cantidadPendiente =
                            dto.cantidadOriginal
                                    - dto.cantidadRetirada;

                    resultado.add(dto);
                }
            }
        }

        return resultado;
    }

    public List<AcopioDTO.DetalleDTO> obtenerDetalle(
            int idAcopio
    ) throws SQLException {

        List<AcopioDTO.DetalleDTO> resultado =
                new ArrayList<>();

        if (idAcopio <= 0) {
            return resultado;
        }

        String sql = """
            SELECT
                ad.id,
                ad.acopio,
                ad.articulo,
                ad.descripcion,
                ad.cantidad,
                ad.precio,

                COALESCE((
                    SELECT SUM(ard.cantidad)
                    FROM util.acopioretirodetalle ard
                    WHERE ard.acopiodetalle = ad.id
                ), 0) AS cantidad_retirada

            FROM util.acopiodetalle ad

            WHERE ad.acopio = ?

            ORDER BY ad.id
            """;

        try (
                Connection cn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idAcopio
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    AcopioDTO.DetalleDTO dto =
                            new AcopioDTO.DetalleDTO();

                    dto.id =
                            rs.getInt("id");

                    dto.acopio =
                            rs.getInt("acopio");

                    dto.articulo =
                            rs.getString("articulo");

                    dto.descripcion =
                            rs.getString("descripcion");

                    dto.cantidad =
                            rs.getDouble("cantidad");

                    dto.precio =
                            rs.getDouble("precio");

                    dto.cantidadRetirada =
                            rs.getDouble(
                                    "cantidad_retirada"
                            );

                    dto.cantidadPendiente =
                            dto.cantidad
                                    - dto.cantidadRetirada;

                    resultado.add(dto);
                }
            }
        }

        return resultado;
    }

    public AcopioDTO.GuardarRetiroResponse guardarRetiro(
            AcopioDTO.GuardarRetiroRequest request
    ) {

        if (request == null) {
            return AcopioDTO.GuardarRetiroResponse.error(
                    "Los datos del retiro son obligatorios"
            );
        }

        if (request.acopio <= 0) {
            return AcopioDTO.GuardarRetiroResponse.error(
                    "El acopio es obligatorio"
            );
        }

        if (request.detalles == null
                || request.detalles.isEmpty()) {

            return AcopioDTO.GuardarRetiroResponse.error(
                    "El retiro no contiene artículos"
            );
        }

        try (Connection cn =
                     dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {

                cn.setAutoCommit(false);

                int idRetiro =
                        insertarRetiro(
                                cn,
                                request
                        );

                for (AcopioDTO.RetiroDetalleRequest detalle
                        : request.detalles) {

                    if (detalle == null
                            || detalle.cantidad <= 0) {
                        continue;
                    }

                    procesarDetalleRetiro(
                            cn,
                            idRetiro,
                            request.acopio,
                            detalle
                    );
                }

                cn.commit();

                return AcopioDTO.GuardarRetiroResponse.ok(
                        idRetiro
                );

            } catch (Exception ex) {

                try {
                    cn.rollback();
                } catch (Exception ignored) {
                }

                ex.printStackTrace();

                return AcopioDTO.GuardarRetiroResponse.error(
                        ex.getMessage() == null
                                ? "Error guardando retiro"
                                : ex.getMessage()
                );

            } finally {

                try {
                    cn.setAutoCommit(
                            autoCommitOriginal
                    );
                } catch (Exception ignored) {
                }
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            return AcopioDTO.GuardarRetiroResponse.error(
                    ex.getMessage() == null
                            ? "Error obteniendo conexión"
                            : ex.getMessage()
            );
        }
    }

    private int insertarRetiro(
            Connection cn,
            AcopioDTO.GuardarRetiroRequest request
    ) throws SQLException {

        String sql = """
        INSERT INTO util.acopioretiro (
            acopio,
            fecha,
            usuario,
            observaciones
        )
        VALUES (?, now(), ?, ?)
        RETURNING id
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.acopio
            );

            ps.setInt(
                    2,
                    request.usuario
            );

            ps.setString(
                    3,
                    texto(request.observaciones)
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo generar el retiro"
                    );
                }

                return rs.getInt("id");
            }
        }
    }

    private void procesarDetalleRetiro(
            Connection cn,
            int idRetiro,
            int idAcopio,
            AcopioDTO.RetiroDetalleRequest request
    ) throws SQLException {

        DetalleRetiroDB detalle =
                obtenerDetalleParaRetiro(
                        cn,
                        idAcopio,
                        request.acopioDetalle
                );

        if (detalle == null) {
            throw new SQLException(
                    "No se encontró el artículo del acopio"
            );
        }

        if (request.cantidad
                > detalle.pendiente + 0.000001) {

            throw new SQLException(
                    "La cantidad solicitada de "
                            + detalle.descripcion
                            + " supera la cantidad pendiente"
            );
        }

        insertarDetalleRetiro(
                cn,
                idRetiro,
                request.acopioDetalle,
                request.cantidad
        );

        descontarStock(
                cn,
                detalle.articulo,
                request.cantidad
        );
    }

    private DetalleRetiroDB obtenerDetalleParaRetiro(
            Connection cn,
            int idAcopio,
            int idAcopioDetalle
    ) throws SQLException {

        String sql = """
        SELECT
            ad.articulo,
            ad.descripcion,
            ad.cantidad - COALESCE((
                SELECT SUM(ard.cantidad)
                FROM util.acopioretirodetalle ard
                WHERE ard.acopiodetalle = ad.id
            ), 0) AS pendiente
        FROM util.acopiodetalle ad
        WHERE ad.id = ?
          AND ad.acopio = ?
        FOR UPDATE
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    idAcopioDetalle
            );

            ps.setInt(
                    2,
                    idAcopio
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                DetalleRetiroDB detalle =
                        new DetalleRetiroDB();

                detalle.articulo =
                        rs.getString("articulo");

                detalle.descripcion =
                        rs.getString("descripcion");

                detalle.pendiente =
                        rs.getDouble("pendiente");

                return detalle;
            }
        }
    }

    private void insertarDetalleRetiro(
            Connection cn,
            int idRetiro,
            int idAcopioDetalle,
            double cantidad
    ) throws SQLException {

        String sql = """
        INSERT INTO util.acopioretirodetalle (
            retiro,
            acopiodetalle,
            cantidad
        )
        VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    idRetiro
            );

            ps.setInt(
                    2,
                    idAcopioDetalle
            );

            ps.setBigDecimal(
                    3,
                    BigDecimal.valueOf(cantidad)
            );

            ps.executeUpdate();
        }
    }

    private void descontarStock(
            Connection cn,
            String codigoArticulo,
            double cantidad
    ) throws SQLException {

        BigDecimal restante =
                BigDecimal.valueOf(cantidad);

        String sql = """
        SELECT
            codigo,
            proveedor,
            stock
        FROM util.articulo
        WHERE codigo = ?
        ORDER BY
            stock DESC,
            fechaactualizacion DESC,
            proveedor
        FOR UPDATE
        """;

        Integer proveedorFallback = null;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    codigoArticulo
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    int proveedor =
                            rs.getInt("proveedor");

                    BigDecimal stock =
                            rs.getBigDecimal("stock");

                    if (stock == null) {
                        stock = BigDecimal.ZERO;
                    }

                    if (proveedorFallback == null) {
                        proveedorFallback =
                                proveedor;
                    }

                    if (restante.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {
                        break;
                    }

                    if (stock.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {
                        continue;
                    }

                    BigDecimal descontar =
                            stock.min(restante);

                    actualizarStock(
                            cn,
                            codigoArticulo,
                            proveedor,
                            descontar
                    );

                    restante =
                            restante.subtract(
                                    descontar
                            );
                }
            }
        }

        if (restante.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            if (proveedorFallback == null) {
                throw new SQLException(
                        "No existe el artículo "
                                + codigoArticulo
                );
            }

            actualizarStock(
                    cn,
                    codigoArticulo,
                    proveedorFallback,
                    restante
            );
        }
    }

    private void actualizarStock(
            Connection cn,
            String codigoArticulo,
            int proveedor,
            BigDecimal cantidad
    ) throws SQLException {

        String sql = """
        UPDATE util.articulo
        SET stock = stock - ?,
            fechaactualizacion = now()
        WHERE codigo = ?
          AND proveedor = ?
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setBigDecimal(
                    1,
                    cantidad
            );

            ps.setString(
                    2,
                    codigoArticulo
            );

            ps.setInt(
                    3,
                    proveedor
            );

            ps.executeUpdate();
        }
    }

    private static class DetalleRetiroDB {

        String articulo;
        String descripcion;
        double pendiente;
    }

    private BigDecimal decimal(
            double value
    ) {
        return BigDecimal.valueOf(value);
    }

    private String texto(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }
}