package com.util.api.articulo;

import com.util.api.generico.ParDTO;
import com.util.api.generico.ResInsertUpdate;
import com.util.api.generico.TriplaDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArticuloRepository {

    @Inject
    DataSource dataSource;

    public List<ArticuloDTO> obtenerCompletos(
            int habilitados,
            int categoriaArticulo,
            int codProveedor
    ) throws SQLException {

        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append("""
            SELECT art.codigo
                 , art.descripcion
                 , art.descripcionpropia
                 , art.unidadcompra
                 , art.multiplicadorcompra
                 , art.costo
                 , art.margenextra
                 , art.llevastock
                 , art.stock
                 , art.stockminimo
                 , art.articulopadre
                 , art.articulopadreproveedor
                 , art.hijos
                 , art.observacion
                 , art.fechaalta
                 , art.origen
                 , art.habilitado
                 , art.estadoproveedor
                 , imp.codigo AS codImp
                 , imp.nombre AS nomImp
                 , imp.alicuota AS aliImp
                 , cat.codigo AS codCat
                 , cat.nombre AS nomCat
                 , uvent.codigo AS codUVent
                 , uvent.nombre AS nomUVent
                 , prov.codigo AS codProv
                 , prov.razonsocial AS nomProv
            FROM util.articulo AS art
            LEFT JOIN util.impuesto AS imp ON art.impuesto = imp.codigo
            LEFT JOIN util.categoriaarticulo AS cat ON art.categoria = cat.codigo
            LEFT JOIN util.unidadventa AS uvent ON art.unidadventa = uvent.codigo
            LEFT JOIN util.proveedor AS prov ON art.proveedor = prov.codigo
            WHERE 1 = 1
        """);

        if (habilitados == 0) {
            sql.append(" AND art.habilitado = TRUE AND art.estadoproveedor = 0 ");
        } else if (habilitados == 1) {
            sql.append(" AND (art.habilitado = FALSE OR art.estadoproveedor <> 0) ");
        }

        if (categoriaArticulo != -1) {
            sql.append(" AND cat.codigo = ? ");
            params.add(categoriaArticulo);
        }

        if (codProveedor != -1) {
            sql.append(" AND art.proveedor = ? ");
            params.add(codProveedor);
        }

        sql.append(" ORDER BY art.descripcion ASC ");

        List<ArticuloDTO> result = new ArrayList<>();

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public List<ArticuloDTO> buscarCodArticulo(
            String codigo,
            String proveedor
    ) throws SQLException {

        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append("""
        SELECT art.codigo
             , art.descripcion
             , art.descripcionpropia
             , art.unidadcompra
             , art.multiplicadorcompra
             , art.costo
             , art.margenextra
             , art.llevastock
             , art.stock
             , art.stockminimo
             , art.articulopadre
             , art.articulopadreproveedor
             , art.hijos
             , art.observacion
             , art.fechaalta
             , art.origen
             , art.habilitado
             , art.estadoproveedor
             , imp.codigo AS codImp
             , imp.nombre AS nomImp
             , imp.alicuota AS aliImp
             , cat.codigo AS codCat
             , cat.nombre AS nomCat
             , uvent.codigo AS codUVent
             , uvent.nombre AS nomUVent
             , prov.codigo AS codProv
             , prov.razonsocial AS nomProv
        FROM util.articulo AS art
        LEFT JOIN util.impuesto AS imp ON art.impuesto = imp.codigo
        LEFT JOIN util.categoriaarticulo AS cat ON art.categoria = cat.codigo
        LEFT JOIN util.unidadventa AS uvent ON art.unidadventa = uvent.codigo
        LEFT JOIN util.proveedor AS prov ON art.proveedor = prov.codigo
        WHERE art.codigo = ?
          AND art.habilitado = TRUE
          AND prov.habilitado = TRUE
    """);

        params.add(codigo);
        if (proveedor != null && !proveedor.isBlank()) {
            sql.append(" AND art.proveedor = ? ");
            params.add(Integer.parseInt(proveedor));
        }

        sql.append(" ORDER BY art.descripcion ASC ");
        List<ArticuloDTO> result = new ArrayList<>();

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    public List<ArticuloDTO> buscarParaVenta(
            String filtro,
            int codigoUsuario,
            int limite
    ) throws SQLException {

        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append("""
        WITH candidatos AS (
            SELECT art.codigo
                 , art.descripcion
                 , art.descripcionpropia
                 , art.unidadcompra
                 , art.multiplicadorcompra
                 , art.costo
                 , art.margenextra
                 , art.llevastock
                 , art.stock
                 , art.stockminimo
                 , art.articulopadre
                 , art.articulopadreproveedor
                 , art.hijos
                 , art.observacion
                 , art.fechaalta
                 , art.origen
                 , art.habilitado
                 , art.estadoproveedor

                 , imp.codigo AS codImp
                 , imp.nombre AS nomImp
                 , imp.alicuota AS aliImp

                 , cat.codigo AS codCat
                 , cat.nombre AS nomCat

                 , uvent.codigo AS codUVent
                 , uvent.nombre AS nomUVent

                 , prov.codigo AS codProv
                 , prov.razonsocial AS nomProv

                 , SUM(art.stock) OVER (
                       PARTITION BY TRIM(art.codigo)
                   ) AS stock_total

                 , ROW_NUMBER() OVER (
                       PARTITION BY TRIM(art.codigo)
                       ORDER BY
                           CASE WHEN art.stock > 0 THEN 0 ELSE 1 END,
                           art.stock DESC,
                           art.fechaactualizacion DESC,
                           art.proveedor
                   ) AS rn

            FROM util.articulo art

            INNER JOIN util.proveedor prov
                    ON art.proveedor = prov.codigo

            LEFT JOIN util.impuesto imp
                   ON art.impuesto = imp.codigo

            LEFT JOIN util.categoriaarticulo cat
                   ON art.categoria = cat.codigo

            LEFT JOIN util.unidadventa uvent
                   ON art.unidadventa = uvent.codigo

            WHERE art.habilitado = TRUE
              AND prov.habilitado = TRUE
              AND art.codigo <> '0'
              AND art.costo > 0

              AND NOT EXISTS (
                  SELECT 1
                  FROM util.proveedorusuario pu
                  WHERE pu.codigoproveedor = prov.codigo
                    AND pu.codigousuario = ?
                    AND pu.estado = 1
              )
        """);

        params.add(codigoUsuario);

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql.append("""
              AND (
                     art.codigo ILIKE ?
                  OR art.descripcionpropia ILIKE ?
                  OR art.descripcion ILIKE ?
              )
        """);

            String like = "%" + filtro.trim() + "%";

            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append("""
        )
        SELECT codigo
             , descripcion
             , descripcionpropia
             , unidadcompra
             , multiplicadorcompra
             , costo
             , margenextra
             , llevastock
             , stock_total AS stock
             , stockminimo
             , articulopadre
             , articulopadreproveedor
             , hijos
             , observacion
             , fechaalta
             , origen
             , habilitado
             , estadoproveedor
             , codImp
             , nomImp
             , aliImp
             , codCat
             , nomCat
             , codUVent
             , nomUVent
             , codProv
             , nomProv

        FROM candidatos
        WHERE rn = 1
        ORDER BY descripcion ASC
    """);

        if (limite > 0) {
            sql.append(" LIMIT ? ");
            params.add(limite);
        }

        List<ArticuloDTO> result = new ArrayList<>();

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public List<ArticuloDTO> buscarParaVentaPorCodigo(
            String codigo,
            int codigoUsuario
    ) throws SQLException {

        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append("""
        SELECT art.codigo
             , art.descripcion
             , art.descripcionpropia
             , art.unidadcompra
             , art.multiplicadorcompra
             , art.costo
             , art.margenextra
             , art.llevastock
             , art.stock
             , art.stockminimo
             , art.articulopadre
             , art.articulopadreproveedor
             , art.hijos
             , art.observacion
             , art.fechaalta
             , art.origen
             , art.habilitado
             , art.estadoproveedor
             , imp.codigo AS codImp
             , imp.nombre AS nomImp
             , imp.alicuota AS aliImp
             , cat.codigo AS codCat
             , cat.nombre AS nomCat
             , uvent.codigo AS codUVent
             , uvent.nombre AS nomUVent
             , prov.codigo AS codProv
             , prov.razonsocial AS nomProv
        FROM util.articulo art
        INNER JOIN util.proveedor prov ON art.proveedor = prov.codigo
        LEFT JOIN util.impuesto imp ON art.impuesto = imp.codigo
        LEFT JOIN util.categoriaarticulo cat ON art.categoria = cat.codigo
        LEFT JOIN util.unidadventa uvent ON art.unidadventa = uvent.codigo
        WHERE art.habilitado = TRUE
          AND prov.habilitado = TRUE
          AND art.costo > 0
          AND TRIM(art.codigo) = ?
          AND NOT EXISTS (
              SELECT 1
              FROM util.proveedorusuario pu
              WHERE pu.codigoproveedor = prov.codigo
                AND pu.codigousuario = ?
                AND pu.estado = 1
          )
        ORDER BY art.descripcion ASC
    """);

        params.add(codigo);
        params.add(codigoUsuario);

        List<ArticuloDTO> result = new ArrayList<>();

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public ResInsertUpdate insert(ArticuloDTO nuevo) {

        if (nuevo == null) {
            return new ResInsertUpdate("-1", false);
        }

        String sql = """
        INSERT INTO util.articulo (
            codigo,
            descripcion,
            unidadventa,
            unidadcompra,
            multiplicadorcompra,
            costo,
            margenextra,
            categoria,
            impuesto,
            proveedor,
            llevastock,
            stock,
            stockminimo,
            observacion,
            origen,
            habilitado,
            estadoproveedor,
            descripcionpropia
        )
        VALUES (
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?
        )
    """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setString(1, nuevo.codigo);
            ps.setString(2, nuevo.descripcion);

            ps.setInt(
                    3,
                    nuevo.unidadVenta == null
                            ? 1
                            : nuevo.unidadVenta.primero
            );

            ps.setString(4, nuevo.unidadCompra);

            ps.setDouble(
                    5,
                    nuevo.multiplicadorCompra == null
                            ? 1d
                            : nuevo.multiplicadorCompra
            );

            ps.setDouble(
                    6,
                    nuevo.costo == null
                            ? 0d
                            : nuevo.costo
            );

            ps.setDouble(
                    7,
                    nuevo.margenExtra == null
                            ? 0d
                            : nuevo.margenExtra
            );

            ps.setInt(
                    8,
                    nuevo.categoria == null
                            ? 1
                            : nuevo.categoria.primero
            );

            ps.setInt(
                    9,
                    nuevo.impuesto == null
                            ? 1
                            : nuevo.impuesto.primero
            );

            ps.setInt(
                    10,
                    nuevo.proveedor == null
                            ? 0
                            : nuevo.proveedor.primero
            );

            ps.setBoolean(
                    11,
                    Boolean.TRUE.equals(nuevo.llevaStock)
            );

            ps.setDouble(
                    12,
                    nuevo.stock == null
                            ? 0d
                            : nuevo.stock
            );

            ps.setDouble(
                    13,
                    nuevo.stockMinimo == null
                            ? 0d
                            : nuevo.stockMinimo
            );

            ps.setString(14, nuevo.observacion);

            ps.setInt(
                    15,
                    nuevo.origen == null
                            ? 0
                            : nuevo.origen
            );

            ps.setBoolean(
                    16,
                    nuevo.habilitado == null
                            || nuevo.habilitado
            );

            ps.setInt(
                    17,
                    nuevo.estadoProveedor == null
                            ? 0
                            : nuevo.estadoProveedor
            );

            ps.setString(18, nuevo.descripcionPropia);

            int insertados = ps.executeUpdate();

            return new ResInsertUpdate(
                    insertados == 1 ? "00000" : "-1",
                    insertados == 1
            );

        } catch (SQLException ex) {
            ex.printStackTrace();

            return new ResInsertUpdate(
                    ex.getSQLState() == null ? "-1" : ex.getSQLState(),
                    false
            );
        }
    }

    public ResInsertUpdate update(
            String codigo,
            int proveedor,
            ArticuloDTO articulo
    ) {
        if (articulo == null
                || codigo == null
                || codigo.isBlank()
                || proveedor < 0) {

            return new ResInsertUpdate("-1", false);
        }

        String sql = """
        UPDATE util.articulo
        SET descripcion = ?
          , unidadventa = ?
          , unidadcompra = ?
          , multiplicadorcompra = ?
          , costo = ?
          , margenextra = ?
          , categoria = ?
          , impuesto = ?
          , proveedor = ?
          , llevastock = ?
          , stock = ?
          , stockminimo = ?
          , articulopadre = ?
          , articulopadreproveedor = ?
          , observacion = ?
          , origen = ?
          , habilitado = ?
          , estadoproveedor = ?
          , descripcionpropia = ?
          , fechaactualizacion = NOW()
        WHERE codigo = ?
          AND proveedor = ?
    """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setString(1, articulo.descripcion);

            ps.setInt(
                    2,
                    articulo.unidadVenta == null
                            ? 1
                            : articulo.unidadVenta.primero
            );

            ps.setString(3, articulo.unidadCompra);

            ps.setDouble(
                    4,
                    articulo.multiplicadorCompra == null
                            ? 1d
                            : articulo.multiplicadorCompra
            );

            ps.setDouble(
                    5,
                    articulo.costo == null
                            ? 0d
                            : articulo.costo
            );

            ps.setDouble(
                    6,
                    articulo.margenExtra == null
                            ? 0d
                            : articulo.margenExtra
            );

            ps.setInt(
                    7,
                    articulo.categoria == null
                            ? 1
                            : articulo.categoria.primero
            );

            ps.setInt(
                    8,
                    articulo.impuesto == null
                            ? 1
                            : articulo.impuesto.primero
            );

            ps.setInt(
                    9,
                    articulo.proveedor == null
                            ? proveedor
                            : articulo.proveedor.primero
            );

            ps.setBoolean(
                    10,
                    Boolean.TRUE.equals(articulo.llevaStock)
            );

            ps.setDouble(
                    11,
                    articulo.stock == null
                            ? 0d
                            : articulo.stock
            );

            ps.setDouble(
                    12,
                    articulo.stockMinimo == null
                            ? 0d
                            : articulo.stockMinimo
            );

            ps.setString(13, articulo.artPadre);

            ps.setInt(
                    14,
                    articulo.artPadreProveedor == null
                            ? 0
                            : articulo.artPadreProveedor
            );

            ps.setString(15, articulo.observacion);

            ps.setInt(
                    16,
                    articulo.origen == null
                            ? 0
                            : articulo.origen
            );

            ps.setBoolean(
                    17,
                    articulo.habilitado == null
                            || articulo.habilitado
            );

            ps.setInt(
                    18,
                    articulo.estadoProveedor == null
                            ? 0
                            : articulo.estadoProveedor
            );

            ps.setString(19, articulo.descripcionPropia);

            ps.setString(20, codigo);
            ps.setInt(21, proveedor);

            int actualizados = ps.executeUpdate();

            return new ResInsertUpdate(
                    actualizados > 0 ? "00000" : "-1",
                    actualizados > 0
            );

        } catch (SQLException ex) {
            ex.printStackTrace();

            return new ResInsertUpdate(
                    ex.getSQLState() == null ? "-1" : ex.getSQLState(),
                    false
            );
        }
    }

    public boolean delete(
            String codigo,
            int proveedor,
            int origen
    ) throws SQLException {

        if (codigo == null
                || codigo.isBlank()
                || proveedor < 0) {
            return false;
        }

        String sql;

        if (origen == 2) {
            sql = """
            UPDATE util.articulo
            SET habilitado = FALSE
              , fechaactualizacion = NOW()
            WHERE codigo = ?
              AND proveedor = ?
        """;
        } else {
            sql = """
            DELETE FROM util.articulo
            WHERE codigo = ?
              AND proveedor = ?
        """;
        }

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            ps.setInt(2, proveedor);

            return ps.executeUpdate() == 1;
        }
    }

    public ResInsertUpdate cambiarProveedor(
            ArticuloDTO.CambiarProveedorRequest request
    ) {
        if (request == null
                || request.codigoArticulo == null
                || request.codigoArticulo.isBlank()
                || request.proveedorActual < 0
                || request.proveedorNuevo < 0
                || request.proveedorActual == request.proveedorNuevo) {

            return new ResInsertUpdate("-1", false);
        }

        String sql = """
        UPDATE util.articulo
        SET proveedor = ?
          , fechaactualizacion = NOW()
        WHERE codigo = ?
          AND proveedor = ?
          AND origen <> 2
    """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, request.proveedorNuevo);
            ps.setString(2, request.codigoArticulo.trim());
            ps.setInt(3, request.proveedorActual);

            int actualizados = ps.executeUpdate();

            return new ResInsertUpdate(
                    actualizados == 1 ? "00000" : "-1",
                    actualizados == 1
            );

        } catch (SQLException ex) {
            ex.printStackTrace();

            return new ResInsertUpdate(
                    ex.getSQLState() == null ? "-1" : ex.getSQLState(),
                    false
            );
        }
    }

    public int establecerMargenPorProveedor(
            ArticuloDTO.EstablecerMargenRequest request
    ) throws SQLException {

        if (request == null || request.codigoProveedor < 0) {
            return 0;
        }

        String sql = """
        UPDATE util.articulo
        SET margenextra = ?
          , fechaactualizacion = NOW()
        WHERE proveedor = ?
    """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setDouble(1, request.margen);
            ps.setInt(2, request.codigoProveedor);

            return ps.executeUpdate();
        }
    }

    public int actualizarCostosPorPorcentaje(
            ArticuloDTO.ActualizarCostosRequest request
    ) throws SQLException {

        if (request == null
                || request.articulos == null
                || request.articulos.isEmpty()) {

            return 0;
        }

        String sql = """
        UPDATE util.articulo
        SET costo = ROUND((costo * ?)::numeric, 2)
          , fechaactualizacion = NOW()
        WHERE codigo = ?
          AND proveedor = ?
    """;

        double factor = 1d + (request.porcentaje / 100d);

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitAnterior = cn.getAutoCommit();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                for (ArticuloDTO articulo : request.articulos) {

                    if (articulo == null
                            || articulo.codigo == null
                            || articulo.codigo.isBlank()
                            || articulo.proveedor == null) {
                        continue;
                    }

                    ps.setDouble(1, factor);
                    ps.setString(2, articulo.codigo);
                    ps.setInt(
                            3,
                            articulo.proveedor.primero
                    );

                    ps.addBatch();
                }

                int[] resultados = ps.executeBatch();

                int actualizados = 0;

                for (int resultado : resultados) {
                    if (resultado > 0
                            || resultado == Statement.SUCCESS_NO_INFO) {
                        actualizados++;
                    }
                }

                cn.commit();
                cn.setAutoCommit(autoCommitAnterior);

                return actualizados;

            } catch (Exception ex) {
                cn.rollback();
                cn.setAutoCommit(autoCommitAnterior);
                throw ex;
            }
        }
    }

    public boolean actualizarCodigo(
            ArticuloDTO.CambiarCodigoRequest request
    ) throws SQLException {

        if (request == null
                || request.codigoActual == null
                || request.codigoActual.isBlank()
                || request.codigoNuevo == null
                || request.codigoNuevo.isBlank()
                || request.proveedor < 0) {

            return false;
        }

        String sql = """
        UPDATE util.articulo
        SET codigo = ?
          , fechaactualizacion = NOW()
        WHERE codigo = ?
          AND proveedor = ?
    """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setString(1, request.codigoNuevo.trim());
            ps.setString(2, request.codigoActual.trim());
            ps.setInt(3, request.proveedor);

            return ps.executeUpdate() > 0;
        }
    }

    public int actualizarCostosPorProveedor(
            int codigoProveedor,
            double porcentaje
    ) {
        if (codigoProveedor < 0 || !Double.isFinite(porcentaje)) {
            return 0;
        }

        String sql = """
        UPDATE util.articulo
        SET costo = costo * (1 + (? / 100.0))
          , fechaactualizacion = NOW()
        WHERE proveedor = ?
          AND origen <> 2
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setDouble(1, porcentaje);
            ps.setInt(2, codigoProveedor);

            return ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public int establecerMargenPorCategoria(
            int codigoCategoria,
            double margen
    ) {
        if (codigoCategoria <= 0 || !Double.isFinite(margen)) {
            return 0;
        }

        String sql = """
        UPDATE util.articulo
        SET margenextra = ?
          , fechaactualizacion = NOW()
        WHERE categoria = ?
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setDouble(1, margen);
            ps.setInt(2, codigoCategoria);

            return ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public ResInsertUpdate updateFromExcel(
            ArticuloDTO articulo,
            boolean actualizaStock,
            boolean actualizaMargen
    ) {
        if (articulo == null
                || articulo.codigo == null
                || articulo.codigo.isBlank()
                || articulo.proveedor == null) {

            return new ResInsertUpdate("-1", false);
        }

        StringBuilder sql = new StringBuilder();

        sql.append("""
        UPDATE util.articulo
        SET descripcion = ?
          , unidadcompra = ?
          , multiplicadorcompra = ?
          , costo = ?
    """);

        if (actualizaStock) {
            sql.append(" , stock = ? ");
        }

        if (actualizaMargen) {
            sql.append(" , margenextra = ? ");
        }

        sql.append("""
          , impuesto = ?
          , categoria = ?
          , llevastock = ?
          , descripcionpropia = ?
    """);

        boolean tieneObservacion =
                articulo.observacion != null
                        && !articulo.observacion.isBlank();

        if (tieneObservacion) {
            sql.append(" , observacion = ? ");
        }

        sql.append("""
          , fechaactualizacion = NOW()
        WHERE codigo = ?
          AND proveedor = ?
    """);

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql.toString())
        ) {
            int i = 1;

            ps.setString(i++, articulo.descripcion);
            ps.setString(i++, articulo.unidadCompra);

            ps.setDouble(
                    i++,
                    articulo.multiplicadorCompra == null
                            ? 1d
                            : articulo.multiplicadorCompra
            );

            ps.setDouble(
                    i++,
                    articulo.costo == null
                            ? 0d
                            : articulo.costo
            );

            if (actualizaStock) {
                ps.setDouble(
                        i++,
                        articulo.stock == null
                                ? 0d
                                : articulo.stock
                );
            }

            if (actualizaMargen) {
                ps.setDouble(
                        i++,
                        articulo.margenExtra == null
                                ? 0d
                                : articulo.margenExtra
                );
            }

            ps.setInt(
                    i++,
                    articulo.impuesto == null
                            ? 1
                            : articulo.impuesto.primero
            );

            ps.setInt(
                    i++,
                    articulo.categoria == null
                            ? 1
                            : articulo.categoria.primero
            );

            ps.setBoolean(
                    i++,
                    Boolean.TRUE.equals(articulo.llevaStock)
            );

            ps.setString(i++, articulo.descripcionPropia);

            if (tieneObservacion) {
                ps.setString(i++, articulo.observacion);
            }

            ps.setString(i++, articulo.codigo);

            ps.setInt(
                    i,
                    articulo.proveedor.primero
            );

            int actualizados = ps.executeUpdate();

            return new ResInsertUpdate(
                    actualizados > 0 ? "00000" : "-1",
                    actualizados > 0
            );

        } catch (SQLException ex) {
            ex.printStackTrace();

            return new ResInsertUpdate(
                    ex.getSQLState() == null ? "-1" : ex.getSQLState(),
                    false
            );
        }
    }

    private ArticuloDTO map(ResultSet rs) throws SQLException {

        ArticuloDTO dto = new ArticuloDTO();

        dto.codigo = rs.getString("codigo");
        dto.descripcion = rs.getString("descripcion");
        dto.descripcionPropia = rs.getString("descripcionpropia");

        dto.unidadVenta = new ParDTO(
                rs.getInt("codUVent"),
                rs.getString("nomUVent")
        );

        dto.unidadCompra = rs.getString("unidadcompra");
        dto.multiplicadorCompra = rs.getDouble("multiplicadorcompra");
        dto.costo = rs.getDouble("costo");
        dto.margenExtra = rs.getDouble("margenextra");

        dto.categoria = new ParDTO(
                rs.getInt("codCat"),
                rs.getString("nomCat")
        );

        dto.impuesto = new TriplaDTO(
                rs.getInt("codImp"),
                rs.getDouble("aliImp"),
                rs.getString("nomImp")
        );

        dto.alicuota = rs.getDouble("aliImp");

        dto.proveedor = new ParDTO(
                rs.getInt("codProv"),
                rs.getString("nomProv")
        );

        dto.llevaStock = rs.getBoolean("llevastock");
        dto.stock = rs.getDouble("stock");
        dto.stockMinimo = rs.getDouble("stockminimo");

        dto.artPadre = rs.getString("articulopadre");
        dto.artPadreProveedor = rs.getInt("articulopadreproveedor");
        dto.hijos = rs.getInt("hijos");

        dto.observacion = rs.getString("observacion");
        dto.fechaAlta = rs.getString("fechaalta");

        dto.origen = rs.getInt("origen");
        dto.habilitado = rs.getBoolean("habilitado");
        dto.estadoProveedor = rs.getInt("estadoproveedor");

        return dto;
    }
}
