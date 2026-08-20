package com.util.api.unidadventa;

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
public class UnidadVentaRepository {

    @Inject
    DataSource dataSource;

    public int insertar(
            String nombre
    ) throws SQLException {

        if (nombre == null
                || nombre.trim().isEmpty()) {
            return 0;
        }

        String sql = """
                INSERT INTO util.unidadventa (
                    nombre
                )
                VALUES (?)
                RETURNING codigo
                """;

        try (
                Connection cn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    nombre.trim()
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("codigo");
                }
            }
        }

        return 0;
    }

    public List<UnidadVentaDTO> obtenerTodos()
            throws SQLException {

        List<UnidadVentaDTO> resultado =
                new ArrayList<>();

        String sql = """
                SELECT
                    uv.codigo,
                    uv.nombre
                FROM util.unidadventa uv
                ORDER BY uv.nombre
                """;

        try (
                Connection cn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        cn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                UnidadVentaDTO dto =
                        new UnidadVentaDTO();

                dto.codigo =
                        rs.getInt("codigo");

                dto.nombre =
                        rs.getString("nombre");

                resultado.add(dto);
            }
        }

        return resultado;
    }
}