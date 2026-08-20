package com.util.api.tipocomprobante;

import com.util.api.ticket.TicketDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class TipoComprobanteRepository {

    @Inject
    DataSource dataSource;

    public TicketDTO.TipoComprobanteDTO obtenerPorCodigo(
            String codigo
    ) throws SQLException {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        int codigoNumerico;

        try {
            codigoNumerico =
                    Integer.parseInt(codigo.trim());

        } catch (NumberFormatException ex) {
            return null;
        }

        String sql = """
                SELECT
                    codigo,
                    descripcion,
                    letra,
                    impacto
                FROM util.tipocomprobante
                WHERE codigo = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoNumerico
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                TicketDTO.TipoComprobanteDTO dto =
                        new TicketDTO.TipoComprobanteDTO();

                dto.codigo =
                        rs.getString("codigo");

                dto.descripcion =
                        rs.getString("descripcion");

                dto.letra =
                        rs.getString("letra");

                dto.impacto =
                        rs.getInt("impacto");

                return dto;
            }
        }
    }
}