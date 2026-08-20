package com.util.api.articulocanal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticuloCanalRepository {

    @Inject
    DataSource dataSource;

    public ArticuloCanalDTO obtener(
            String codigoArticulo,
            String codigoProveedor,
            String canal
    ) throws SQLException {

        String sql =
                " SELECT codigoarticulo, " +
                        "        codigoproveedor, " +
                        "        canal, " +
                        "        idexterno, " +
                        "        idvarianteexterna, " +
                        "        publicado, " +
                        "        ultimasincronizacion " +
                        " FROM util.articulocanal " +
                        " WHERE codigoarticulo = ? " +
                        "   AND codigoproveedor = ? " +
                        "   AND canal = ? ";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, codigoArticulo);
            ps.setString(2, codigoProveedor);
            ps.setString(3, canal);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return map(rs);
            }
        }
    }

    public List<ArticuloCanalDTO> obtenerPorCanal(
            String canal
    ) throws SQLException {

        List<ArticuloCanalDTO> resultado =
                new ArrayList<>();

        String sql =
                " SELECT codigoarticulo, " +
                        "        codigoproveedor, " +
                        "        canal, " +
                        "        idexterno, " +
                        "        idvarianteexterna, " +
                        "        publicado, " +
                        "        ultimasincronizacion " +
                        " FROM util.articulocanal " +
                        " WHERE canal = ? ";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {

            ps.setString(1, canal);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(map(rs));
                }
            }
        }

        return resultado;
    }

    public boolean guardar(
            ArticuloCanalDTO dto
    ) throws SQLException {

        String sql =
                " INSERT INTO util.articulocanal " +
                        " (codigoarticulo, " +
                        "  codigoproveedor, " +
                        "  canal, " +
                        "  idexterno, " +
                        "  idvarianteexterna, " +
                        "  publicado, " +
                        "  ultimasincronizacion) " +
                        " VALUES (?, ?, ?, ?, ?, ?, NOW()) " +
                        " ON CONFLICT (codigoarticulo, codigoproveedor, canal) " +
                        " DO UPDATE SET " +
                        "   idexterno = EXCLUDED.idexterno, " +
                        "   idvarianteexterna = EXCLUDED.idvarianteexterna, " +
                        "   publicado = EXCLUDED.publicado, " +
                        "   ultimasincronizacion = NOW() ";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, dto.codigoArticulo);
            ps.setString(2, dto.codigoProveedor);
            ps.setString(3, dto.canal);
            ps.setString(4, dto.idExterno);
            ps.setString(5, dto.idVarianteExterna);
            ps.setBoolean(
                    6,
                    dto.publicado != null && dto.publicado
            );

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(
            String codigoArticulo,
            String codigoProveedor,
            String canal
    ) throws SQLException {

        String sql =
                " DELETE FROM util.articulocanal " +
                        " WHERE codigoarticulo = ? " +
                        "   AND codigoproveedor = ? " +
                        "   AND canal = ? ";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, codigoArticulo);
            ps.setString(2, codigoProveedor);
            ps.setString(3, canal);

            return ps.executeUpdate() > 0;
        }
    }

    private ArticuloCanalDTO map(
            ResultSet rs
    ) throws SQLException {

        ArticuloCanalDTO dto =
                new ArticuloCanalDTO();

        dto.codigoArticulo =
                rs.getString("codigoarticulo");

        dto.codigoProveedor =
                rs.getString("codigoproveedor");

        dto.canal =
                rs.getString("canal");

        dto.idExterno =
                rs.getString("idexterno");

        dto.idVarianteExterna =
                rs.getString("idvarianteexterna");

        dto.publicado =
                rs.getBoolean("publicado");

        if (rs.getTimestamp("ultimasincronizacion") != null) {
            dto.ultimaSincronizacion =
                    rs.getTimestamp("ultimasincronizacion")
                            .toLocalDateTime();
        }

        return dto;
    }
}