package com.util.api.proveedor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProveedorRepository {

    private static final int CODIGO_USUARIO_INICIAL = 1;

    private static final String SELECT_COMPLETO = """
            SELECT prov.codigo,
                   prov.razonsocial,
                   prov.codigoidentificacion,
                   prov.ingresosbrutos,
                   prov.telfijo,
                   prov.telmovil,
                   prov.emailpersonal,
                   prov.emailfacturacion,
                   prov.domicilio,
                   prov.bonificacion,
                   prov.observacion,
                   prov.habilitado,

                   NOT EXISTS (
                       SELECT 1
                       FROM util.proveedorusuario pu
                       WHERE pu.codigoproveedor = prov.codigo
                         AND pu.codigousuario = ?
                         AND pu.estado = 1
                   ) AS "habilitadoUsuario",

                   COALESCE(
                       civa.codigo,
                       -1
                   ) AS "codCondicionIva",

                   civa.descripcion AS "nomCondicionIva",

                   COALESCE(
                       tipoident.codigo,
                       -1
                   ) AS "codIdent",

                   tipoident.descripcion AS "nomIdent",

                   loc.id AS localidad,
                   loc.nombre AS "nomLocalidad",

                   provincia.id AS provincia,
                   provincia.nombre AS "nomProvincia",

                   provExterno.codigoproveedor
                       AS "codigoProveedorExterno"

            FROM util.proveedor prov

            LEFT JOIN util.tipoidentificacion tipoident
                   ON prov.tipoidentificacion =
                      tipoident.codigo
                  AND tipoident.habilitado = TRUE

            LEFT JOIN util.localidad loc
                   ON prov.localidad = loc.id

            LEFT JOIN util.provincia provincia
                   ON loc.provincia = provincia.id

            LEFT JOIN util.condicioniva civa
                   ON prov.condicioniva = civa.codigo
                  AND civa.habilitado = TRUE

            LEFT JOIN util.proveedorexternoinstalacion provExterno
                   ON prov.codigo =
                      provExterno.idproveedorlocal
            """;

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // LISTADOS
    // -------------------------------------------------------------------------

    public List<ProveedorDTO> obtenerCompleto(
            boolean incluirProvExterno,
            int codigoUsuario
    ) throws SQLException {

        List<ProveedorDTO> resultado =
                new ArrayList<>();

        StringBuilder sql =
                new StringBuilder(SELECT_COMPLETO);

        if (!incluirProvExterno) {
            sql.append("""
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM util.proveedorexternoinstalacion tmp
                        WHERE tmp.idproveedorlocal = prov.codigo
                    )
                    """);
        }

        sql.append("""
                ORDER BY prov.codigo
                """);

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql.toString())
        ) {
            ps.setInt(
                    1,
                    codigoUsuario
            );

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(
                            mapCompleto(rs)
                    );
                }
            }
        }

        return resultado;
    }

    public List<ProveedorDTO.ComboDTO> obtenerParaCombo(
            boolean incluirTodos
    ) throws SQLException {

        List<ProveedorDTO.ComboDTO> resultado =
                new ArrayList<>();

        if (incluirTodos) {
            resultado.add(
                    new ProveedorDTO.ComboDTO(
                            -1,
                            "Todos"
                    )
            );
        }

        String sql = """
                SELECT prov.codigo,
                       prov.razonsocial
                FROM util.proveedor prov
                ORDER BY
                    CASE
                        WHEN LOWER(
                            TRIM(prov.razonsocial)
                        ) = 'propio'
                        THEN 0
                        ELSE 1
                    END,
                    prov.razonsocial ASC
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
                        new ProveedorDTO.ComboDTO(
                                rs.getInt("codigo"),
                                rs.getString("razonsocial")
                        )
                );
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // BÚSQUEDAS
    // -------------------------------------------------------------------------

    public ProveedorDTO buscarCod(
            int codigo,
            int codigoUsuario
    ) throws SQLException {

        String sql =
                SELECT_COMPLETO
                        + """
                WHERE prov.codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(
                    1,
                    codigoUsuario
            );

            ps.setInt(
                    2,
                    codigo
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCompleto(rs);
                }
            }
        }

        return null;
    }

    public ProveedorDTO buscarPorCodigoIdentificacion(
            String codigoIdentificacion,
            int codigoUsuario
    ) throws SQLException {

        String sql =
                SELECT_COMPLETO
                        + """
                WHERE TRIM(prov.codigoidentificacion) = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(
                    1,
                    codigoUsuario
            );

            ps.setString(
                    2,
                    codigoIdentificacion.trim()
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCompleto(rs);
                }
            }
        }

        return null;
    }

    public String obtenerRazonSocial(
            int codigo,
            int habilitado
    ) throws SQLException {

        StringBuilder sql =
                new StringBuilder("""
                        SELECT razonsocial
                        FROM util.proveedor
                        WHERE codigo = ?
                        """);

        if (habilitado == 0) {
            sql.append("""
                    AND habilitado = TRUE
                    """);

        } else if (habilitado == 1) {
            sql.append("""
                    AND habilitado = FALSE
                    """);
        }

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql.toString())
        ) {
            ps.setInt(
                    1,
                    codigo
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return valorString(
                            rs.getString("razonsocial")
                    );
                }
            }
        }

        return "";
    }

    public double obtenerSaldo(
            int codigoProveedor
    ) throws SQLException {

        String sql = """
                SELECT util.saldoProvHasta(
                    ?,
                    CURRENT_DATE
                ) AS saldo
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(
                    1,
                    codigoProveedor
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return redondear(
                            rs.getDouble("saldo"),
                            2
                    );
                }
            }
        }

        return 0;
    }

    // -------------------------------------------------------------------------
    // INSERTAR
    // -------------------------------------------------------------------------

    public boolean insertar(
            ProveedorDTO.GuardarRequest request
    ) throws SQLException {

        String sqlProveedor = """
                INSERT INTO util.proveedor (
                    razonsocial,
                    tipoidentificacion,
                    codigoidentificacion,
                    condicioniva,
                    ingresosbrutos,
                    telfijo,
                    telmovil,
                    emailpersonal,
                    emailfacturacion,
                    domicilio,
                    localidad,
                    bonificacion,
                    observacion,
                    habilitado
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?
                )
                RETURNING codigo
                """;

        String sqlProveedorUsuario = """
                INSERT INTO util.proveedorusuario (
                    codigoproveedor,
                    codigousuario,
                    estado
                )
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int codigoProveedor;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlProveedor
                             )) {

                    cargarDatosProveedor(
                            ps,
                            request.proveedor
                    );

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return false;
                        }

                        codigoProveedor =
                                rs.getInt("codigo");
                    }
                }

                int estado =
                        request.proveedor.habilitadoUsuario
                                ? 0
                                : 1;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlProveedorUsuario
                             )) {

                    ps.setInt(
                            1,
                            codigoProveedor
                    );

                    /*
                     * Conserva el comportamiento del DBDAO.
                     *
                     * Para usar el usuario recibido:
                     *
                     * ps.setInt(2, request.codigoUsuario);
                     */
                    ps.setInt(
                            2,
                            CODIGO_USUARIO_INICIAL
                    );

                    ps.setInt(
                            3,
                            estado
                    );

                    int filasInsertadas =
                            ps.executeUpdate();

                    if (filasInsertadas != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();

                return true;

            } catch (Exception ex) {
                rollback(connection);
                throw ex;

            } finally {
                restaurarAutoCommit(
                        connection,
                        autoCommitOriginal
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    public boolean actualizar(
            int codigoProveedor,
            ProveedorDTO.GuardarRequest request
    ) throws SQLException {

        String sqlProveedor = """
                UPDATE util.proveedor
                SET razonsocial = ?,
                    tipoidentificacion = ?,
                    codigoidentificacion = ?,
                    condicioniva = ?,
                    ingresosbrutos = ?,
                    telfijo = ?,
                    telmovil = ?,
                    emailpersonal = ?,
                    emailfacturacion = ?,
                    domicilio = ?,
                    localidad = ?,
                    bonificacion = ?,
                    observacion = ?,
                    habilitado = ?
                WHERE codigo = ?
                """;

        String sqlProveedorUsuario = """
                INSERT INTO util.proveedorusuario (
                    codigoproveedor,
                    codigousuario,
                    estado
                )
                VALUES (?, ?, ?)
                ON CONFLICT (
                    codigoproveedor,
                    codigousuario
                )
                DO UPDATE SET
                    estado = EXCLUDED.estado
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int actualizados;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlProveedor
                             )) {

                    cargarDatosProveedor(
                            ps,
                            request.proveedor
                    );

                    ps.setInt(
                            15,
                            codigoProveedor
                    );

                    actualizados =
                            ps.executeUpdate();
                }

                if (actualizados != 1) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlProveedorUsuario
                             )) {

                    ps.setInt(
                            1,
                            codigoProveedor
                    );

                    ps.setInt(
                            2,
                            request.codigoUsuario
                    );

                    ps.setInt(
                            3,
                            request.proveedor.habilitadoUsuario
                                    ? 0
                                    : 1
                    );

                    ps.executeUpdate();
                }

                connection.commit();

                return true;

            } catch (Exception ex) {
                rollback(connection);
                throw ex;

            } finally {
                restaurarAutoCommit(
                        connection,
                        autoCommitOriginal
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // MAPEOS
    // -------------------------------------------------------------------------

    private ProveedorDTO mapCompleto(
            ResultSet rs
    ) throws SQLException {

        ProveedorDTO dto =
                new ProveedorDTO();

        dto.codigo =
                rs.getInt("codigo");

        dto.codigoProveedorExterno =
                (Integer) rs.getObject(
                        "codigoProveedorExterno"
                );

        dto.razonSocial =
                rs.getString("razonsocial");

        dto.tipoIdentificacionCodigo =
                rs.getInt("codIdent");

        dto.tipoIdentificacionNombre =
                rs.getString("nomIdent");

        dto.codigoIdentificacion =
                rs.getString("codigoidentificacion");

        dto.condicionIvaCodigo =
                rs.getInt("codCondicionIva");

        dto.condicionIvaNombre =
                rs.getString("nomCondicionIva");

        dto.ingresosBrutos =
                rs.getString("ingresosbrutos");

        dto.telefonoFijo =
                rs.getString("telfijo");

        dto.telefonoMovil =
                rs.getString("telmovil");

        dto.emailPersonal =
                rs.getString("emailpersonal");

        dto.emailFacturacion =
                rs.getString("emailfacturacion");

        dto.domicilio =
                rs.getString("domicilio");

        dto.localidadId =
                rs.getInt("localidad");

        dto.localidadNombre =
                rs.getString("nomLocalidad");

        dto.provinciaId =
                (Integer) rs.getObject("provincia");

        dto.provinciaNombre =
                rs.getString("nomProvincia");

        dto.bonificacion =
                rs.getDouble("bonificacion");

        dto.observacion =
                rs.getString("observacion");

        dto.habilitado =
                rs.getBoolean("habilitado");

        dto.habilitadoUsuario =
                rs.getBoolean("habilitadoUsuario");

        return dto;
    }

    private void cargarDatosProveedor(
            PreparedStatement ps,
            ProveedorDTO proveedor
    ) throws SQLException {

        ps.setString(
                1,
                valorString(proveedor.razonSocial)
        );

        ps.setInt(
                2,
                proveedor.tipoIdentificacionCodigo
        );

        ps.setString(
                3,
                valorString(
                        proveedor.codigoIdentificacion
                )
        );

        ps.setInt(
                4,
                proveedor.condicionIvaCodigo
        );

        ps.setString(
                5,
                valorString(proveedor.ingresosBrutos)
        );

        ps.setString(
                6,
                valorString(proveedor.telefonoFijo)
        );

        ps.setString(
                7,
                valorString(proveedor.telefonoMovil)
        );

        ps.setString(
                8,
                valorString(proveedor.emailPersonal)
        );

        ps.setString(
                9,
                valorString(proveedor.emailFacturacion)
        );

        ps.setString(
                10,
                valorString(proveedor.domicilio)
        );

        ps.setInt(
                11,
                proveedor.localidadId
        );

        ps.setDouble(
                12,
                proveedor.bonificacion
        );

        ps.setString(
                13,
                valorString(proveedor.observacion)
        );

        ps.setBoolean(
                14,
                proveedor.habilitado
        );
    }

    // -------------------------------------------------------------------------
    // UTILIDADES
    // -------------------------------------------------------------------------

    private String valorString(
            String valor
    ) {
        return valor == null
                ? ""
                : valor;
    }

    private double redondear(
            double valor,
            int decimales
    ) {
        return BigDecimal
                .valueOf(valor)
                .setScale(
                        decimales,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private void rollback(
            Connection connection
    ) {
        try {
            connection.rollback();

        } catch (SQLException ex) {
            System.err.println(
                    "Error ejecutando rollback de proveedor"
            );

            ex.printStackTrace();
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

        } catch (SQLException ex) {
            System.err.println(
                    "Error restaurando autoCommit de proveedor"
            );

            ex.printStackTrace();
        }
    }
}