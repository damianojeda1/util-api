package com.util.api.parametrosconexion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class ParametrosConexionRepository {

    @Inject
    DataSource dataSource;

    public ParametrosConexionDTO obtenerConexionUtil() {
        String sql = """
            SELECT port, url, db, dbuser, pass
            FROM util.parametroconexionutil
            LIMIT 1
            """;

        return obtenerUno(sql);
    }

    public ParametrosConexionDTO obtenerConexionProveedor(
            int codigoProveedor
    ) {
        if (codigoProveedor <= 0) {
            return null;
        }

        String sql = """
            SELECT port, url, db, dbuser, pass
            FROM fcentral.proveedorexternoconexionweb
            WHERE codigoproveedor = ?
            LIMIT 1
            """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private ParametrosConexionDTO obtenerUno(
            String sql
    ) {
        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return mapear(rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private ParametrosConexionDTO mapear(
            ResultSet rs
    ) throws SQLException {
        ParametrosConexionDTO dto =
                new ParametrosConexionDTO();

        dto.port = rs.getString("port");
        dto.url = rs.getString("url");
        dto.db = rs.getString("db");
        dto.dbuser = rs.getString("dbuser");
        dto.pass = rs.getString("pass");

        return dto;
    }
}