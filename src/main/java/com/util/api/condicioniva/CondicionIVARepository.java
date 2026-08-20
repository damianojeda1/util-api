package com.util.api.condicioniva;

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
public class CondicionIVARepository {

    @Inject
    DataSource dataSource;

    public List<CondicionIVADTO>
    obtenerHabilitadas() throws SQLException {

        List<CondicionIVADTO> resultado =
                new ArrayList<>();

        String sql = """
                SELECT
                    codigo,
                    descripcion
                FROM util.condicioniva
                WHERE habilitado = TRUE
                ORDER BY descripcion
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {
            while (rs.next()) {
                resultado.add(
                        mapCondicionIVA(rs)
                );
            }
        }

        return resultado;
    }

    private CondicionIVADTO mapCondicionIVA(
            ResultSet rs
    ) throws SQLException {

        return new CondicionIVADTO(
                rs.getInt("codigo"),
                rs.getString("descripcion")
        );
    }
}