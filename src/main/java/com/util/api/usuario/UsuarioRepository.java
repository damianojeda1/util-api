package com.util.api.usuario;

import com.util.api.generico.ParDTO;
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
public class UsuarioRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // LISTADO
    // -------------------------------------------------------------------------

    public List<UsuarioDTO.Response> obtenerUsuarios(
            boolean soloHabilitados,
            boolean incluirAdmin
    ) throws SQLException {

        List<UsuarioDTO.Response> resultado =
                new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                """
                SELECT
                    usr.codigo,
                    usr.nombre,
                    usr.password,
                    usr.habilitado,
                    usr.mostrarcostos,
                    usr.facturar,
                    nvl.codigo AS codnivel,
                    nvl.descripcion AS nomnivel
                FROM util.usuario usr
                INNER JOIN util.nivel nvl
                        ON usr.nivel = nvl.codigo
                WHERE 1 = 1
                """
        );

        if (!incluirAdmin) {
            sql.append(" AND usr.nivel > 1 ");
        }

        if (soloHabilitados) {
            sql.append(" AND usr.habilitado = TRUE ");
        }

        sql.append(" ORDER BY usr.nombre ");

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps =
                        cn.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                resultado.add(mapearUsuario(rs));
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------------------------

    public boolean login(
            UsuarioDTO.LoginRequest request
    ) throws SQLException {

        if (request == null
                || request.nombreUsuario == null
                || request.password == null) {

            return false;
        }

        String sql = """
                SELECT codigo
                FROM util.usuario
                WHERE habilitado = TRUE
                  AND nombre = ?
                  AND password = ?
                """;

        try (Connection cn = dataSource.getConnection()) {

            Integer codigoUsuario = null;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setString(
                        1,
                        request.nombreUsuario
                );

                ps.setString(
                        2,
                        request.password
                );

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        codigoUsuario =
                                rs.getInt("codigo");
                    }
                }
            }

            if (codigoUsuario != null) {

                if (request.recordar) {
                    recordarUsuario(
                            cn,
                            codigoUsuario,
                            request.terminal
                    );
                }

                return true;
            }

            return validarUsuarioRecordado(
                    cn,
                    request.recordado,
                    request.nombreUsuario,
                    request.terminal
            );
        }
    }

    private boolean validarUsuarioRecordado(
            Connection cn,
            String recordado,
            String nombreUsuario,
            String terminalActual
    ) throws SQLException {

        if (recordado == null
                || nombreUsuario == null
                || !recordado.equals(nombreUsuario)) {

            return false;
        }

        String sql = """
                SELECT terminal
                FROM util.usuario
                WHERE habilitado = TRUE
                  AND nombre = ?
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    nombreUsuario
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                String terminalGuardada =
                        rs.getString("terminal");

                return terminalActual != null
                        && terminalActual.equals(
                        terminalGuardada
                );
            }
        }
    }

    private void recordarUsuario(
            Connection cn,
            int codigoUsuario,
            String terminal
    ) throws SQLException {

        boolean autoCommitOriginal =
                cn.getAutoCommit();

        try {
            cn.setAutoCommit(false);

            String desmarcarSql = """
                    UPDATE util.usuario
                    SET recordar = FALSE
                    """;

            try (
                    PreparedStatement ps =
                            cn.prepareStatement(desmarcarSql)
            ) {
                ps.executeUpdate();
            }

            String recordarSql = """
                    UPDATE util.usuario
                    SET terminal = ?,
                        recordar = TRUE
                    WHERE codigo = ?
                    """;

            try (
                    PreparedStatement ps =
                            cn.prepareStatement(recordarSql)
            ) {

                ps.setString(
                        1,
                        terminal == null ? "" : terminal
                );

                ps.setInt(
                        2,
                        codigoUsuario
                );

                int filas = ps.executeUpdate();

                if (filas != 1) {
                    throw new SQLException(
                            "No se pudo recordar el usuario "
                                    + codigoUsuario
                    );
                }
            }

            cn.commit();

        } catch (SQLException ex) {

            cn.rollback();
            throw ex;

        } finally {

            cn.setAutoCommit(autoCommitOriginal);
        }
    }

    // -------------------------------------------------------------------------
    // BÚSQUEDAS
    // -------------------------------------------------------------------------

    public UsuarioDTO.Response obtenerUsuario(
            int codigoUsuario
    ) throws SQLException {

        String sql = consultaBase()
                + " WHERE usr.codigo = ? ";

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoUsuario
            );

            return obtenerUno(ps);
        }
    }

    public UsuarioDTO.Response obtenerUsuarioRecordado(
            String terminal
    ) throws SQLException {

        if (terminal == null || terminal.isBlank()) {
            return null;
        }

        String sql = consultaBase()
                + """
                  WHERE usr.recordar = TRUE
                    AND usr.terminal = ?
                  """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    terminal
            );

            return obtenerUno(ps);
        }
    }

    public UsuarioDTO.Response obtenerUsuarioNombre(
            String nombre
    ) throws SQLException {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String sql = consultaBase()
                + " WHERE usr.nombre = ? ";

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    nombre.trim()
            );

            return obtenerUno(ps);
        }
    }

    private UsuarioDTO.Response obtenerUno(
            PreparedStatement ps
    ) throws SQLException {

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapearUsuario(rs);
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // INSERT
    // -------------------------------------------------------------------------

    public boolean insertarUsuario(
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        if (!guardarRequestValido(request)
                || request.password == null) {

            return false;
        }

        String sql = """
                INSERT INTO util.usuario (
                    nombre,
                    habilitado,
                    mostrarcostos,
                    facturar,
                    password,
                    nivel
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    request.nombre
            );

            ps.setBoolean(
                    2,
                    request.habilitado
            );

            ps.setBoolean(
                    3,
                    request.mostrarCostos
            );

            ps.setBoolean(
                    4,
                    request.facturar
            );

            ps.setString(
                    5,
                    request.password
            );

            ps.setInt(
                    6,
                    request.codigoNivel
            );

            return ps.executeUpdate() == 1;
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public boolean actualizarUsuario(
            int codigoUsuario,
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        if (codigoUsuario <= 0
                || !guardarRequestValido(request)) {

            return false;
        }

        if (request.password == null) {
            return actualizarUsuarioSinPassword(
                    codigoUsuario,
                    request
            );
        }

        return actualizarUsuarioConPassword(
                codigoUsuario,
                request
        );
    }

    private boolean actualizarUsuarioSinPassword(
            int codigoUsuario,
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        String sql = """
                UPDATE util.usuario
                SET nombre = ?,
                    nivel = ?,
                    habilitado = ?,
                    mostrarcostos = ?,
                    facturar = ?
                WHERE codigo = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    request.nombre
            );

            ps.setInt(
                    2,
                    request.codigoNivel
            );

            ps.setBoolean(
                    3,
                    request.habilitado
            );

            ps.setBoolean(
                    4,
                    request.mostrarCostos
            );

            ps.setBoolean(
                    5,
                    request.facturar
            );

            ps.setInt(
                    6,
                    codigoUsuario
            );

            return ps.executeUpdate() == 1;
        }
    }

    private boolean actualizarUsuarioConPassword(
            int codigoUsuario,
            UsuarioDTO.GuardarRequest request
    ) throws SQLException {

        String sql = """
                UPDATE util.usuario
                SET nombre = ?,
                    nivel = ?,
                    habilitado = ?,
                    mostrarcostos = ?,
                    facturar = ?,
                    password = ?
                WHERE codigo = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    request.nombre
            );

            ps.setInt(
                    2,
                    request.codigoNivel
            );

            ps.setBoolean(
                    3,
                    request.habilitado
            );

            ps.setBoolean(
                    4,
                    request.mostrarCostos
            );

            ps.setBoolean(
                    5,
                    request.facturar
            );

            ps.setString(
                    6,
                    request.password
            );

            ps.setInt(
                    7,
                    codigoUsuario
            );

            return ps.executeUpdate() == 1;
        }
    }

    // -------------------------------------------------------------------------
    // AUXILIARES
    // -------------------------------------------------------------------------

    private boolean guardarRequestValido(
            UsuarioDTO.GuardarRequest request
    ) {

        return request != null
                && request.nombre != null
                && !request.nombre.isBlank()
                && request.codigoNivel > 0;
    }

    private UsuarioDTO.Response mapearUsuario(
            ResultSet rs
    ) throws SQLException {

        UsuarioDTO.Response dto =
                new UsuarioDTO.Response();

        dto.codigo =
                rs.getInt("codigo");

        dto.nombre =
                rs.getString("nombre");

        dto.password =
                rs.getString("password");

        dto.nivel = new ParDTO(
                rs.getInt("codnivel"),
                rs.getString("nomnivel")
        );

        dto.habilitado =
                rs.getBoolean("habilitado");

        dto.mostrarcostos =
                rs.getBoolean("mostrarcostos");

        dto.facturar =
                rs.getBoolean("facturar");

        return dto;
    }

    private String consultaBase() {

        return """
                SELECT
                    usr.codigo,
                    usr.nombre,
                    usr.password,
                    usr.habilitado,
                    usr.mostrarcostos,
                    usr.facturar,
                    nvl.codigo AS codnivel,
                    nvl.descripcion AS nomnivel
                FROM util.usuario usr
                INNER JOIN util.nivel nvl
                        ON usr.nivel = nvl.codigo
                """;
    }
}