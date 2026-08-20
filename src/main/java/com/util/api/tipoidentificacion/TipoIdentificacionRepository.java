package com.util.api.tipoidentificacion;

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
public class TipoIdentificacionRepository {

    @Inject
    DataSource dataSource;

    public List<TipoIdentificacionDTO>
    obtenerParaCombo() throws SQLException {

        List<TipoIdentificacionDTO> resultado =
                new ArrayList<>();

        String sql = """
                SELECT
                    codigo,
                    descripcion
                FROM util.tipoidentificacion
                WHERE habilitado = TRUE
                  AND descripcion IN ('DNI', 'CUIT')
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
                        mapTipoIdentificacion(rs)
                );
            }
        }

        return resultado;
    }

    private TipoIdentificacionDTO mapTipoIdentificacion(
            ResultSet rs
    ) throws SQLException {

        return new TipoIdentificacionDTO(
                rs.getInt("codigo"),
                rs.getString("descripcion")
        );
    }
}