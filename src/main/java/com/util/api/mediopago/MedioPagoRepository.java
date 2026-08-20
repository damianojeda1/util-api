package com.util.api.mediopago;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MedioPagoRepository {

    private static final String CAMPOS = """
            id,
            codigo,
            descripcion,
            activo,
            orden,
            es_fisico,
            permite_ref,
            codigo_enum,
            porcentaje_comision
            """;

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // LISTADO
    // -------------------------------------------------------------------------

    public List<MedioPagoDTO> listar(
            boolean incluirInactivos
    ) throws SQLException {

        List<MedioPagoDTO> resultado =
                new ArrayList<>();

        String sql;

        if (incluirInactivos) {
            sql = """
                    SELECT %s
                    FROM util.medio_pago
                    ORDER BY orden, descripcion
                    """.formatted(CAMPOS);

        } else {
            sql = """
                    SELECT %s
                    FROM util.medio_pago
                    WHERE activo = TRUE
                    ORDER BY orden, descripcion
                    """.formatted(CAMPOS);
        }

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // INSERTAR
    // -------------------------------------------------------------------------

    public int insertar(
            MedioPagoDTO medioPago
    ) throws SQLException {

        if (!esValidoParaGuardar(medioPago)) {
            return -1;
        }

        if (medioPago.codigoEnum == null
                || medioPago.codigoEnum.isBlank()) {

            medioPago.codigoEnum =
                    normalizarCodigoEnum(
                            medioPago.codigo
                    );
        }

        String sql = """
                INSERT INTO util.medio_pago (
                    codigo,
                    descripcion,
                    activo,
                    orden,
                    es_fisico,
                    permite_ref,
                    codigo_enum,
                    porcentaje_comision
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    medioPago.codigo.trim()
            );

            ps.setString(
                    2,
                    medioPago.descripcion.trim()
            );

            ps.setBoolean(
                    3,
                    medioPago.activo
            );

            ps.setInt(
                    4,
                    medioPago.orden
            );

            ps.setBoolean(
                    5,
                    medioPago.esFisico
            );

            ps.setBoolean(
                    6,
                    medioPago.permiteRef
            );

            ps.setString(
                    7,
                    medioPago.codigoEnum
            );

            ps.setBigDecimal(
                    8,
                    decimal(
                            medioPago.porcentajeComision
                    )
            );

            return ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    public int actualizar(
            int id,
            MedioPagoDTO medioPago
    ) throws SQLException {

        if (id <= 0
                || !esValidoParaGuardar(medioPago)) {

            return -1;
        }

        if (medioPago.codigoEnum == null
                || medioPago.codigoEnum.isBlank()) {

            medioPago.codigoEnum =
                    normalizarCodigoEnum(
                            medioPago.codigo
                    );
        }

        String sql = """
                UPDATE util.medio_pago
                SET codigo = ?,
                    descripcion = ?,
                    activo = ?,
                    orden = ?,
                    es_fisico = ?,
                    permite_ref = ?,
                    codigo_enum = ?,
                    porcentaje_comision = ?
                WHERE id = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    medioPago.codigo.trim()
            );

            ps.setString(
                    2,
                    medioPago.descripcion.trim()
            );

            ps.setBoolean(
                    3,
                    medioPago.activo
            );

            ps.setInt(
                    4,
                    medioPago.orden
            );

            ps.setBoolean(
                    5,
                    medioPago.esFisico
            );

            ps.setBoolean(
                    6,
                    medioPago.permiteRef
            );

            ps.setString(
                    7,
                    medioPago.codigoEnum
            );

            ps.setBigDecimal(
                    8,
                    decimal(
                            medioPago.porcentajeComision
                    )
            );

            ps.setInt(
                    9,
                    id
            );

            return ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // OCULTAR / MOSTRAR
    // -------------------------------------------------------------------------

    public int ocultar(int id) throws SQLException {

        if (id <= 0) {
            return -1;
        }

        return actualizarActivo(
                id,
                false
        );
    }

    public int mostrar(int id) throws SQLException {

        if (id <= 0) {
            return -1;
        }

        return actualizarActivo(
                id,
                true
        );
    }

    private int actualizarActivo(
            int id,
            boolean activo
    ) throws SQLException {

        String sql = """
                UPDATE util.medio_pago
                SET activo = ?
                WHERE id = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setBoolean(
                    1,
                    activo
            );

            ps.setInt(
                    2,
                    id
            );

            return ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // BÚSQUEDAS
    // -------------------------------------------------------------------------

    public MedioPagoDTO findById(
            int id
    ) throws SQLException {

        if (id <= 0) {
            return null;
        }

        String sql = """
                SELECT %s
                FROM util.medio_pago
                WHERE id = ?
                """.formatted(CAMPOS);

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    id
            );

            return obtenerUno(ps);
        }
    }

    public MedioPagoDTO findPorCodigoEnum(
            String codigoEnum
    ) throws SQLException {

        if (codigoEnum == null
                || codigoEnum.isBlank()) {

            return null;
        }

        String sql = """
                SELECT %s
                FROM util.medio_pago
                WHERE UPPER(codigo_enum) = UPPER(?)
                """.formatted(CAMPOS);

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    codigoEnum.trim()
            );

            return obtenerUno(ps);
        }
    }

    private MedioPagoDTO obtenerUno(
            PreparedStatement ps
    ) throws SQLException {

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapear(rs);
            }
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // MAPEO
    // -------------------------------------------------------------------------

    private MedioPagoDTO mapear(
            ResultSet rs
    ) throws SQLException {

        MedioPagoDTO medioPago =
                new MedioPagoDTO();

        medioPago.id =
                rs.getInt("id");

        medioPago.codigo =
                rs.getString("codigo");

        medioPago.descripcion =
                rs.getString("descripcion");

        medioPago.activo =
                rs.getBoolean("activo");

        medioPago.orden =
                rs.getInt("orden");

        medioPago.esFisico =
                rs.getBoolean("es_fisico");

        medioPago.permiteRef =
                rs.getBoolean("permite_ref");

        medioPago.codigoEnum =
                rs.getString("codigo_enum");

        medioPago.porcentajeComision =
                decimal(
                        rs.getBigDecimal(
                                "porcentaje_comision"
                        )
                );

        return medioPago;
    }

    // -------------------------------------------------------------------------
    // VALIDACIONES
    // -------------------------------------------------------------------------

    private boolean esValidoParaGuardar(
            MedioPagoDTO medioPago
    ) {

        return medioPago != null
                && medioPago.codigo != null
                && !medioPago.codigo.isBlank()
                && medioPago.descripcion != null
                && !medioPago.descripcion.isBlank();
    }

    private BigDecimal decimal(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String normalizarCodigoEnum(
            String codigo
    ) {

        if (codigo == null) {
            return null;
        }

        String normalizado =
                Normalizer.normalize(
                        codigo.trim(),
                        Normalizer.Form.NFD
                );

        normalizado =
                normalizado.replaceAll(
                        "\\p{M}",
                        ""
                );

        return normalizado
                .toUpperCase()
                .replaceAll(
                        "[^A-Z0-9]+",
                        "_"
                )
                .replaceAll(
                        "^_+|_+$",
                        ""
                );
    }
}