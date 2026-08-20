package com.util.api.categoriaarticulo;

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
public class CategoriaArticuloRepository {

    private static final int CATEGORIA_SIN_CATEGORIA = 1;

    @Inject
    DataSource dataSource;

    public CategoriaArticuloDTO findById(
            int codigo
    ) throws SQLException {

        String sql = """
                SELECT
                    cat.codigo,
                    cat.nombre
                FROM util.categoriaarticulo cat
                WHERE cat.codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategoria(rs);
                }
            }
        }

        return null;
    }

    public CategoriaArticuloDTO buscarPorDescripcion(
            String nombreCategoria
    ) throws SQLException {

        if (nombreCategoria == null
                || nombreCategoria.trim().isEmpty()
                || nombreCategoria.trim().equals("0")) {

            return categoriaPorDefecto();
        }

        String sql = """
                SELECT
                    cat.codigo,
                    cat.nombre
                FROM util.categoriaarticulo cat
                WHERE LOWER(TRIM(cat.nombre)) =
                      LOWER(TRIM(?))
                LIMIT 1
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(
                    1,
                    nombreCategoria.trim()
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategoria(rs);
                }
            }
        }

        return categoriaPorDefecto();
    }

    public boolean insert(
            CategoriaArticuloDTO categoria
    ) throws SQLException {

        if (!esValida(categoria)) {
            return false;
        }

        String sql = """
                INSERT INTO util.categoriaarticulo (
                    nombre
                )
                VALUES (?)
                """;

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(
                    1,
                    categoria.nombre.trim()
            );

            return ps.executeUpdate() == 1;
        }
    }

    public boolean update(
            CategoriaArticuloDTO categoria
    ) throws SQLException {

        if (!esValida(categoria)) {
            return false;
        }

        if (categoria.codigo == CATEGORIA_SIN_CATEGORIA) {
            return false;
        }

        String sql = """
                UPDATE util.categoriaarticulo
                SET nombre = ?
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
                    categoria.nombre.trim()
            );

            ps.setInt(
                    2,
                    categoria.codigo
            );

            return ps.executeUpdate() == 1;
        }
    }

    public List<CategoriaArticuloDTO>
    obtenerCompletos() throws SQLException {

        List<CategoriaArticuloDTO> resultado =
                new ArrayList<>();

        String sql = """
                SELECT
                    cat.codigo,
                    cat.nombre
                FROM util.categoriaarticulo cat
                ORDER BY cat.nombre
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
                        mapCategoria(rs)
                );
            }
        }

        return resultado;
    }

    public boolean delete(
            int codigo
    ) throws SQLException {

        if (codigo == CATEGORIA_SIN_CATEGORIA) {
            return false;
        }

        String sqlActualizarArticulos = """
                UPDATE util.articulo
                SET categoria = ?
                WHERE categoria = ?
                """;

        String sqlEliminarCategoria = """
                DELETE FROM util.categoriaarticulo
                WHERE codigo = ?
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
                                connection.prepareStatement(
                                        sqlActualizarArticulos
                                )
                ) {
                    ps.setInt(
                            1,
                            CATEGORIA_SIN_CATEGORIA
                    );

                    ps.setInt(
                            2,
                            codigo
                    );

                    ps.executeUpdate();
                }

                int eliminadas;

                try (
                        PreparedStatement ps =
                                connection.prepareStatement(
                                        sqlEliminarCategoria
                                )
                ) {
                    ps.setInt(
                            1,
                            codigo
                    );

                    eliminadas =
                            ps.executeUpdate();
                }

                if (eliminadas != 1) {
                    connection.rollback();
                    return false;
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

    private CategoriaArticuloDTO mapCategoria(
            ResultSet rs
    ) throws SQLException {

        return new CategoriaArticuloDTO(
                rs.getInt("codigo"),
                rs.getString("nombre")
        );
    }

    private boolean esValida(
            CategoriaArticuloDTO categoria
    ) {
        return categoria != null
                && categoria.nombre != null
                && !categoria.nombre.trim().isEmpty();
    }

    private CategoriaArticuloDTO categoriaPorDefecto() {
        return new CategoriaArticuloDTO(
                CATEGORIA_SIN_CATEGORIA,
                ""
        );
    }

    private void rollbackSilencioso(
            Connection connection
    ) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            rollbackException.printStackTrace();
        }
    }

    private void restaurarAutoCommit(
            Connection connection,
            boolean autoCommitOriginal
    ) {
        try {
            connection.setAutoCommit(
                    autoCommitOriginal
            );
        } catch (SQLException autoCommitException) {
            autoCommitException.printStackTrace();
        }
    }
}