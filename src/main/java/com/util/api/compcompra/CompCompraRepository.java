package com.util.api.compcompra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@ApplicationScoped
public class CompCompraRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // EXISTENCIA
    // -------------------------------------------------------------------------

    public boolean existeComprobante(
            int codigoProveedor,
            String letra,
            String centro,
            String numero
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM util.comprobantecompracab
                WHERE nroproveedor = ?
                  AND letra = ?
                  AND centro = ?
                  AND numero = ?
                LIMIT 1
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoProveedor);
            ps.setString(2, letra);
            ps.setInt(3, Integer.parseInt(centro));
            ps.setInt(4, Integer.parseInt(numero));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // -------------------------------------------------------------------------
    // INSERTAR COMPROBANTE
    // -------------------------------------------------------------------------

    public int insertarComprobante(
            CompCompraDTO.ComprobanteRequest request
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int codigoComprobante =
                        insertarCabecera(
                                connection,
                                request
                        );

                if (codigoComprobante <= 0) {
                    connection.rollback();
                    return -1;
                }

                insertarRenglones(
                        connection,
                        codigoComprobante,
                        request
                );

                connection.commit();

                return codigoComprobante;

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

    private int insertarCabecera(
            Connection connection,
            CompCompraDTO.ComprobanteRequest request
    ) throws SQLException {

        String sql = """
                INSERT INTO util.comprobantecompracab (
                    nroproveedor,
                    codigocompcompra,
                    fechacomprobante,
                    periodoiva,
                    sistemacomprobante,
                    letra,
                    centro,
                    numero,
                    netogravado,
                    conceptosnogravados,
                    exentos,
                    periibb,
                    periva,
                    perimpinternos,
                    perimpmunicipales,
                    perotrosimp,
                    retiva,
                    retganancias,
                    total,
                    observacion,
                    fecharegistracioncomprobante,
                    usuarioregistracion
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                RETURNING id
                """;

        java.util.Date periodoIVA =
                request.periodoIVA != null
                        ? request.periodoIVA
                        : request.fechaComprobante;

        java.util.Date fechaCarga =
                request.fechaCarga != null
                        ? request.fechaCarga
                        : new java.util.Date();

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            int index = 1;

            ps.setInt(
                    index++,
                    request.codigoProveedor
            );

            ps.setInt(
                    index++,
                    request.codigoTipoComprobante
            );

            ps.setDate(
                    index++,
                    new Date(
                            request.fechaComprobante.getTime()
                    )
            );

            ps.setDate(
                    index++,
                    new Date(periodoIVA.getTime())
            );

            ps.setInt(
                    index++,
                    request.sistemaComprobante
            );

            ps.setString(
                    index++,
                    request.letra
            );

            ps.setInt(
                    index++,
                    request.centro
            );

            ps.setInt(
                    index++,
                    request.numero
            );

            ps.setDouble(
                    index++,
                    request.netoGravado
            );

            ps.setDouble(
                    index++,
                    request.conceptosNoGravados
            );

            ps.setDouble(
                    index++,
                    request.exentos
            );

            ps.setDouble(
                    index++,
                    request.percepcionIIBB
            );

            ps.setDouble(
                    index++,
                    request.percepcionIVA
            );

            ps.setDouble(
                    index++,
                    request.percepcionImpuestosInternos
            );

            ps.setDouble(
                    index++,
                    request.percepcionMunicipal
            );

            ps.setDouble(
                    index++,
                    request.percepcionOtros
            );

            ps.setDouble(
                    index++,
                    request.retencionIVA
            );

            ps.setDouble(
                    index++,
                    request.retencionGanancias
            );

            ps.setDouble(
                    index++,
                    request.total
            );

            ps.setString(
                    index++,
                    normalizarTexto(request.observacion)
            );

            ps.setTimestamp(
                    index++,
                    new Timestamp(fechaCarga.getTime())
            );

            ps.setInt(
                    index,
                    request.codigoUsuario
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return -1;
    }

    private void insertarRenglones(
            Connection connection,
            int codigoComprobante,
            CompCompraDTO.ComprobanteRequest request
    ) throws SQLException {

        String sql = """
                INSERT INTO util.comprobantecomprareng (
                    idcomprobante,
                    idrenglon,
                    codigocompcompra,
                    codigoarticulo,
                    codigoproveedorarticulo,
                    detallearticulo,
                    cantidad,
                    preciobase,
                    porcentajebonificacion,
                    preciobonificado,
                    tasaiva,
                    totalfinal
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            for (CompCompraDTO.ItemRequest item :
                    request.items) {

                int index = 1;

                ps.setInt(
                        index++,
                        codigoComprobante
                );

                ps.setInt(
                        index++,
                        item.numeroRenglon
                );

                ps.setInt(
                        index++,
                        item.codigoTipoComprobante
                );

                ps.setString(
                        index++,
                        item.codigoArticulo
                );

                ps.setInt(
                        index++,
                        item.codigoProveedorArticulo
                );

                ps.setString(
                        index++,
                        normalizarTexto(item.descripcion)
                );

                ps.setDouble(
                        index++,
                        item.cantidad
                );

                ps.setDouble(
                        index++,
                        item.precioBase
                );

                ps.setDouble(
                        index++,
                        item.porcentajeBonificacion
                );

                ps.setDouble(
                        index++,
                        item.precioBonificado
                );

                ps.setDouble(
                        index++,
                        item.alicuotaIVA
                );

                ps.setDouble(
                        index,
                        item.totalFinal
                );

                ps.addBatch();
            }

            int[] resultados =
                    ps.executeBatch();

            if (resultados.length != request.items.size()) {
                throw new SQLException(
                        "No se insertaron todos los renglones"
                );
            }

            for (int resultado : resultados) {
                if (resultado == PreparedStatement.EXECUTE_FAILED) {
                    throw new SQLException(
                            "Falló la inserción de un renglón"
                    );
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // INSERTAR PAGO
    // -------------------------------------------------------------------------

    public int insertarPago(
            CompCompraDTO.PagoRequest request
    ) throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int codigoMovimientoCaja =
                        insertarMovimientoCaja(
                                connection,
                                request
                        );

                if (codigoMovimientoCaja <= 0) {
                    connection.rollback();
                    return -1;
                }

                int codigoPago =
                        insertarPagoCompra(
                                connection,
                                request,
                                codigoMovimientoCaja
                        );

                if (codigoPago <= 0) {
                    connection.rollback();
                    return -1;
                }

                insertarMediosPago(
                        connection,
                        request.pagos,
                        codigoMovimientoCaja
                );

                connection.commit();

                return codigoPago;

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

    private int insertarMovimientoCaja(
            Connection connection,
            CompCompraDTO.PagoRequest request
    ) throws SQLException {

        String sql = """
                INSERT INTO util.movimientocaja (
                    idcaja,
                    importe,
                    tipomovcaja,
                    estado
                )
                VALUES (?, ?, ?, ?)
                RETURNING codigo
                """;

        double importeMovimiento =
                request.codigoTipoComprobante == 1
                        ? -Math.abs(request.total)
                        : Math.abs(request.total);

        int tipoMovimiento =
                request.codigoTipoComprobante == 1
                        ? 50
                        : 51;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.codigoCaja
            );

            ps.setDouble(
                    2,
                    importeMovimiento
            );

            ps.setInt(
                    3,
                    tipoMovimiento
            );

            ps.setInt(
                    4,
                    10
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("codigo");
                }
            }
        }

        return -1;
    }

    private int insertarPagoCompra(
            Connection connection,
            CompCompraDTO.PagoRequest request,
            int codigoMovimientoCaja
    ) throws SQLException {

        String sql = """
                INSERT INTO util.comprobantecomprapago (
                    tipo,
                    fecha,
                    total,
                    idvendedor,
                    nombrevendedor,
                    idproveedor,
                    nombreproveedor,
                    idmovcaja,
                    estado,
                    observacion
                )
                VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING codigo
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.codigoTipoComprobante
            );

            ps.setDouble(
                    2,
                    request.total
            );

            ps.setInt(
                    3,
                    request.codigoUsuario
            );

            ps.setString(
                    4,
                    normalizarTexto(request.nombreUsuario)
            );

            ps.setInt(
                    5,
                    request.codigoProveedor
            );

            ps.setString(
                    6,
                    normalizarTexto(request.nombreProveedor)
            );

            ps.setInt(
                    7,
                    codigoMovimientoCaja
            );

            ps.setInt(
                    8,
                    10
            );

            ps.setString(
                    9,
                    normalizarTexto(request.observacion)
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("codigo");
                }
            }
        }

        return -1;
    }

    private void insertarMediosPago(
            Connection connection,
            List<CompCompraDTO.MedioPagoRequest> pagos,
            int codigoMovimientoCaja
    ) throws SQLException {

        String sql = """
                INSERT INTO util.pago (
                    tipo,
                    idmovcaja,
                    importe,
                    id_medio_pago,
                    referencia
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            for (CompCompraDTO.MedioPagoRequest pago :
                    pagos) {

                ps.setInt(
                        1,
                        pago.tipoLegacy
                );

                ps.setInt(
                        2,
                        codigoMovimientoCaja
                );

                ps.setDouble(
                        3,
                        -Math.abs(pago.importe)
                );

                if (pago.idMedioPago > 0) {
                    ps.setInt(
                            4,
                            pago.idMedioPago
                    );
                } else {
                    ps.setNull(
                            4,
                            Types.INTEGER
                    );
                }

                ps.setString(
                        5,
                        normalizarTexto(pago.referencia)
                );

                ps.addBatch();
            }

            int[] resultados =
                    ps.executeBatch();

            if (resultados.length != pagos.size()) {
                throw new SQLException(
                        "No se insertaron todos los medios de pago"
                );
            }

            for (int resultado : resultados) {
                if (resultado == PreparedStatement.EXECUTE_FAILED) {
                    throw new SQLException(
                            "Falló la inserción de un medio de pago"
                    );
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER PAGO
    // -------------------------------------------------------------------------

    public CompCompraDTO.PagoDTO obtenerPago(
            int codigoPago
    ) throws SQLException {

        String sql = """
                SELECT codigo,
                       fecha,
                       total,
                       idvendedor,
                       nombrevendedor,
                       idproveedor,
                       nombreproveedor,
                       idmovcaja,
                       estado,
                       observacion,
                       tipo
                FROM util.comprobantecomprapago
                WHERE codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(
                    1,
                    codigoPago
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                CompCompraDTO.PagoDTO dto =
                        new CompCompraDTO.PagoDTO();

                dto.codigo =
                        rs.getInt("codigo");

                dto.fecha =
                        rs.getTimestamp("fecha");

                dto.total =
                        rs.getDouble("total");

                dto.idVendedor =
                        rs.getInt("idvendedor");

                dto.nombreVendedor =
                        rs.getString("nombrevendedor");

                dto.idProveedor =
                        rs.getInt("idproveedor");

                dto.nombreProveedor =
                        rs.getString("nombreproveedor");

                dto.idMovimientoCaja =
                        rs.getInt("idmovcaja");

                dto.estado =
                        rs.getInt("estado");

                dto.observacion =
                        rs.getString("observacion");

                dto.tipo =
                        rs.getInt("tipo");

                return dto;
            }
        }
    }

    // -------------------------------------------------------------------------
    // CUENTA CORRIENTE
    // -------------------------------------------------------------------------

    public CompCompraDTO.CuentaCorrienteDTO obtenerCuentaCorriente(
            int codigoProveedor,
            java.util.Date desde,
            java.util.Date hasta
    ) throws SQLException {

        CompCompraDTO.CuentaCorrienteDTO resultado =
                new CompCompraDTO.CuentaCorrienteDTO();

        Calendar calendario =
                Calendar.getInstance();

        calendario.setTime(desde);
        calendario.add(
                Calendar.DAY_OF_YEAR,
                -1
        );

        java.util.Date fechaAnterior =
                calendario.getTime();

        String sql = """
                SELECT origen,
                       codigo_tipo,
                       descripcion,
                       codigo,
                       fecha,
                       total,
                       id_movimiento_caja
                FROM (
                    SELECT 3 AS origen,
                           1 AS codigo_tipo,
                           'Saldo Ant.' AS descripcion,
                           0 AS codigo,
                           ?::timestamp AS fecha,
                           COALESCE(
                               util.saldoProvHasta(?, ?),
                               0
                           ) AS total,
                           ? AS proveedor,
                           0 AS id_movimiento_caja

                    UNION ALL

                    SELECT 1 AS origen,
                           codigocompcompra AS codigo_tipo,
                           CONCAT(
                               '  ',
                               letra,
                               ' ',
                               centro,
                               ' - ',
                               numero
                           ) AS descripcion,
                           id AS codigo,
                           fechacomprobante AS fecha,
                           total,
                           nroproveedor AS proveedor,
                           0 AS id_movimiento_caja
                    FROM util.comprobantecompracab
                    WHERE CAST(fechacomprobante AS DATE) >= ?

                    UNION ALL

                    SELECT 2 AS origen,
                           tipo AS codigo_tipo,
                           CONCAT('  ', codigo) AS descripcion,
                           codigo,
                           fecha,
                           total,
                           idproveedor AS proveedor,
                           idmovcaja AS id_movimiento_caja
                    FROM util.comprobantecomprapago
                    WHERE CAST(fecha AS DATE) >= ?
                ) movimientos
                WHERE proveedor = ?
                  AND CAST(fecha AS DATE) <= ?
                ORDER BY fecha ASC,
                         codigo ASC
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            int index = 1;

            ps.setTimestamp(
                    index++,
                    new Timestamp(fechaAnterior.getTime())
            );

            ps.setInt(
                    index++,
                    codigoProveedor
            );

            ps.setTimestamp(
                    index++,
                    new Timestamp(fechaAnterior.getTime())
            );

            ps.setInt(
                    index++,
                    codigoProveedor
            );

            ps.setDate(
                    index++,
                    new Date(desde.getTime())
            );

            ps.setDate(
                    index++,
                    new Date(desde.getTime())
            );

            ps.setInt(
                    index++,
                    codigoProveedor
            );

            ps.setDate(
                    index,
                    new Date(hasta.getTime())
            );

            try (ResultSet rs = ps.executeQuery()) {
                double saldoAcumulado = 0;

                while (rs.next()) {
                    CompCompraDTO.MovimientoCuentaCorrienteDTO movimiento =
                            new CompCompraDTO.MovimientoCuentaCorrienteDTO();

                    movimiento.origen =
                            rs.getInt("origen");

                    movimiento.codigoTipo =
                            rs.getInt("codigo_tipo");

                    movimiento.codigo =
                            rs.getInt("codigo");

                    movimiento.idMovimientoCaja =
                            rs.getInt("id_movimiento_caja");

                    movimiento.fecha =
                            rs.getTimestamp("fecha");

                    double total =
                            rs.getDouble("total");

                    String detalle =
                            rs.getString("descripcion");

                    if (movimiento.origen == 1) {
                        boolean esCompra =
                                movimiento.codigoTipo == 1;

                        movimiento.descripcion =
                                (esCompra
                                        ? "Comprobante"
                                        : "Devolución")
                                        + detalle;

                        if (esCompra) {
                            movimiento.debe = total;
                        } else {
                            movimiento.haber = total;
                        }

                    } else if (movimiento.origen == 2) {
                        boolean esPagoCompra =
                                movimiento.codigoTipo == 1;

                        movimiento.descripcion =
                                (esPagoCompra
                                        ? "Pago Comprobante"
                                        : "Pago Devolución")
                                        + detalle;

                        if (esPagoCompra) {
                            movimiento.haber = total;
                        } else {
                            movimiento.debe = total;
                        }

                    } else {
                        movimiento.descripcion =
                                detalle;

                        saldoAcumulado += total;
                    }

                    resultado.totalDebe +=
                            movimiento.debe;

                    resultado.totalHaber +=
                            movimiento.haber;

                    saldoAcumulado +=
                            movimiento.debe
                                    - movimiento.haber;

                    movimiento.saldo =
                            redondear(
                                    saldoAcumulado,
                                    2
                            );

                    resultado.movimientos.add(
                            movimiento
                    );
                }

                resultado.totalDebe =
                        redondear(
                                resultado.totalDebe,
                                2
                        );

                resultado.totalHaber =
                        redondear(
                                resultado.totalHaber,
                                2
                        );

                resultado.saldo =
                        redondear(
                                saldoAcumulado,
                                2
                        );
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // OBTENER COMPROBANTE
    // -------------------------------------------------------------------------

    public CompCompraDTO.ComprobanteDTO obtenerComprobante(
            int codigoComprobante
    ) throws SQLException {

        String sqlCabecera = """
                SELECT ccc.id,
                       ccc.nroproveedor,
                       prov.razonsocial,
                       ccc.codigocompcompra,
                       ccc.fechacomprobante,
                       ccc.periodoiva,
                       ccc.sistemacomprobante,
                       ccc.letra,
                       ccc.centro,
                       ccc.numero,
                       ccc.netogravado,
                       ccc.conceptosnogravados,
                       ccc.exentos,
                       ccc.periibb,
                       ccc.periva,
                       ccc.perimpinternos,
                       ccc.perimpmunicipales,
                       ccc.perotrosimp,
                       ccc.retiva,
                       ccc.retganancias,
                       ccc.total,
                       ccc.fecharegistracioncomprobante,
                       ccc.usuarioregistracion
                FROM util.comprobantecompracab ccc
                INNER JOIN util.proveedor prov
                        ON prov.codigo = ccc.nroproveedor
                WHERE ccc.id = ?
                """;

        String sqlRenglones = """
                SELECT idcomprobante,
                       idrenglon,
                       codigocompcompra,
                       codigoarticulo,
                       codigoproveedorarticulo,
                       detallearticulo,
                       cantidad,
                       preciobase,
                       porcentajebonificacion,
                       preciobonificado,
                       tasaiva,
                       totalfinal
                FROM util.comprobantecomprareng
                WHERE idcomprobante = ?
                ORDER BY idrenglon ASC
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            CompCompraDTO.ComprobanteDTO comprobante =
                    obtenerCabeceraComprobante(
                            connection,
                            sqlCabecera,
                            codigoComprobante
                    );

            if (comprobante == null) {
                return null;
            }

            obtenerRenglonesComprobante(
                    connection,
                    sqlRenglones,
                    comprobante
            );

            return comprobante;
        }
    }

    private CompCompraDTO.ComprobanteDTO obtenerCabeceraComprobante(
            Connection connection,
            String sql,
            int codigoComprobante
    ) throws SQLException {

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoComprobante
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                CompCompraDTO.ComprobanteDTO dto =
                        new CompCompraDTO.ComprobanteDTO();

                dto.id =
                        rs.getInt("id");

                dto.codigoProveedor =
                        rs.getInt("nroproveedor");

                dto.nombreProveedor =
                        rs.getString("razonsocial");

                dto.codigoTipoComprobante =
                        rs.getInt("codigocompcompra");

                dto.fechaComprobante =
                        rs.getTimestamp("fechacomprobante");

                dto.periodoIVA =
                        rs.getTimestamp("periodoiva");

                dto.sistemaComprobante =
                        rs.getInt("sistemacomprobante");

                dto.letra =
                        rs.getString("letra");

                dto.centro =
                        rs.getString("centro");

                dto.numero =
                        rs.getString("numero");

                dto.netoGravado =
                        rs.getDouble("netogravado");

                dto.conceptosNoGravados =
                        rs.getDouble("conceptosnogravados");

                dto.exentos =
                        rs.getDouble("exentos");

                dto.percepcionIIBB =
                        rs.getDouble("periibb");

                dto.percepcionIVA =
                        rs.getDouble("periva");

                dto.percepcionImpuestosInternos =
                        rs.getDouble("perimpinternos");

                dto.percepcionMunicipal =
                        rs.getDouble("perimpmunicipales");

                dto.percepcionOtros =
                        rs.getDouble("perotrosimp");

                dto.retencionIVA =
                        rs.getDouble("retiva");

                dto.retencionGanancias =
                        rs.getDouble("retganancias");

                dto.total =
                        rs.getDouble("total");

                dto.fechaRegistracion =
                        rs.getTimestamp(
                                "fecharegistracioncomprobante"
                        );

                dto.codigoUsuario =
                        rs.getInt("usuarioregistracion");

                return dto;
            }
        }
    }

    private void obtenerRenglonesComprobante(
            Connection connection,
            String sql,
            CompCompraDTO.ComprobanteDTO comprobante
    ) throws SQLException {

        try (PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    comprobante.id
            );

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CompCompraDTO.RenglonDTO renglon =
                            new CompCompraDTO.RenglonDTO();

                    renglon.idComprobante =
                            rs.getInt("idcomprobante");

                    renglon.numeroRenglon =
                            rs.getInt("idrenglon");

                    renglon.codigoTipoComprobante =
                            rs.getInt("codigocompcompra");

                    renglon.codigoArticulo =
                            rs.getString("codigoarticulo");

                    renglon.codigoProveedorArticulo =
                            rs.getInt(
                                    "codigoproveedorarticulo"
                            );

                    renglon.descripcion =
                            rs.getString("detallearticulo");

                    renglon.cantidad =
                            rs.getDouble("cantidad");

                    renglon.precioBase =
                            rs.getDouble("preciobase");

                    renglon.porcentajeBonificacion =
                            rs.getDouble(
                                    "porcentajebonificacion"
                            );

                    renglon.precioBonificado =
                            rs.getDouble("preciobonificado");

                    renglon.tasaIVA =
                            rs.getDouble("tasaiva");

                    renglon.totalFinal =
                            rs.getDouble("totalfinal");

                    comprobante.renglones.add(
                            renglon
                    );
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // LIBRO IVA
    // -------------------------------------------------------------------------

    public List<CompCompraDTO.LibroIvaCompraDTO> listarLibroIva(
            java.util.Date desde,
            java.util.Date hasta
    ) throws SQLException {

        List<CompCompraDTO.LibroIvaCompraDTO> resultado =
                new ArrayList<>();

        String sql = """
                WITH impuestos AS (
                    SELECT reng.idcomprobante,
                           SUM(
                               CASE
                                   WHEN reng.tasaiva = 10.5
                                   THEN reng.totalfinal
                                        - (reng.preciobase * reng.cantidad)
                                   ELSE 0
                               END
                           ) AS iva105,
                           SUM(
                               CASE
                                   WHEN reng.tasaiva = 21
                                   THEN reng.totalfinal
                                        - (reng.preciobase * reng.cantidad)
                                   ELSE 0
                               END
                           ) AS iva21
                    FROM util.comprobantecomprareng reng
                    GROUP BY reng.idcomprobante
                )
                SELECT TO_CHAR(
                           compra.fechacomprobante,
                           'DD/MM/YYYY'
                       ) AS fecha,
                       CONCAT(
                           compra.letra,
                           ' ',
                           compra.centro,
                           ' - ',
                           compra.numero
                       ) AS descripcion,
                       proveedor.codigoidentificacion AS cuit,
                       proveedor.razonsocial AS proveedor,
                       COALESCE(compra.netogravado, 0) AS neto,
                       COALESCE(impuestos.iva105, 0) AS iva105,
                       COALESCE(impuestos.iva21, 0) AS iva21,
                       COALESCE(
                           compra.conceptosnogravados,
                           0
                       ) AS conceptosnogravados,
                       COALESCE(compra.exentos, 0) AS exentos,
                       COALESCE(compra.periibb, 0) AS periibb,
                       COALESCE(compra.periva, 0) AS periva,
                       COALESCE(
                           compra.perimpinternos,
                           0
                       ) AS perimpinternos,
                       COALESCE(
                           compra.perimpmunicipales,
                           0
                       ) AS perimpmunicipales,
                       COALESCE(
                           compra.perotrosimp,
                           0
                       ) AS perotrosimp,
                       COALESCE(compra.retiva, 0) AS retiva,
                       COALESCE(
                           compra.retganancias,
                           0
                       ) AS retganancias,
                       COALESCE(compra.total, 0) AS total
                FROM util.comprobantecompracab compra
                INNER JOIN util.proveedor proveedor
                        ON proveedor.codigo =
                           compra.nroproveedor
                LEFT JOIN impuestos
                       ON impuestos.idcomprobante =
                          compra.id
                WHERE compra.fechacomprobante >= ?
                  AND compra.fechacomprobante <= ?
                ORDER BY compra.fechacomprobante ASC,
                         compra.id ASC
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setDate(
                    1,
                    new Date(desde.getTime())
            );

            ps.setDate(
                    2,
                    new Date(hasta.getTime())
            );

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CompCompraDTO.LibroIvaCompraDTO dto =
                            new CompCompraDTO.LibroIvaCompraDTO();

                    dto.setFecha(
                            rs.getString("fecha")
                    );

                    dto.setDescripcion(
                            rs.getString("descripcion")
                    );

                    dto.setCuit(
                            rs.getString("cuit")
                    );

                    dto.setProveedor(
                            rs.getString("proveedor")
                    );

                    dto.setNeto(
                            rs.getDouble("neto")
                    );

                    dto.setIva105(
                            rs.getDouble("iva105")
                    );

                    dto.setIva21(
                            rs.getDouble("iva21")
                    );

                    dto.setConceptosNoGravados(
                            rs.getDouble(
                                    "conceptosnogravados"
                            )
                    );

                    dto.setExentos(
                            rs.getDouble("exentos")
                    );

                    dto.setPercepcionIibb(
                            rs.getDouble("periibb")
                    );

                    dto.setPercepcionIva(
                            rs.getDouble("periva")
                    );

                    dto.setPercepcionInternos(
                            rs.getDouble(
                                    "perimpinternos"
                            )
                    );

                    dto.setPercepcionMunicipales(
                            rs.getDouble(
                                    "perimpmunicipales"
                            )
                    );

                    dto.setOtrosImpuestos(
                            rs.getDouble("perotrosimp")
                    );

                    dto.setRetencionIva(
                            rs.getDouble("retiva")
                    );

                    dto.setRetencionGanancias(
                            rs.getDouble(
                                    "retganancias"
                            )
                    );

                    dto.setTotal(
                            rs.getDouble("total")
                    );

                    resultado.add(dto);
                }
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // RESUMEN DEL PERÍODO
    // -------------------------------------------------------------------------

    public ResumenPeriodoDTO obtenerResumenPeriodo(
            java.util.Date desde,
            java.util.Date hasta
    ) throws SQLException {

        ResumenPeriodoDTO resultado =
                new ResumenPeriodoDTO();

        String sql = """
                WITH impuestos AS (
                    SELECT reng.idcomprobante,
                           SUM(
                               CASE
                                   WHEN reng.tasaiva IN (10.5, 21)
                                   THEN reng.totalfinal
                                        - (
                                            reng.preciobase
                                            * reng.cantidad
                                        )
                                   ELSE 0
                               END
                           ) AS iva
                    FROM util.comprobantecomprareng reng
                    GROUP BY reng.idcomprobante
                )
                SELECT COALESCE(
                           SUM(compra.netogravado),
                           0
                       ) AS neto,
                       COALESCE(
                           SUM(impuestos.iva),
                           0
                       ) AS iva,
                       COALESCE(
                           SUM(compra.total),
                           0
                       ) AS total
                FROM util.comprobantecompracab compra
                LEFT JOIN impuestos
                       ON impuestos.idcomprobante =
                          compra.id
                WHERE compra.fechacomprobante >= ?
                  AND compra.fechacomprobante <= ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection();

                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setDate(
                    1,
                    new Date(desde.getTime())
            );

            ps.setDate(
                    2,
                    new Date(hasta.getTime())
            );

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado.setNeto(
                            rs.getDouble("neto")
                    );

                    resultado.setIva(
                            rs.getDouble("iva")
                    );

                    resultado.setTotal(
                            rs.getDouble("total")
                    );
                }
            }
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // ANULAR COMPROBANTE
    // -------------------------------------------------------------------------

    public boolean anularComprobante(
            int codigoComprobante
    ) throws SQLException {

        String sqlRenglones = """
                DELETE FROM util.comprobantecomprareng
                WHERE idcomprobante = ?
                """;

        String sqlCabecera = """
                DELETE FROM util.comprobantecompracab
                WHERE id = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlRenglones
                             )) {

                    ps.setInt(
                            1,
                            codigoComprobante
                    );

                    ps.executeUpdate();
                }

                int cabecerasEliminadas;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlCabecera
                             )) {

                    ps.setInt(
                            1,
                            codigoComprobante
                    );

                    cabecerasEliminadas =
                            ps.executeUpdate();
                }

                if (cabecerasEliminadas != 1) {
                    connection.rollback();
                    return false;
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
    // ANULAR PAGO
    // -------------------------------------------------------------------------

    public boolean anularPago(
            int codigoPago,
            int codigoMovimientoCaja
    ) throws SQLException {

        String sqlComprobantePago = """
                DELETE FROM util.comprobantecomprapago
                WHERE codigo = ?
                  AND idmovcaja = ?
                """;

        String sqlMediosPago = """
                DELETE FROM util.pago
                WHERE idmovcaja = ?
                """;

        String sqlMovimientoCaja = """
                DELETE FROM util.movimientocaja
                WHERE codigo = ?
                """;

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                int comprobantesEliminados;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlComprobantePago
                             )) {

                    ps.setInt(
                            1,
                            codigoPago
                    );

                    ps.setInt(
                            2,
                            codigoMovimientoCaja
                    );

                    comprobantesEliminados =
                            ps.executeUpdate();
                }

                if (comprobantesEliminados != 1) {
                    connection.rollback();
                    return false;
                }

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlMediosPago
                             )) {

                    ps.setInt(
                            1,
                            codigoMovimientoCaja
                    );

                    ps.executeUpdate();
                }

                int movimientosEliminados;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     sqlMovimientoCaja
                             )) {

                    ps.setInt(
                            1,
                            codigoMovimientoCaja
                    );

                    movimientosEliminados =
                            ps.executeUpdate();
                }

                if (movimientosEliminados != 1) {
                    connection.rollback();
                    return false;
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
    // UTILIDADES
    // -------------------------------------------------------------------------

    private String normalizarTexto(
            String valor
    ) {
        return valor == null
                ? ""
                : valor.trim();
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
                    "Error ejecutando rollback de compra"
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
                    "Error restaurando autoCommit"
            );

            ex.printStackTrace();
        }
    }
}