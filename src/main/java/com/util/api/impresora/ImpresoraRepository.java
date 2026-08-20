package com.util.api.impresora;

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
public class ImpresoraRepository {

    private static final String SELECT_POR_TERMINAL = """
            SELECT imp.reporte,
                   imp.nombreimpresora,
                   imp.terminal,
                   imp.esfiscal
            FROM util.impresora imp
            WHERE imp.terminal = ?
            ORDER BY imp.reporte,
                     imp.nombreimpresora
            """;

    private static final String SELECT_POR_TERMINAL_Y_REPORTE = """
            SELECT imp.reporte,
                   imp.nombreimpresora,
                   imp.terminal,
                   imp.esfiscal
            FROM util.impresora imp
            WHERE imp.terminal = ?
              AND imp.reporte = ?
            LIMIT 1
            """;

    private static final String INSERT = """
            INSERT INTO util.impresora (
                reporte,
                nombreimpresora,
                terminal,
                esfiscal
            )
            VALUES (?, ?, ?, ?)
            """;

    private static final String INSERT_O_ACTUALIZAR = """
            INSERT INTO util.impresora (
                reporte,
                nombreimpresora,
                terminal,
                esfiscal
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT (reporte, terminal)
            DO UPDATE SET
                nombreimpresora = EXCLUDED.nombreimpresora,
                esfiscal = EXCLUDED.esfiscal
            """;

    private static final String DELETE = """
            DELETE FROM util.impresora
            WHERE reporte = ?
              AND nombreimpresora = ?
              AND terminal = ?
            """;

    @Inject
    DataSource dataSource;

    public List<ImpresoraDTO.Response> listarPorTerminal(
            String terminal
    ) throws SQLException {

        List<ImpresoraDTO.Response> resultado =
                new ArrayList<>();

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(
                                SELECT_POR_TERMINAL
                        )
        ) {
            ps.setString(1, terminal);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapImpresora(rs));
                }
            }
        }

        return resultado;
    }

    public ImpresoraDTO.Response buscarTerminalReporte(
            String terminal,
            String reporte
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(
                                SELECT_POR_TERMINAL_Y_REPORTE
                        )
        ) {
            ps.setString(1, terminal);
            ps.setString(2, reporte);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapImpresora(rs);
                }
            }
        }

        return null;
    }

    public boolean insertar(
            ImpresoraDTO.CrearRequest request
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(INSERT)
        ) {
            ps.setString(
                    1,
                    request.getReporte()
            );

            ps.setString(
                    2,
                    request.getNombreImpresora()
            );

            ps.setString(
                    3,
                    request.getTerminal()
            );

            ps.setBoolean(
                    4,
                    request.isFiscal()
            );

            return ps.executeUpdate() == 1;
        }
    }

    public boolean guardarOActualizar(
            ImpresoraDTO.CrearRequest request
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(
                                INSERT_O_ACTUALIZAR
                        )
        ) {
            ps.setString(
                    1,
                    request.getReporte().trim()
            );

            ps.setString(
                    2,
                    request.getNombreImpresora() == null
                            ? ""
                            : request.getNombreImpresora().trim()
            );

            ps.setString(
                    3,
                    request.getTerminal().trim()
            );

            ps.setBoolean(
                    4,
                    request.isFiscal()
            );

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(
            ImpresoraDTO.EliminarRequest request
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(DELETE)
        ) {
            ps.setString(
                    1,
                    request.getReporte()
            );

            ps.setString(
                    2,
                    request.getNombreImpresora()
            );

            ps.setString(
                    3,
                    request.getTerminal()
            );

            return ps.executeUpdate() == 1;
        }
    }

    private ImpresoraDTO.Response mapImpresora(
            ResultSet rs
    ) throws SQLException {

        ImpresoraDTO.Response dto =
                new ImpresoraDTO.Response();

        dto.setReporte(
                rs.getString("reporte")
        );

        dto.setNombreImpresora(
                rs.getString("nombreimpresora")
        );

        dto.setTerminal(
                rs.getString("terminal")
        );

        dto.setFiscal(
                rs.getBoolean("esfiscal")
        );

        return dto;
    }
}