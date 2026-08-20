package com.util.api.localidad;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LocalidadRepository {

    private static final String SELECT_LOCALIDAD_COMPLETA = """
            SELECT loc.id,
                   loc.cp,
                   loc.nombre,
                   prov.id AS idprov,
                   prov.nombre AS nombreprov,
                   pais.id AS idpais,
                   pais.nombre AS nombrepais
            FROM util.localidad loc
            INNER JOIN util.provincia prov
                    ON prov.id = loc.provincia
            INNER JOIN util.pais pais
                    ON pais.id = prov.pais
            """;

    @Inject
    DataSource dataSource;

    public List<LocalidadDTO.Response> obtenerCompleto()
            throws SQLException {

        List<LocalidadDTO.Response> resultado =
                new ArrayList<>();

        String sql = SELECT_LOCALIDAD_COMPLETA + """
                ORDER BY pais.nombre,
                         prov.nombre,
                         loc.nombre
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
                resultado.add(mapLocalidad(rs));
            }
        }

        return resultado;
    }

    public LocalidadDTO.Response buscarId(int id)
            throws SQLException {

        String sql = SELECT_LOCALIDAD_COMPLETA + """
                WHERE loc.id = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapLocalidad(rs);
                }
            }
        }

        return null;
    }

    public List<LocalidadDTO.Response> buscarCodPostal(
            String codigoPostal
    ) throws SQLException {

        List<LocalidadDTO.Response> resultado =
                new ArrayList<>();

        String sql = SELECT_LOCALIDAD_COMPLETA + """
                WHERE TRIM(loc.cp) = TRIM(?)
                ORDER BY loc.nombre
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(1, codigoPostal);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapLocalidad(rs));
                }
            }
        }

        return resultado;
    }

    public LocalidadDTO.Response buscarCodPostalNombre(
            String codigoPostal,
            String nombre
    ) throws SQLException {

        String sql = SELECT_LOCALIDAD_COMPLETA + """
                WHERE TRIM(loc.cp) = TRIM(?)
                  AND LOWER(TRIM(loc.nombre)) =
                      LOWER(TRIM(?))
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(1, codigoPostal);
            ps.setString(2, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapLocalidad(rs);
                }
            }
        }

        return null;
    }

    public LocalidadDTO.OperacionResponse insertar(
            LocalidadDTO.CrearRequest request
    ) throws SQLException {

        String sql = """
            INSERT INTO util.localidad (
                nombre,
                cp,
                provincia
            )
            VALUES (?, ?, ?)
            RETURNING id
            """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setString(1, request.getNombre());
            ps.setString(2, request.getCodigoPostal());
            ps.setInt(3, request.getProvinciaId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LocalidadDTO.OperacionResponse(
                            true,
                            rs.getInt("id"),
                            "Localidad insertada correctamente"
                    );
                }
            }

            return new LocalidadDTO.OperacionResponse(
                    false,
                    0,
                    "No se pudo insertar la localidad"
            );
        }
    }

    private LocalidadDTO.Response mapLocalidad(
            ResultSet rs
    ) throws SQLException {

        LocalidadDTO.Response dto =
                new LocalidadDTO.Response();

        dto.setId(
                rs.getInt("id")
        );

        dto.setCodigoPostal(
                rs.getString("cp")
        );

        dto.setNombre(
                rs.getString("nombre")
        );

        dto.setProvinciaId(
                rs.getInt("idprov")
        );

        dto.setProvinciaNombre(
                rs.getString("nombreprov")
        );

        dto.setPaisId(
                rs.getInt("idpais")
        );

        dto.setPaisNombre(
                rs.getString("nombrepais")
        );

        return dto;
    }
}