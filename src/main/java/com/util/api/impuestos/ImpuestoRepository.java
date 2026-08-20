package com.util.api.impuestos;

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
public class ImpuestoRepository {

    @Inject
    DataSource dataSource;

    public List<ImpuestoDTO> obtenerTodos()
            throws SQLException {

        List<ImpuestoDTO> resultado =
                new ArrayList<>();

        String sql = """
                SELECT
                    imp.codigo,
                    imp.alicuota,
                    imp.nombre
                FROM util.impuesto imp
                ORDER BY imp.nombre
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

                ImpuestoDTO dto =
                        new ImpuestoDTO();

                dto.codigo =
                        rs.getInt("codigo");

                /*
                 * Lo mantenemos como String porque así
                 * lo espera hoy el Desktop al construir Tripla.
                 */
                dto.alicuota =
                        rs.getDouble("alicuota");

                dto.nombre =
                        rs.getString("nombre");

                resultado.add(dto);
            }
        }

        return resultado;
    }
}