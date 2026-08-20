package com.util.api.proveedorexterno;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class ProveedorExternoConnectionFactory {

    public Connection abrir(
            Connection conexionLocal,
            int codigoProveedor
    ) throws SQLException {

        ConfiguracionConexion configuracion =
                obtenerConfiguracion(
                        conexionLocal,
                        codigoProveedor
                );

        String jdbcUrl =
                construirJdbcUrl(configuracion);

        return DriverManager.getConnection(
                jdbcUrl,
                configuracion.usuario,
                configuracion.password
        );
    }

    private ConfiguracionConexion obtenerConfiguracion(
            Connection conexionLocal,
            int codigoProveedor
    ) throws SQLException {

        String sql =
                " SELECT port,\n"
                        + "        url,\n"
                        + "        db,\n"
                        + "        dbuser,\n"
                        + "        pass\n"
                        + " FROM fcentral."
                        + "proveedorexternoconexionweb\n"
                        + " WHERE codigoproveedor = ?";

        try (PreparedStatement ps =
                     conexionLocal.prepareStatement(sql)) {

            ps.setInt(1, codigoProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No existe configuración de conexión "
                                    + "para el proveedor "
                                    + codigoProveedor
                    );
                }

                ConfiguracionConexion configuracion =
                        new ConfiguracionConexion();

                configuracion.port =
                        rs.getString("port");
                configuracion.url =
                        rs.getString("url");
                configuracion.db =
                        rs.getString("db");
                configuracion.usuario =
                        rs.getString("dbuser");
                configuracion.password =
                        rs.getString("pass");

                return configuracion;
            }
        }
    }

    private String construirJdbcUrl(
            ConfiguracionConexion configuracion
    ) {
        String url = configuracion.url.trim();

        if (url.startsWith("jdbc:")) {
            return url;
        }

        return "jdbc:postgresql://"
                + url
                + ":"
                + configuracion.port
                + "/"
                + configuracion.db;
    }

    private static class ConfiguracionConexion {

        private String port;
        private String url;
        private String db;
        private String usuario;
        private String password;
    }
}