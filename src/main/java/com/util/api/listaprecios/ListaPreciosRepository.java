package com.util.api.listaprecios;

import com.util.api.generico.ResInsertUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ListaPreciosRepository {

    private static final String SELECT_COMPLETAS = """
            SELECT lista.codigo,
                   lista.descripcion,
                   lista.porcentaje
            FROM util.listaprecios lista
            ORDER BY lista.codigo ASC
            """;

    private static final String SELECT_POR_ID = """
            SELECT lista.codigo,
                   lista.descripcion,
                   lista.porcentaje
            FROM util.listaprecios lista
            WHERE lista.codigo = ?
            """;

    private static final String INSERT = """
            INSERT INTO util.listaprecios (
                descripcion,
                porcentaje
            )
            VALUES (?, ?)
            """;

    private static final String UPDATE = """
            UPDATE util.listaprecios
            SET descripcion = ?,
                porcentaje = ?
            WHERE codigo = ?
            """;

    private static final String SELECT_SIGUIENTE_CODIGO = """
            SELECT COALESCE(MAX(codigo), 0) + 1 AS nro
            FROM util.listaprecios
            """;

    @Inject
    DataSource dataSource;

    public List<ListaPreciosDTO.Response> obtenerCompletas()
            throws SQLException {

        List<ListaPreciosDTO.Response> resultado =
                new ArrayList<>();

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(SELECT_COMPLETAS);

                ResultSet rs =
                        ps.executeQuery()
        ) {
            while (rs.next()) {
                resultado.add(mapListaPrecios(rs));
            }
        }

        return resultado;
    }

    public ListaPreciosDTO.Response findById(
            int codigo
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(SELECT_POR_ID)
        ) {
            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapListaPrecios(rs);
                }
            }
        }

        return null;
    }

    public ResInsertUpdate insert(
            ListaPreciosDTO.CrearRequest request
    ) {
        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(INSERT)
        ) {
            ps.setString(
                    1,
                    request.getDescripcion()
            );

            ps.setDouble(
                    2,
                    request.getPorcentaje()
            );

            int filasInsertadas =
                    ps.executeUpdate();

            return filasInsertadas == 1
                    ? resultadoCorrecto()
                    : resultadoError();

        } catch (PSQLException ex) {
            registrarError(
                    "Error PostgreSQL insertando lista de precios",
                    ex
            );

            return new ResInsertUpdate(
                    ex.getSQLState(),
                    false
            );

        } catch (SQLException ex) {
            registrarError(
                    "Error SQL insertando lista de precios",
                    ex
            );

            return new ResInsertUpdate(
                    ex.getSQLState(),
                    false
            );

        } catch (Exception ex) {
            registrarError(
                    "Error inesperado insertando lista de precios",
                    ex
            );

            return resultadoError();
        }
    }

    public ResInsertUpdate update(
            int codigo,
            ListaPreciosDTO.ActualizarRequest request
    ) {
        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(UPDATE)
        ) {
            ps.setString(
                    1,
                    request.getDescripcion()
            );

            ps.setDouble(
                    2,
                    request.getPorcentaje()
            );

            ps.setInt(
                    3,
                    codigo
            );

            int filasActualizadas =
                    ps.executeUpdate();

            return filasActualizadas == 1
                    ? resultadoCorrecto()
                    : resultadoError();

        } catch (PSQLException ex) {
            registrarError(
                    "Error PostgreSQL actualizando lista de precios "
                            + codigo,
                    ex
            );

            return new ResInsertUpdate(
                    ex.getSQLState(),
                    false
            );

        } catch (SQLException ex) {
            registrarError(
                    "Error SQL actualizando lista de precios "
                            + codigo,
                    ex
            );

            return new ResInsertUpdate(
                    ex.getSQLState(),
                    false
            );

        } catch (Exception ex) {
            registrarError(
                    "Error inesperado actualizando lista de precios "
                            + codigo,
                    ex
            );

            return resultadoError();
        }
    }

    public int nroNuevaLista() throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(
                                SELECT_SIGUIENTE_CODIGO
                        );

                ResultSet rs =
                        ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("nro");
            }
        }

        return -1;
    }

    private ListaPreciosDTO.Response mapListaPrecios(
            ResultSet rs
    ) throws SQLException {

        return new ListaPreciosDTO.Response(
                rs.getInt("codigo"),
                rs.getString("descripcion"),
                rs.getDouble("porcentaje")
        );
    }

    private ResInsertUpdate resultadoCorrecto() {
        return new ResInsertUpdate(
                "00000",
                true
        );
    }

    private ResInsertUpdate resultadoError() {
        return new ResInsertUpdate(
                "-1",
                false
        );
    }

    private void registrarError(
            String mensaje,
            Exception ex
    ) {
        System.err.println(mensaje);
        ex.printStackTrace();
    }
}