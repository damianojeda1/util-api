package com.util.api.acopio;

import com.util.api.ticket.TicketDTO;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class AcopioRepository {

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