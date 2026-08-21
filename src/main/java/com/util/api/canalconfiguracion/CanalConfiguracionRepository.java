package com.util.api.canalconfiguracion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@ApplicationScoped
public class CanalConfiguracionRepository {

    @Inject
    DataSource dataSource;

    public CanalConfiguracionDTO obtener(
            String canal
    ) throws SQLException {

        String sql = """
                SELECT codigo,
                       canal,
                       storeId,
                       accessToken,
                       activo,
                       fechaVinculacion
                FROM util.canalConfiguracion
                WHERE canal = ?
                """;

        try (
                Connection conn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    canal
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next()) {
                    return null;
                }

                return mapear(rs);
            }
        }
    }

    public boolean guardar(
            CanalConfiguracionDTO dto
    ) throws SQLException {

        String sql = """
                INSERT INTO util.canalConfiguracion
                (
                    canal,
                    storeId,
                    accessToken,
                    activo,
                    fechaVinculacion
                )
                VALUES (?, ?, ?, ?, ?)

                ON CONFLICT (canal)
                DO UPDATE SET
                    storeId = EXCLUDED.storeId,
                    accessToken = EXCLUDED.accessToken,
                    activo = EXCLUDED.activo,
                    fechaVinculacion = EXCLUDED.fechaVinculacion
                """;

        try (
                Connection conn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    dto.canal
            );

            ps.setString(
                    2,
                    dto.storeId
            );

            ps.setString(
                    3,
                    dto.accessToken
            );

            ps.setBoolean(
                    4,
                    dto.activo
            );

            if (dto.fechaVinculacion != null) {

                ps.setTimestamp(
                        5,
                        Timestamp.valueOf(
                                dto.fechaVinculacion
                        )
                );

            } else {

                ps.setTimestamp(
                        5,
                        null
                );
            }

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(
            String canal
    ) throws SQLException {

        String sql = """
                DELETE FROM util.canalConfiguracion
                WHERE canal = ?
                """;

        try (
                Connection conn =
                        dataSource.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    canal
            );

            return ps.executeUpdate() > 0;
        }
    }

    private CanalConfiguracionDTO mapear(
            ResultSet rs
    ) throws SQLException {

        CanalConfiguracionDTO dto =
                new CanalConfiguracionDTO();

        dto.codigo =
                rs.getInt(
                        "codigo"
                );

        dto.canal =
                rs.getString(
                        "canal"
                );

        dto.storeId =
                rs.getString(
                        "storeId"
                );

        dto.accessToken =
                rs.getString(
                        "accessToken"
                );

        dto.activo =
                rs.getBoolean(
                        "activo"
                );

        Timestamp fecha =
                rs.getTimestamp(
                        "fechaVinculacion"
                );

        if (fecha != null) {
            dto.fechaVinculacion =
                    fecha.toLocalDateTime();
        }

        return dto;
    }
}