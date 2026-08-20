package com.util.api.configuracion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ConfiguracionRepository {

    @Inject
    DataSource dataSource;

    public List<ConfiguracionDTO> obtenerTodas()
            throws SQLException {

        List<ConfiguracionDTO> configuraciones =
                new ArrayList<>();

        String sql = """
                SELECT
                    codigo,
                    valor
                FROM util.configuracion
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
                configuraciones.add(
                        mapConfiguracion(rs)
                );
            }
        }

        return configuraciones;
    }

    public ConfiguracionDTO obtener(
            String clave
    ) throws SQLException {

        if (!claveValida(clave)) {
            return null;
        }

        String sql = """
                SELECT
                    codigo,
                    valor
                FROM util.configuracion
                WHERE codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(
                    1,
                    clave.trim()
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConfiguracion(rs);
                }
            }
        }

        return null;
    }

    public boolean guardar(
            String clave,
            String valor
    ) throws SQLException {

        if (!claveValida(clave)) {
            return false;
        }

        String sql = """
                INSERT INTO util.configuracion (
                    codigo,
                    valor
                )
                VALUES (?, ?)
                ON CONFLICT (codigo)
                DO UPDATE SET
                    valor = EXCLUDED.valor
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(
                    1,
                    clave.trim()
            );

            /*
             * Replica el comportamiento del DBDAO:
             * guardar individual convierte null en cadena vacía.
             */
            ps.setString(
                    2,
                    valor == null ? "" : valor
            );

            return ps.executeUpdate() == 1;
        }
    }

    public boolean guardarTodas(
            Map<String, String> configuraciones
    ) throws SQLException {

        if (configuraciones == null
                || configuraciones.isEmpty()) {

            return true;
        }

        String sql = """
                INSERT INTO util.configuracion (
                    codigo,
                    valor
                )
                VALUES (?, ?)
                ON CONFLICT (codigo)
                DO UPDATE SET
                    valor = EXCLUDED.valor
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                try (
                        PreparedStatement ps =
                                connection.prepareStatement(sql)
                ) {
                    for (
                            Map.Entry<String, String> entry
                            : configuraciones.entrySet()
                    ) {
                        ps.setString(
                                1,
                                entry.getKey()
                        );

                        /*
                         * En guardarTodas el DBDAO conserva null,
                         * por lo tanto no se convierte en "".
                         */
                        ps.setString(
                                2,
                                entry.getValue()
                        );

                        ps.addBatch();
                    }

                    int[] resultados =
                            ps.executeBatch();

                    if (!batchExitoso(resultados)) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();

                return true;

            } catch (SQLException e) {
                rollbackSilencioso(connection);
                throw e;

            } finally {
                restaurarAutoCommit(
                        connection,
                        autoCommitOriginal
                );
            }
        }
    }

    public boolean eliminar(
            String clave
    ) throws SQLException {

        if (!claveValida(clave)) {
            return false;
        }

        String sql = """
                DELETE FROM util.configuracion
                WHERE codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(
                    1,
                    clave.trim()
            );

            return ps.executeUpdate() == 1;
        }
    }

    private ConfiguracionDTO mapConfiguracion(
            ResultSet rs
    ) throws SQLException {

        return new ConfiguracionDTO(
                rs.getString("codigo"),
                rs.getString("valor")
        );
    }

    private boolean claveValida(
            String clave
    ) {
        return clave != null
                && !clave.trim().isEmpty();
    }

    private boolean batchExitoso(
            int[] resultados
    ) {
        if (resultados == null) {
            return false;
        }

        for (int resultado : resultados) {
            if (resultado == Statement.EXECUTE_FAILED) {
                return false;
            }
        }

        return true;
    }

    private void rollbackSilencioso(
            Connection connection
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void restaurarAutoCommit(
            Connection connection,
            boolean autoCommitOriginal
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(
                    autoCommitOriginal
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}