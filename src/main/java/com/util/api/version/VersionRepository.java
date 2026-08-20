package com.util.api.version;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class VersionRepository {

    private static final BigDecimal VERSION_INICIAL =
            BigDecimal.ONE;

    private static final String OBSERVACION_INICIAL =
            "V1";

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // OBTENER POR UNA TERMINAL
    // -------------------------------------------------------------------------

    public VersionDTO obtenerActual(
            int idApp,
            String terminal
    ) throws SQLException {

        if (idApp <= 0 || esVacio(terminal)) {
            return null;
        }

        String sql = """
                SELECT
                    ver.idapp,
                    ver.terminal,
                    ver.versionactual,
                    ver.fecha,
                    ver.observacion,
                    ver.fechaact,
                    ver.fecharev
                FROM util.version ver
                WHERE ver.idapp = ?
                  AND ver.terminal = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idApp
            );

            ps.setString(
                    2,
                    terminal.trim()
            );

            return obtenerUno(ps);
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER ENTRE DOS TERMINALES
    // -------------------------------------------------------------------------

    public VersionDTO obtenerActual(
            int idApp,
            String terminalPrincipal,
            String terminalAlternativa
    ) throws SQLException {

        if (idApp <= 0
                || (
                esVacio(terminalPrincipal)
                        && esVacio(terminalAlternativa)
        )) {

            return null;
        }

        String principal =
                normalizarTerminal(
                        terminalPrincipal
                );

        String alternativa =
                normalizarTerminal(
                        terminalAlternativa
                );

        if (principal == null) {
            principal = alternativa;
        }

        if (alternativa == null) {
            alternativa = principal;
        }

        String sql = """
                SELECT
                    ver.idapp,
                    ver.terminal,
                    ver.versionactual,
                    ver.fecha,
                    ver.observacion,
                    ver.fechaact,
                    ver.fecharev
                FROM util.version ver
                WHERE ver.idapp = ?
                  AND (
                      ver.terminal = ?
                      OR ver.terminal = ?
                  )
                ORDER BY
                    CASE
                        WHEN ver.terminal = ? THEN 0
                        ELSE 1
                    END
                LIMIT 1
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idApp
            );

            ps.setString(
                    2,
                    principal
            );

            ps.setString(
                    3,
                    alternativa
            );

            ps.setString(
                    4,
                    principal
            );

            return obtenerUno(ps);
        }
    }

    // -------------------------------------------------------------------------
    // EXISTENCIA
    // -------------------------------------------------------------------------

    public boolean existe(
            int idApp,
            List<String> terminales
    ) throws SQLException {

        if (idApp <= 0
                || terminales == null
                || terminales.isEmpty()) {

            return false;
        }

        List<String> validas =
                normalizarTerminales(terminales);

        if (validas.isEmpty()) {
            return false;
        }

        StringBuilder sql = new StringBuilder(
                """
                SELECT 1
                FROM util.version
                WHERE idapp = ?
                  AND terminal IN (
                """
        );

        for (int i = 0; i < validas.size(); i++) {

            if (i > 0) {
                sql.append(", ");
            }

            sql.append("?");
        }

        sql.append(") LIMIT 1");

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps =
                        cn.prepareStatement(sql.toString())
        ) {

            ps.setInt(
                    1,
                    idApp
            );

            int parametro = 2;

            for (String terminal : validas) {
                ps.setString(
                        parametro++,
                        terminal
                );
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // -------------------------------------------------------------------------
    // REGISTRO INICIAL
    // -------------------------------------------------------------------------

    public boolean asegurarRegistroInicial(
            VersionDTO.IdentificacionRequest request
    ) throws SQLException {

        if (request == null
                || request.idApp <= 0
                || esVacio(request.terminal)) {

            return false;
        }

        String terminal =
                request.terminal.trim();

        String sql = """
                INSERT INTO util.version (
                    idapp,
                    terminal,
                    versionactual,
                    fecha,
                    observacion,
                    fechaact
                )
                SELECT
                    ?,
                    ?,
                    ?,
                    CURRENT_DATE,
                    ?,
                    CURRENT_DATE
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM util.version
                    WHERE idapp = ?
                      AND terminal = ?
                )
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    request.idApp
            );

            ps.setString(
                    2,
                    terminal
            );

            ps.setBigDecimal(
                    3,
                    VERSION_INICIAL
            );

            ps.setString(
                    4,
                    OBSERVACION_INICIAL
            );

            ps.setInt(
                    5,
                    request.idApp
            );

            ps.setString(
                    6,
                    terminal
            );

            int filasInsertadas =
                    ps.executeUpdate();

            if (filasInsertadas == 1) {
                return true;
            }

            return existe(
                    request.idApp,
                    List.of(terminal)
            );
        }
    }

    // -------------------------------------------------------------------------
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    public boolean actualizarVersion(
            int idApp,
            String terminal,
            BigDecimal versionActual,
            String observacion
    ) throws SQLException {

        if (idApp <= 0
                || terminal == null
                || terminal.trim().isEmpty()
                || versionActual == null) {

            return false;
        }

        String sql =
                " INSERT INTO util.version " +
                        " (idapp, terminal, versionactual, fecha, observacion, fechaact) " +
                        " VALUES (?, ?, ?, CURRENT_DATE, ?, CURRENT_DATE) " +
                        " ON CONFLICT (idapp, terminal) " +
                        " DO UPDATE SET " +
                        "     versionactual = EXCLUDED.versionactual, " +
                        "     observacion = EXCLUDED.observacion, " +
                        "     fechaact = CURRENT_DATE ";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, idApp);
            ps.setString(2, terminal.trim());
            ps.setBigDecimal(3, versionActual);

            if (observacion == null
                    || observacion.trim().isEmpty()) {

                ps.setNull(
                        4,
                        Types.VARCHAR
                );

            } else {

                ps.setString(
                        4,
                        observacion.trim()
                );
            }

            return ps.executeUpdate() == 1;
        }
    }

    // -------------------------------------------------------------------------
    // MAPEOS
    // -------------------------------------------------------------------------

    private VersionDTO obtenerUno(
            PreparedStatement ps
    ) throws SQLException {

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapVersion(rs);
            }
        }

        return null;
    }

    private VersionDTO mapVersion(
            ResultSet rs
    ) throws SQLException {

        VersionDTO version =
                new VersionDTO();

        version.idApp =
                rs.getInt("idapp");

        version.terminal =
                rs.getString("terminal");

        version.versionActual =
                rs.getBigDecimal("versionactual");

        version.fecha =
                obtenerFechaComoString(
                        rs,
                        "fecha"
                );

        version.observacion =
                rs.getString("observacion");

        version.fechaActualizacion =
                obtenerFechaComoString(
                        rs,
                        "fechaact"
                );

        version.fechaRevision =
                obtenerFechaComoString(
                        rs,
                        "fecharev"
                );

        return version;
    }

    private String obtenerFechaComoString(
            ResultSet rs,
            String columna
    ) throws SQLException {

        java.sql.Date fecha =
                rs.getDate(columna);

        return fecha == null
                ? null
                : fecha.toString();
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private List<String> normalizarTerminales(
            List<String> terminales
    ) {

        List<String> resultado =
                new ArrayList<>();

        for (String terminal : terminales) {

            String normalizada =
                    normalizarTerminal(terminal);

            if (normalizada == null
                    || resultado.contains(normalizada)) {

                continue;
            }

            resultado.add(normalizada);
        }

        return resultado;
    }

    private void setNullableString(
            PreparedStatement ps,
            int posicion,
            String valor
    ) throws SQLException {

        if (esVacio(valor)) {

            ps.setNull(
                    posicion,
                    Types.VARCHAR
            );

        } else {

            ps.setString(
                    posicion,
                    valor.trim()
            );
        }
    }

    private boolean esVacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }

    private String normalizarTerminal(
            String terminal
    ) {

        if (esVacio(terminal)) {
            return null;
        }

        return terminal.trim();
    }
}