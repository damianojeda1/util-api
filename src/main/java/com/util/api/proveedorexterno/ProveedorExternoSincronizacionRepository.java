package com.util.api.proveedorexterno;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class ProveedorExternoSincronizacionRepository {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    ProveedorExternoConnectionFactory connectionFactory;

    public ProveedorExternoDTO.SincronizarResponse sincronizar(
            int codigoProveedor,
            boolean cargarArticulos
    ) throws SQLException {

        try (Connection local = dataSource.getConnection()) {
            boolean autoCommitOriginal =
                    local.getAutoCommit();

            try {
                local.setAutoCommit(false);

                Instalacion instalacion =
                        obtenerInstalacion(
                                local,
                                codigoProveedor
                        );

                try (Connection externa =
                             connectionFactory.abrir(
                                     local,
                                     codigoProveedor
                             )) {

                    sincronizarEstados(local, externa);
                    limpiarEmpaques(local);
                    sincronizarDetalleEmpaques(
                            local,
                            externa
                    );

                    suspenderArticulosLocales(
                            local,
                            instalacion.idProveedorLocal
                    );

                    ResultadoArticulos articulos =
                            sincronizarArticulos(
                                    local,
                                    externa,
                                    instalacion.codigoCliente
                            );

                    int empaques =
                            sincronizarEmpaques(
                                    local,
                                    externa
                            );

                    int insertadosUtil = 0;

                    if (cargarArticulos) {
                        insertadosUtil =
                                insertarArticulosEnUtil(
                                        local,
                                        instalacion.idProveedorLocal
                                );
                    }

                    int actualizadosUtil =
                            actualizarArticulosEnUtil(
                                    local,
                                    instalacion.idProveedorLocal
                            );

                    String fecha =
                            guardarFechaActualizacion(
                                    local,
                                    codigoProveedor
                            );

                    local.commit();

                    return crearRespuesta(
                            articulos,
                            empaques,
                            insertadosUtil,
                            actualizadosUtil,
                            fecha
                    );
                }
            } catch (Exception e) {
                local.rollback();

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "Error durante la sincronización",
                        e
                );
            } finally {
                local.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    private Instalacion obtenerInstalacion(
            Connection connection,
            int codigoProveedor
    ) throws SQLException {

        String sql =
                " SELECT idproveedorlocal, codigocliente\n"
                        + " FROM util.proveedorexternoinstalacion\n"
                        + " WHERE codigoproveedor = ?";

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, codigoProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "El proveedor externo no está instalado"
                    );
                }

                Instalacion instalacion = new Instalacion();

                instalacion.idProveedorLocal =
                        rs.getInt("idproveedorlocal");
                instalacion.codigoCliente =
                        rs.getInt("codigocliente");

                return instalacion;
            }
        }
    }

    private void sincronizarEstados(
            Connection local,
            Connection externa
    ) throws SQLException {

        String select =
                " SELECT ArticuloEstadoId,\n"
                        + "        ArticuloEstadoDescripcion\n"
                        + " FROM web_util_articuloestado";

        String upsert =
                " INSERT INTO fcentral.articulosestados\n"
                        + " (codigo, descripcion, habilitadocompra)\n"
                        + " VALUES (?, ?, TRUE)\n"
                        + " ON CONFLICT (codigo) DO UPDATE SET\n"
                        + " descripcion = EXCLUDED.descripcion,\n"
                        + " habilitadocompra = "
                        + "EXCLUDED.habilitadocompra";

        try (
                PreparedStatement origen =
                        externa.prepareStatement(select);
                ResultSet rs = origen.executeQuery();
                PreparedStatement destino =
                        local.prepareStatement(upsert)
        ) {
            while (rs.next()) {
                destino.setInt(
                        1,
                        rs.getInt("ArticuloEstadoId")
                );
                destino.setString(
                        2,
                        rs.getString(
                                "ArticuloEstadoDescripcion"
                        )
                );
                destino.addBatch();
            }

            destino.executeBatch();
        }
    }

    private void limpiarEmpaques(
            Connection local
    ) throws SQLException {

        String sql =
                " TRUNCATE TABLE\n"
                        + " fcentral.articulosempaque,\n"
                        + " fcentral.articulosempaquedetalle";

        try (PreparedStatement ps =
                     local.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void sincronizarDetalleEmpaques(
            Connection local,
            Connection externa
    ) throws SQLException {

        String select =
                " SELECT EmpaqueID, EmpaqueDetalle\n"
                        + " FROM web_util_empaquearticulo";

        String insert =
                " INSERT INTO "
                        + "fcentral.articulosempaquedetalle\n"
                        + " (codigo, descripcion)\n"
                        + " VALUES (?, ?)\n"
                        + " ON CONFLICT (codigo) DO UPDATE SET\n"
                        + " descripcion = EXCLUDED.descripcion";

        try (
                PreparedStatement origen =
                        externa.prepareStatement(select);
                ResultSet rs = origen.executeQuery();
                PreparedStatement destino =
                        local.prepareStatement(insert)
        ) {
            while (rs.next()) {
                destino.setInt(
                        1,
                        rs.getInt("EmpaqueID")
                );
                destino.setString(
                        2,
                        rs.getString("EmpaqueDetalle")
                );
                destino.addBatch();
            }

            destino.executeBatch();
        }
    }

    private void suspenderArticulosLocales(
            Connection local,
            int idProveedorLocal
    ) throws SQLException {

        String sql =
                " UPDATE util.articulo\n"
                        + " SET estadoproveedor = 2\n"
                        + " WHERE proveedor = ?";

        try (PreparedStatement ps =
                     local.prepareStatement(sql)) {

            ps.setInt(1, idProveedorLocal);
            ps.executeUpdate();
        }
    }

    private ResultadoArticulos sincronizarArticulos(
            Connection local,
            Connection externa,
            int codigoCliente
    ) throws SQLException {

        ResultadoArticulos resultado =
                new ResultadoArticulos();

        Set<String> existentes =
                obtenerCodigosExistentes(local);

        String select =
                " SELECT art.ArticuloCodigo,\n"
                        + "        art.ArticuloDescripcion,\n"
                        + "        art.ArticuloUnidadVenta,\n"
                        + "        art.ArticuloTasaIVA,\n"
                        + "        ROUND(\n"
                        + "          art.ArticuloPrecio\n"
                        + "          * (1 + lis.ListaPorcentaje / 100)\n"
                        + "          * (1 - "
                        + "cli.ClienteDescuentoLista / 100),\n"
                        + "          2\n"
                        + "        ) AS ArticuloPrecio,\n"
                        + "        art.ArticuloStock,\n"
                        + "        art.ArticuloStockMin,\n"
                        + "        art.ArticuloStockMax,\n"
                        + "        art.ArticuloCantidadMaxSugerida,\n"
                        + "        art.ArticuloCantidadMinSugerida,\n"
                        + "        art.ArticuloCantidadMultiplo,\n"
                        + "        art.ArticuloObservacion,\n"
                        + "        art.ArticuloEstadoId,\n"
                        + "        art.ArticuloFechaAlta\n"
                        + " FROM web_util_articulo AS art,\n"
                        + "      web_util_cliente AS cli\n"
                        + " INNER JOIN web_util_listaprecio AS lis\n"
                        + "   ON lis.ListaID = cli.ListaID\n"
                        + " WHERE cli.ClienteCodigo = ?";

        String upsert =
                " INSERT INTO fcentral.articulos\n"
                        + " (codigo, descripcion, unidadventa,\n"
                        + "  tasaiva, precio, stock,\n"
                        + "  stockmin, stockmax,\n"
                        + "  cantidadmaxsugerida,\n"
                        + "  cantidadminsugerida,\n"
                        + "  cantidadmultiplo, observacion,\n"
                        + "  articuloestadocodigo, fechaalta)\n"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?)\n"
                        + " ON CONFLICT (codigo) DO UPDATE SET\n"
                        + " descripcion = EXCLUDED.descripcion,\n"
                        + " unidadventa = EXCLUDED.unidadventa,\n"
                        + " tasaiva = EXCLUDED.tasaiva,\n"
                        + " precio = EXCLUDED.precio,\n"
                        + " stock = EXCLUDED.stock,\n"
                        + " stockmin = EXCLUDED.stockmin,\n"
                        + " stockmax = EXCLUDED.stockmax,\n"
                        + " cantidadmaxsugerida = "
                        + "EXCLUDED.cantidadmaxsugerida,\n"
                        + " cantidadminsugerida = "
                        + "EXCLUDED.cantidadminsugerida,\n"
                        + " cantidadmultiplo = "
                        + "EXCLUDED.cantidadmultiplo,\n"
                        + " observacion = EXCLUDED.observacion,\n"
                        + " articuloestadocodigo = "
                        + "EXCLUDED.articuloestadocodigo,\n"
                        + " fechaalta = EXCLUDED.fechaalta";

        try (
                PreparedStatement origen =
                        externa.prepareStatement(select);
                PreparedStatement destino =
                        local.prepareStatement(upsert)
        ) {
            origen.setInt(1, codigoCliente);

            try (ResultSet rs = origen.executeQuery()) {
                while (rs.next()) {
                    String codigo =
                            rs.getString("ArticuloCodigo");

                    if (existentes.contains(codigo)) {
                        resultado.actualizados++;
                    } else {
                        resultado.insertados++;
                        existentes.add(codigo);
                    }

                    cargarArticulo(destino, rs);
                    destino.addBatch();
                    resultado.total++;
                }
            }

            destino.executeBatch();
        }

        return resultado;
    }

    private Set<String> obtenerCodigosExistentes(
            Connection local
    ) throws SQLException {

        Set<String> resultado = new HashSet<>();

        String sql =
                " SELECT codigo FROM fcentral.articulos";

        try (
                PreparedStatement ps =
                        local.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                resultado.add(rs.getString("codigo"));
            }
        }

        return resultado;
    }

    private void cargarArticulo(
            PreparedStatement ps,
            ResultSet rs
    ) throws SQLException {

        ps.setString(1, rs.getString("ArticuloCodigo"));
        ps.setString(2, rs.getString("ArticuloDescripcion"));
        ps.setString(3, rs.getString("ArticuloUnidadVenta"));
        ps.setDouble(4, rs.getDouble("ArticuloTasaIVA"));
        ps.setDouble(5, rs.getDouble("ArticuloPrecio"));
        ps.setDouble(6, rs.getDouble("ArticuloStock"));
        ps.setDouble(7, rs.getDouble("ArticuloStockMin"));
        ps.setDouble(8, rs.getDouble("ArticuloStockMax"));

        ps.setDouble(
                9,
                rs.getDouble("ArticuloCantidadMaxSugerida")
        );
        ps.setDouble(
                10,
                rs.getDouble("ArticuloCantidadMinSugerida")
        );
        ps.setDouble(
                11,
                rs.getDouble("ArticuloCantidadMultiplo")
        );

        ps.setString(
                12,
                rs.getString("ArticuloObservacion")
        );
        ps.setInt(
                13,
                rs.getInt("ArticuloEstadoId")
        );

        java.sql.Date fecha =
                rs.getDate("ArticuloFechaAlta");

        if (fecha == null) {
            ps.setNull(14, Types.DATE);
        } else {
            ps.setDate(14, fecha);
        }
    }

    private int sincronizarEmpaques(
            Connection local,
            Connection externa
    ) throws SQLException {

        String select =
                " SELECT ArticuloCodigo,\n"
                        + "        EmpaqueID,\n"
                        + "        ArticuloEmpaqueCantidad\n"
                        + " FROM web_util_articuloempaques";

        String insert =
                " INSERT INTO fcentral.articulosempaque\n"
                        + " (articulocodigo,\n"
                        + "  articulosempaquedetallecodigo,\n"
                        + "  empaquecantidad)\n"
                        + " VALUES (?, ?, ?)";

        int cantidad = 0;

        try (
                PreparedStatement origen =
                        externa.prepareStatement(select);
                ResultSet rs = origen.executeQuery();
                PreparedStatement destino =
                        local.prepareStatement(insert)
        ) {
            while (rs.next()) {
                destino.setString(
                        1,
                        rs.getString("ArticuloCodigo")
                );
                destino.setInt(
                        2,
                        rs.getInt("EmpaqueID")
                );
                destino.setDouble(
                        3,
                        rs.getDouble(
                                "ArticuloEmpaqueCantidad"
                        )
                );

                destino.addBatch();
                cantidad++;
            }

            destino.executeBatch();
        }

        return cantidad;
    }

    private int insertarArticulosEnUtil(
            Connection local,
            int idProveedorLocal
    ) throws SQLException {

        String sql =
                " INSERT INTO util.articulo\n"
                        + " (codigo, descripcion, unidadcompra,\n"
                        + "  costo, impuesto, proveedor,\n"
                        + "  llevastock, observacion,\n"
                        + "  origen, estadoproveedor)\n"
                        + " SELECT art.codigo,\n"
                        + "        art.descripcion,\n"
                        + "        art.unidadventa,\n"
                        + "        art.precio,\n"
                        + "        COALESCE(\n"
                        + "          (SELECT imp.codigo\n"
                        + "           FROM util.impuesto AS imp\n"
                        + "           WHERE imp.alicuota = art.tasaiva\n"
                        + "           LIMIT 1),\n"
                        + "          -1\n"
                        + "        ),\n"
                        + "        ?,\n"
                        + "        COALESCE(\n"
                        + "          (SELECT conf.valor::boolean\n"
                        + "           FROM util.configuracion AS conf\n"
                        + "           WHERE conf.codigo = "
                        + "'ventas_llevastock'\n"
                        + "           LIMIT 1),\n"
                        + "          FALSE\n"
                        + "        ),\n"
                        + "        art.observacion,\n"
                        + "        2,\n"
                        + "        art.articuloestadocodigo\n"
                        + " FROM fcentral.articulos AS art\n"
                        + " ON CONFLICT ON CONSTRAINT pk_articulo "
                        + "DO NOTHING";

        try (PreparedStatement ps =
                     local.prepareStatement(sql)) {

            ps.setInt(1, idProveedorLocal);
            return ps.executeUpdate();
        }
    }

    private int actualizarArticulosEnUtil(
            Connection local,
            int idProveedorLocal
    ) throws SQLException {

        String sql =
                " UPDATE util.articulo AS local\n"
                        + " SET descripcion = externo.descripcion,\n"
                        + "     costo = CASE\n"
                        + "       WHEN externo.precio > 0\n"
                        + "       THEN externo.precio\n"
                        + "       ELSE local.costo\n"
                        + "     END,\n"
                        + "     impuesto = COALESCE(\n"
                        + "       (SELECT imp.codigo\n"
                        + "        FROM util.impuesto AS imp\n"
                        + "        WHERE imp.alicuota = externo.tasaiva\n"
                        + "        LIMIT 1),\n"
                        + "       -1\n"
                        + "     ),\n"
                        + "     unidadcompra = externo.unidadventa,\n"
                        + "     estadoproveedor = "
                        + "externo.articuloestadocodigo\n"
                        + " FROM fcentral.articulos AS externo\n"
                        + " WHERE local.codigo = externo.codigo\n"
                        + "   AND local.proveedor = ?\n"
                        + "   AND local.origen = 2";

        try (PreparedStatement ps =
                     local.prepareStatement(sql)) {

            ps.setInt(1, idProveedorLocal);
            return ps.executeUpdate();
        }
    }

    private String guardarFechaActualizacion(
            Connection local,
            int codigoProveedor
    ) throws SQLException {

        String sql =
                " INSERT INTO "
                        + "fcentral.proveedorexternoparametro\n"
                        + " (codigoproveedor, codigo, valor)\n"
                        + " VALUES (\n"
                        + "   ?,\n"
                        + "   'actualizacionarticulos',\n"
                        + "   util.parsefecha(CURRENT_DATE)::varchar\n"
                        + " )\n"
                        + " ON CONFLICT (codigoproveedor, codigo)\n"
                        + " DO UPDATE SET valor = EXCLUDED.valor\n"
                        + " RETURNING valor";

        try (PreparedStatement ps =
                     local.prepareStatement(sql)) {

            ps.setInt(1, codigoProveedor);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getString("valor")
                        : "";
            }
        }
    }

    private ProveedorExternoDTO.SincronizarResponse
    crearRespuesta(
            ResultadoArticulos articulos,
            int empaques,
            int insertadosUtil,
            int actualizadosUtil,
            String fecha
    ) {

        ProveedorExternoDTO.SincronizarResponse response =
                new ProveedorExternoDTO.SincronizarResponse();

        response.setExitoso(true);
        response.setMensaje(
                "La sincronización finalizó correctamente"
        );
        response.setTotalArticulos(articulos.total);
        response.setArticulosInsertados(
                articulos.insertados
        );
        response.setArticulosActualizados(
                articulos.actualizados
        );
        response.setEmpaquesInsertados(empaques);
        response.setArticulosUtilInsertados(
                insertadosUtil
        );
        response.setArticulosUtilActualizados(
                actualizadosUtil
        );
        response.setFechaActualizacion(fecha);

        return response;
    }

    private static class Instalacion {

        private int idProveedorLocal;
        private int codigoCliente;
    }

    private static class ResultadoArticulos {

        private int total;
        private int insertados;
        private int actualizados;
    }
}