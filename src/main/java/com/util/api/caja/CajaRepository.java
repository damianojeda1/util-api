package com.util.api.caja;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CajaRepository {

    private static final int ESTADO_ACTIVO = 10;

    private static final int ESTADO_CERRADA = 20;
    private static final int ESTADO_CERRADA_DIFERENCIA = 30;

    private static final int TIPO_APERTURA = 0;
    private static final int TIPO_INGRESO_MANUAL = 10;
    private static final int TIPO_EGRESO_MANUAL = 20;
    private static final int TIPO_ARQUEO = 30;

    @Inject
    DataSource dataSource;

    public CajaDTO obtenerUltimoCierre()
            throws SQLException {

        String sql = """
            SELECT importecierre,
                   importecierrevirtual
            FROM util.caja
            WHERE importecierre IS NOT NULL
            ORDER BY codigo DESC
            LIMIT 1
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (!rs.next()) {
                return null;
            }

            CajaDTO caja = new CajaDTO();

            double importeCierre =
                    rs.getDouble("importecierre");

            caja.importeCierre =
                    rs.wasNull()
                            ? null
                            : importeCierre;

            double importeCierreVirtual =
                    rs.getDouble("importecierrevirtual");

            caja.importeCierreVirtual =
                    rs.wasNull()
                            ? null
                            : importeCierreVirtual;

            return caja;
        }
    }

    public int abrirCaja(
            CajaDTO.AperturaRequest request
    ) throws SQLException {

        if (request == null) {
            return -1;
        }

        String sqlCaja = """
            INSERT INTO util.caja (
                importeapertura,
                importeaperturavirtual,
                idvendedor,
                nombrevendedor,
                estado,
                observacionapertura
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING codigo
        """;

        String sqlMovimiento = """
            INSERT INTO util.movimientocaja (
                idcaja,
                importe,
                estado,
                tipomovcaja
            )
            VALUES (?, ?, ?, ?)
        """;

        try (Connection cn = dataSource.getConnection()) {
            cn.setAutoCommit(false);

            try {
                int codigoCaja;

                try (PreparedStatement ps =
                             cn.prepareStatement(sqlCaja)) {

                    ps.setDouble(
                            1,
                            request.importeApertura
                    );

                    ps.setDouble(
                            2,
                            request.importeAperturaVirtual
                    );

                    ps.setInt(
                            3,
                            request.idVendedor
                    );

                    ps.setString(
                            4,
                            request.nombreVendedor
                    );

                    ps.setInt(
                            5,
                            ESTADO_ACTIVO
                    );

                    setNullableString(
                            ps,
                            6,
                            request.observacion
                    );

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            cn.rollback();
                            return -1;
                        }

                        codigoCaja =
                                rs.getInt("codigo");
                    }
                }

                try (PreparedStatement ps =
                             cn.prepareStatement(sqlMovimiento)) {

                    ps.setInt(1, codigoCaja);
                    ps.setDouble(
                            2,
                            request.importeApertura
                    );
                    ps.setInt(3, ESTADO_ACTIVO);
                    ps.setInt(4, TIPO_APERTURA);

                    if (ps.executeUpdate() != 1) {
                        cn.rollback();
                        return -1;
                    }
                }

                cn.commit();

                return codigoCaja;

            } catch (Exception ex) {
                cn.rollback();

                if (ex instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                        "Error abriendo caja",
                        ex
                );
            }
        }
    }

    public List<CajaDTO> obtenerHistoricas()
            throws SQLException {

        List<CajaDTO> resultado =
                new ArrayList<>();

        String sql = """
            SELECT caj.codigo,
                   caj.fechaapertura,
                   caj.fechacierre,
                   caj.importeapertura,
                   caj.importeaperturavirtual,
                   caj.importecierre,
                   caj.importecierrevirtual,
                   caj.idvendedor,
                   caj.nombrevendedor,
                   caj.estado,
                   COALESCE(
                       caj.observacionapertura,
                       ''
                   ) AS observacionapertura,
                   COALESCE(
                       caj.observacioncierre,
                       ''
                   ) AS observacioncierre
            FROM util.caja caj
            ORDER BY caj.codigo DESC
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                resultado.add(mapCaja(rs));
            }
        }

        return resultado;
    }

    public CajaDTO obtenerDetalle(
            int codigoCaja,
            boolean incluirPagosProveedor
    ) throws SQLException {

        CajaDTO caja = obtenerCaja(codigoCaja);

        if (caja == null) {
            return null;
        }

        caja.movimientos = obtenerMovimientos(
                codigoCaja,
                incluirPagosProveedor
        );

        caja.resumenPagos = obtenerResumenPagos(
                codigoCaja,
                incluirPagosProveedor
        );

        calcularTotales(caja);
        calcularTotalesPorTipoCliente(codigoCaja, caja.totales);

        return caja;
    }

    private void calcularTotalesPorTipoCliente(
            int codigoCaja,
            CajaDTO.TotalesDTO totales
    ) throws SQLException {

        String sql = """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN COALESCE(r.mayorista, FALSE) = FALSE
                            THEN p.importe
                        ELSE 0
                    END
                ),
                0
            ) AS estandar,

            COALESCE(
                SUM(
                    CASE
                        WHEN COALESCE(r.mayorista, FALSE) = TRUE
                            THEN p.importe
                        ELSE 0
                    END
                ),
                0
            ) AS mayorista

        FROM util.movimientocaja mov

        INNER JOIN util.recibo r
                ON r.idmovcaja = mov.codigo

        INNER JOIN util.pago p
                ON p.idmovcaja = mov.codigo

        WHERE mov.idcaja = ?
          AND mov.estado = ?
          AND r.estado = ?
          AND mov.tipomovcaja = 40
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(1, codigoCaja);
            ps.setInt(2, ESTADO_ACTIVO);
            ps.setInt(3, ESTADO_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totales.totalVentasEstandar =
                            rs.getDouble("estandar");

                    totales.totalVentasMayorista =
                            rs.getDouble("mayorista");
                }
            }
        }
    }

    private CajaDTO obtenerCaja(
            int codigoCaja
    ) throws SQLException {

        String sql = """
            SELECT caj.codigo,
                   caj.fechaapertura,
                   caj.fechacierre,
                   caj.importeapertura,
                   COALESCE(
                       caj.importeaperturavirtual,
                       0
                   ) AS importeaperturavirtual,
                   caj.importecierre,
                   caj.importecierrevirtual,
                   caj.idvendedor,
                   caj.nombrevendedor,
                   caj.estado,
                   COALESCE(
                       caj.observacionapertura,
                       ''
                   ) AS observacionapertura,
                   COALESCE(
                       caj.observacioncierre,
                       ''
                   ) AS observacioncierre
            FROM util.caja caj
            WHERE caj.codigo = ?
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoCaja);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return mapCaja(rs);
            }
        }
    }

    private List<CajaDTO.MovimientoDTO> obtenerMovimientos(
            int codigoCaja,
            boolean incluirPagosProveedor
    ) throws SQLException {

        List<CajaDTO.MovimientoDTO> resultado =
                new ArrayList<>();

        String sql = """
            SELECT mov.codigo AS nromovimiento,
                   TO_CHAR(
                       mov.creado,
                       'DD/MM/YYYY HH24:MI'
                   ) AS creado,
                   mov.importe,
                   mov.tipomovcaja,
                   mov.estado AS estadomovimiento,
                   tipo.descripcion AS descripcionmovimiento,
                   TRIM(
                       COALESCE(
                           mov.observacion,
                           ''
                       )
                   ) AS observacion,
                   COALESCE(
                       pagos.tienefisico,
                       FALSE
                   ) AS tienefisico,
                   COALESCE(
                       pagos.tienevirtual,
                       FALSE
                   ) AS tienevirtual
            FROM util.movimientocaja mov
            INNER JOIN util.tipomovimientocaja tipo
                    ON tipo.codigo = mov.tipomovcaja
            LEFT JOIN (
                SELECT p.idmovcaja,
                       BOOL_OR(
                           COALESCE(
                               mp.es_fisico,
                               p.tipo = 1
                           )
                       ) AS tienefisico,
                       BOOL_OR(
                           NOT COALESCE(
                               mp.es_fisico,
                               p.tipo = 1
                           )
                       ) AS tienevirtual
                FROM util.pago p
                LEFT JOIN util.medio_pago mp
                       ON mp.id = p.id_medio_pago
                GROUP BY p.idmovcaja
            ) pagos
                   ON pagos.idmovcaja = mov.codigo
            WHERE mov.idcaja = ?
        """ + (
                incluirPagosProveedor
                        ? ""
                        : " AND mov.tipomovcaja <> 50 "
        ) + """
            ORDER BY mov.codigo DESC
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoCaja);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CajaDTO.MovimientoDTO movimiento =
                            new CajaDTO.MovimientoDTO();

                    movimiento.codigo =
                            rs.getInt("nromovimiento");

                    movimiento.tipoMovimiento =
                            rs.getInt("tipomovcaja");

                    movimiento.fecha =
                            rs.getString("creado");

                    movimiento.detalle =
                            rs.getString(
                                    "descripcionmovimiento"
                            )
                                    + (
                                    rs.getInt(
                                            "estadomovimiento"
                                    ) == ESTADO_ACTIVO
                                            ? ""
                                            : " (ANULADO)"
                            );

                    movimiento.observacion =
                            rs.getString("observacion");

                    movimiento.importe =
                            rs.getDouble("importe");

                    movimiento.origenPago =
                            determinarOrigenPago(
                                    movimiento.tipoMovimiento,
                                    rs.getBoolean(
                                            "tienefisico"
                                    ),
                                    rs.getBoolean(
                                            "tienevirtual"
                                    )
                            );

                    resultado.add(movimiento);
                }
            }
        }

        return resultado;
    }

    public List<CajaDTO.PagoDTO> obtenerPagosMovimiento(
            int codigoMovimiento
    ) throws SQLException {

        List<CajaDTO.PagoDTO> resultado =
                new ArrayList<>();

        String sql = """
            SELECT p.tipo,
                   p.id_medio_pago,
                   p.importe,
                   mp.descripcion,
                   mp.es_fisico
            FROM util.pago p
            LEFT JOIN util.medio_pago mp
                   ON mp.id = p.id_medio_pago
            WHERE p.idmovcaja = ?
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoMovimiento);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tipoLegacy =
                            rs.getInt("tipo");

                    int idMedio =
                            rs.getInt("id_medio_pago");

                    boolean tieneMedio =
                            !rs.wasNull();

                    String descripcion;
                    boolean fisico;

                    if (tieneMedio) {
                        descripcion =
                                rs.getString("descripcion");

                        if (descripcion == null
                                || descripcion.isBlank()) {
                            descripcion =
                                    "Medio " + idMedio;
                        }

                        fisico =
                                rs.getBoolean("es_fisico");

                        if (rs.wasNull()) {
                            fisico = tipoLegacy == 1;
                        }

                    } else {
                        descripcion =
                                descripcionLegacy(tipoLegacy);

                        fisico = tipoLegacy == 1;
                    }

                    resultado.add(
                            new CajaDTO.PagoDTO(
                                    descripcion,
                                    rs.getDouble("importe"),
                                    fisico
                            )
                    );
                }
            }
        }

        return resultado;
    }

    public CajaDTO.ImpresionCajaDTO obtenerParaImpresion(
            int codigoCaja
    ) throws SQLException {

        if (codigoCaja <= 0) {
            return null;
        }

        String sql = """
            SELECT codigo,
                   fechaapertura,
                   fechacierre,
                   COALESCE(
                       importeapertura,
                       0
                   ) AS importeapertura,
                   COALESCE(
                       importeaperturavirtual,
                       0
                   ) AS importeaperturavirtual,
                   COALESCE(
                       importecierre,
                       0
                   ) AS importecierre,
                   COALESCE(
                       importecierrevirtual,
                       0
                   ) AS importecierrevirtual,
                   COALESCE(
                       nombrevendedor,
                       ''
                   ) AS nombrevendedor,
                   COALESCE(
                       observacionapertura,
                       ''
                   ) AS observacionapertura,
                   COALESCE(
                       observacioncierre,
                       ''
                   ) AS observacioncierre,
                   estado
            FROM util.caja
            WHERE codigo = ?
        """;

        try (Connection cn = dataSource.getConnection()) {
            CajaDTO.ImpresionCajaDTO dto;

            try (PreparedStatement ps =
                         cn.prepareStatement(sql)) {

                ps.setInt(1, codigoCaja);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    dto =
                            new CajaDTO.ImpresionCajaDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.fechaApertura =
                            rs.getTimestamp(
                                    "fechaapertura"
                            );

                    dto.fechaCierre =
                            rs.getTimestamp(
                                    "fechacierre"
                            );

                    dto.importeApertura =
                            rs.getDouble(
                                    "importeapertura"
                            );

                    dto.importeAperturaVirtual =
                            rs.getDouble(
                                    "importeaperturavirtual"
                            );

                    dto.importeCierre =
                            rs.getDouble(
                                    "importecierre"
                            );

                    dto.importeCierreVirtual =
                            rs.getDouble(
                                    "importecierrevirtual"
                            );

                    dto.nombreVendedor =
                            rs.getString(
                                    "nombrevendedor"
                            );

                    dto.observacionApertura =
                            rs.getString(
                                    "observacionapertura"
                            );

                    dto.observacionCierre =
                            rs.getString(
                                    "observacioncierre"
                            );

                    dto.estado =
                            rs.getInt("estado");
                }
            }

            dto.totales =
                    obtenerTotalesParaImpresion(
                            cn,
                            codigoCaja
                    );

            dto.mediosPago =
                    obtenerMediosPagoParaImpresion(
                            cn,
                            codigoCaja
                    );

            return dto;
        }
    }

    private CajaDTO.TotalesDTO obtenerTotalesParaImpresion(
            Connection cn,
            int codigoCaja
    ) throws SQLException {

        CajaDTO.TotalesDTO totales =
                new CajaDTO.TotalesDTO();

        obtenerMovimientosManualesParaImpresion(
                cn,
                codigoCaja,
                totales
        );

        obtenerPagosParaImpresion(
                cn,
                codigoCaja,
                totales
        );

        totales.totalFisico =
                totales.cajaInicial
                        + totales.ingresosManuales
                        + totales.egresosManuales
                        + totales.arqueos
                        + totales.pagosFisicos
                        + totales.egresosFisicos;

        totales.totalVirtual =
                totales.pagosVirtuales
                        + totales.egresosVirtuales;

        totales.totalVentas =
                totales.pagosFisicos
                        + totales.pagosVirtuales;

        return totales;
    }

    private void obtenerPagosParaImpresion(
            Connection cn,
            int codigoCaja,
            CajaDTO.TotalesDTO totales
    ) throws SQLException {

        String sql = """
            SELECT mov.tipomovcaja,
                   pago.tipo,
                   pago.id_medio_pago,
                   mov.importe AS importe_movimiento,
                   SUM(pago.importe) AS importe_pago,
                   mp.es_fisico
            FROM util.movimientocaja mov
            INNER JOIN util.pago pago
                    ON pago.idmovcaja = mov.codigo
            LEFT JOIN util.medio_pago mp
                   ON mp.id = pago.id_medio_pago
            WHERE mov.idcaja = ?
              AND mov.estado = ?
            GROUP BY mov.tipomovcaja,
                     pago.tipo,
                     pago.id_medio_pago,
                     mov.importe,
                     mp.es_fisico
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(1, codigoCaja);
            ps.setInt(2, ESTADO_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tipoMovimiento =
                            rs.getInt("tipomovcaja");

                    int tipoLegacy =
                            rs.getInt("tipo");

                    double importeMovimiento =
                            rs.getDouble(
                                    "importe_movimiento"
                            );

                    double importePago =
                            rs.getDouble(
                                    "importe_pago"
                            );

                    rs.getInt("id_medio_pago");

                    boolean tieneMedioPago =
                            !rs.wasNull();

                    boolean fisico;

                    if (tieneMedioPago) {
                        fisico =
                                rs.getBoolean("es_fisico");

                        if (rs.wasNull()) {
                            fisico =
                                    tipoLegacy == 1;
                        }

                    } else {
                        fisico =
                                tipoLegacy == 1;
                    }

                    double signo =
                            importeMovimiento < 0
                                    ? -1d
                                    : 1d;

                    double importeFinal =
                            importePago * signo;

                    if (tipoMovimiento == 40) {
                        if (fisico) {
                            totales.pagosFisicos +=
                                    importeFinal;
                        } else {
                            totales.pagosVirtuales +=
                                    importeFinal;
                        }
                    }

                    if (tipoMovimiento == 50
                            || tipoMovimiento == 51) {

                        if (fisico) {
                            totales.egresosFisicos +=
                                    importeFinal;
                        } else {
                            totales.egresosVirtuales +=
                                    importeFinal;
                        }
                    }
                }
            }
        }
    }

    private void obtenerMovimientosManualesParaImpresion(
            Connection cn,
            int codigoCaja,
            CajaDTO.TotalesDTO totales
    ) throws SQLException {

        String sql = """
            SELECT tipomovcaja,
                   COALESCE(
                       SUM(importe),
                       0
                   ) AS total
            FROM util.movimientocaja
            WHERE idcaja = ?
              AND estado = ?
            GROUP BY tipomovcaja
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(1, codigoCaja);
            ps.setInt(2, ESTADO_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tipoMovimiento =
                            rs.getInt("tipomovcaja");

                    double total =
                            rs.getDouble("total");

                    switch (tipoMovimiento) {
                        case TIPO_APERTURA:
                            totales.cajaInicial += total;
                            break;

                        case TIPO_INGRESO_MANUAL:
                            totales.ingresosManuales += total;
                            break;

                        case TIPO_EGRESO_MANUAL:
                            totales.egresosManuales += total;
                            break;

                        case TIPO_ARQUEO:
                            totales.arqueos += total;
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    private List<CajaDTO.ResumenMedioPagoDTO>
    obtenerMediosPagoParaImpresion(
            Connection cn,
            int codigoCaja
    ) throws SQLException {

        List<CajaDTO.ResumenMedioPagoDTO> resultado =
                new ArrayList<>();

        String sql = """
        SELECT
            COALESCE(
                mp.descripcion,
                CASE pago.tipo
                    WHEN 1 THEN 'Efectivo'
                    WHEN 2 THEN 'Débito'
                    WHEN 3 THEN 'Crédito'
                    WHEN 4 THEN 'Transferencia'
                    WHEN 5 THEN 'Otros'
                    ELSE 'Tipo ' || pago.tipo
                END
            ) AS medio_pago,

            COALESCE(
                mp.es_fisico,
                pago.tipo = 1
            ) AS es_fisico,

            movcaja.tipomovcaja,

            COALESCE(
                recibo.mayorista,
                FALSE
            ) AS mayorista,

            SUM(
                CASE
                    WHEN movcaja.importe < 0
                        THEN pago.importe * -1
                    ELSE pago.importe
                END
            ) AS total

        FROM util.movimientocaja movcaja

        INNER JOIN util.pago pago
                ON pago.idmovcaja = movcaja.codigo

        LEFT JOIN util.medio_pago mp
               ON mp.id = pago.id_medio_pago

        LEFT JOIN util.recibo recibo
               ON recibo.idmovcaja = movcaja.codigo
              AND recibo.estado = ?

        WHERE movcaja.idcaja = ?
          AND movcaja.estado = ?

        GROUP BY
            pago.tipo,
            mp.descripcion,
            mp.es_fisico,
            movcaja.tipomovcaja,
            recibo.mayorista

        ORDER BY
            COALESCE(
                recibo.mayorista,
                FALSE
            ),
            COALESCE(
                mp.es_fisico,
                pago.tipo = 1
            ) DESC,
            mp.descripcion,
            pago.tipo
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(1, ESTADO_ACTIVO);
            ps.setInt(2, codigoCaja);
            ps.setInt(3, ESTADO_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CajaDTO.ResumenMedioPagoDTO dto =
                            new CajaDTO.ResumenMedioPagoDTO();

                    dto.descripcion =
                            rs.getString("medio_pago");

                    dto.fisico =
                            rs.getBoolean("es_fisico");

                    dto.tipoMovimiento =
                            rs.getInt("tipomovcaja");

                    dto.mayorista =
                            rs.getBoolean("mayorista");

                    dto.total =
                            rs.getDouble("total");

                    resultado.add(dto);
                }
            }
        }

        return resultado;
    }

    public boolean insertarMovimiento(
            CajaDTO.MovimientoRequest request
    ) throws SQLException {

        if (request == null
                || request.codigoCaja <= 0) {
            return false;
        }

        String sql = """
            INSERT INTO util.movimientocaja (
                idcaja,
                importe,
                estado,
                tipomovcaja,
                observacion
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(
                    1,
                    request.codigoCaja
            );

            ps.setDouble(
                    2,
                    request.importe
            );

            ps.setInt(
                    3,
                    ESTADO_ACTIVO
            );

            ps.setInt(
                    4,
                    request.tipoMovimiento
            );

            setNullableString(
                    ps,
                    5,
                    request.observacion
            );

            return ps.executeUpdate() == 1;
        }
    }

    public boolean cerrarCaja(
            CajaDTO.CierreRequest request
    ) throws SQLException {

        if (request == null
                || request.codigoCaja <= 0) {
            return false;
        }

        try (Connection cn = dataSource.getConnection()) {
            cn.setAutoCommit(false);

            try {
                if (request.crearArqueo) {
                    insertarArqueo(
                            cn,
                            request
                    );
                }

                int actualizadas =
                        actualizarCierreCaja(
                                cn,
                                request
                        );

                if (actualizadas != 1) {
                    cn.rollback();
                    return false;
                }

                cn.commit();
                return true;

            } catch (Exception ex) {
                cn.rollback();

                if (ex instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(
                        "Error cerrando caja",
                        ex
                );
            }
        }
    }

    private void insertarArqueo(
            Connection cn,
            CajaDTO.CierreRequest request
    ) throws SQLException {

        String sql = """
            INSERT INTO util.movimientocaja (
                idcaja,
                importe,
                estado,
                tipomovcaja
            )
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.codigoCaja
            );

            ps.setDouble(
                    2,
                    request.importeArqueo
            );

            ps.setInt(
                    3,
                    ESTADO_ACTIVO
            );

            ps.setInt(
                    4,
                    TIPO_ARQUEO
            );

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo insertar el arqueo"
                );
            }
        }
    }

    private int actualizarCierreCaja(
            Connection cn,
            CajaDTO.CierreRequest request
    ) throws SQLException {

        boolean tieneDiferenciaFisica =
                Math.abs(
                        request.importeArqueo
                ) >= 0.01;

        int estado =
                tieneDiferenciaFisica
                        ? ESTADO_CERRADA_DIFERENCIA
                        : ESTADO_CERRADA;

        String sql = """
            UPDATE util.caja
            SET fechacierre = NOW(),
                importecierre = ?,
                importecierrevirtual = ?,
                estado = ?,
                observacioncierre = ?
            WHERE codigo = ?
        """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setDouble(
                    1,
                    request.importeCierre
            );

            ps.setDouble(
                    2,
                    request.importeCierreVirtual
            );

            ps.setInt(
                    3,
                    estado
            );

            setNullableString(
                    ps,
                    4,
                    request.observacionCierre
            );

            ps.setInt(
                    5,
                    request.codigoCaja
            );

            return ps.executeUpdate();
        }
    }

    private List<CajaDTO.ResumenPagoDTO> obtenerResumenPagos(
            int codigoCaja,
            boolean incluirPagosProveedor
    ) throws SQLException {

        List<CajaDTO.ResumenPagoDTO> resultado =
                new ArrayList<>();

        String sql = """
            SELECT COALESCE(
                       mp.descripcion,
                       CASE pago.tipo
                           WHEN 1 THEN 'Efectivo'
                           WHEN 2 THEN 'Débito'
                           WHEN 3 THEN 'Crédito'
                           WHEN 4 THEN 'Transferencia'
                           WHEN 5 THEN 'Otros'
                           ELSE 'Tipo ' || pago.tipo
                       END
                   ) AS mediopago,
                   COALESCE(
                       mp.es_fisico,
                       pago.tipo = 1
                   ) AS esfisico,
                   mov.tipomovcaja,
                   SUM(
                       CASE
                           WHEN mov.importe < 0
                               THEN pago.importe * -1
                           ELSE pago.importe
                       END
                   ) AS total
            FROM util.movimientocaja mov
            INNER JOIN util.pago pago
                    ON pago.idmovcaja = mov.codigo
            LEFT JOIN util.medio_pago mp
                   ON mp.id = pago.id_medio_pago
            WHERE mov.idcaja = ?
              AND mov.estado = ?
        """ + (
                incluirPagosProveedor
                        ? ""
                        : " AND mov.tipomovcaja <> 50 "
        ) + """
            GROUP BY pago.tipo,
                     mp.descripcion,
                     mp.es_fisico,
                     mov.tipomovcaja
            ORDER BY mediopago
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoCaja);
            ps.setInt(2, ESTADO_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String grupo =
                            rs.getBoolean("esfisico")
                                    ? "EFECTIVO"
                                    : "BANCARIO";

                    int tipoMovimiento =
                            rs.getInt("tipomovcaja");

                    double total =
                            rs.getDouble("total");

                    Double ingreso = null;
                    Double egreso = null;

                    if (tipoMovimiento == 40) {
                        ingreso = total;
                    }

                    if (tipoMovimiento == 50
                            || tipoMovimiento == 51) {
                        egreso =
                                Math.abs(total);
                    }

                    resultado.add(
                            new CajaDTO.ResumenPagoDTO(
                                    grupo,
                                    rs.getString("mediopago"),
                                    ingreso,
                                    egreso
                            )
                    );
                }
            }
        }

        return resultado;
    }

    public List<CajaDTO> obtenerAbiertas()
            throws SQLException {

        List<CajaDTO> resultado =
                new ArrayList<>();

        String sql = """
            SELECT caj.codigo,
                   caj.fechaapertura,
                   caj.idvendedor,
                   caj.nombrevendedor,
                   caj.estado
            FROM util.caja caj
            WHERE caj.estado IN (0, 10)
            ORDER BY caj.fechaapertura ASC,
                     caj.codigo ASC
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                CajaDTO caja = new CajaDTO();

                caja.codigo =
                        rs.getInt("codigo");

                /*
                 * Se usa Timestamp y no Date para no perder la hora.
                 */
                caja.fechaApertura =
                        rs.getTimestamp(
                                "fechaapertura"
                        );

                caja.idVendedor =
                        rs.getInt("idvendedor");

                caja.nombreVendedor =
                        rs.getString("nombrevendedor");

                caja.estado =
                        rs.getInt("estado");

                resultado.add(caja);
            }
        }

        return resultado;
    }

    public boolean cerrarAbiertasUsuario(
            int codigoUsuario
    ) throws SQLException {

        if (codigoUsuario <= 0) {
            return false;
        }

        String sql = """
            UPDATE util.caja
            SET fechacierre = NOW(),
                importecierre = (
                    SELECT COALESCE(
                        SUM(mov.importe),
                        0
                    )
                    FROM util.movimientocaja mov
                    WHERE mov.idcaja = util.caja.codigo
                ),
                estado = 100
            WHERE estado < 20
              AND idvendedor = ?
        """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            ps.setInt(1, codigoUsuario);

            ps.executeUpdate();

            /*
             * Cero filas también es una ejecución válida:
             * simplemente no había cajas abiertas.
             */
            return true;
        }
    }

    private void calcularTotales(
            CajaDTO caja
    ) {
        CajaDTO.TotalesDTO totales =
                new CajaDTO.TotalesDTO();

        for (CajaDTO.MovimientoDTO movimiento
                : caja.movimientos) {

            switch (movimiento.tipoMovimiento) {
                case TIPO_APERTURA:
                    totales.cajaInicial +=
                            movimiento.importe;
                    break;

                case TIPO_INGRESO_MANUAL:
                    totales.ingresosManuales +=
                            movimiento.importe;
                    break;

                case TIPO_EGRESO_MANUAL:
                    totales.egresosManuales +=
                            movimiento.importe;
                    break;

                case TIPO_ARQUEO:
                    totales.arqueos +=
                            movimiento.importe;
                    break;

                default:
                    break;
            }
        }

        for (CajaDTO.ResumenPagoDTO fila
                : caja.resumenPagos) {

            double ingreso =
                    fila.ingreso == null
                            ? 0d
                            : fila.ingreso;

            double egreso =
                    fila.egreso == null
                            ? 0d
                            : fila.egreso * -1d;

            if ("EFECTIVO".equals(fila.grupo)) {
                totales.pagosFisicos += ingreso;
                totales.egresosFisicos += egreso;

            } else {
                totales.pagosVirtuales += ingreso;
                totales.egresosVirtuales += egreso;
            }
        }

        totales.totalFisico =
                totales.cajaInicial
                        + totales.ingresosManuales
                        + totales.egresosManuales
                        + totales.arqueos
                        + totales.pagosFisicos
                        + totales.egresosFisicos;

        totales.totalVirtual =
                caja.importeAperturaVirtual
                        + totales.pagosVirtuales
                        + totales.egresosVirtuales;

        totales.totalVentas =
                          totales.pagosFisicos
                        + totales.pagosVirtuales;

        caja.totales = totales;
    }

    private CajaDTO mapCaja(
            ResultSet rs
    ) throws SQLException {

        CajaDTO caja = new CajaDTO();

        caja.codigo =
                rs.getInt("codigo");

        caja.fechaApertura =
                rs.getTimestamp("fechaapertura");

        caja.fechaCierre =
                rs.getTimestamp("fechacierre");

        caja.importeApertura =
                rs.getDouble("importeapertura");

        caja.importeAperturaVirtual =
                rs.getDouble(
                        "importeaperturavirtual"
                );

        double importeCierre =
                rs.getDouble("importecierre");

        caja.importeCierre =
                rs.wasNull()
                        ? null
                        : importeCierre;

        double importeCierreVirtual =
                rs.getDouble(
                        "importecierrevirtual"
                );

        caja.importeCierreVirtual =
                rs.wasNull()
                        ? null
                        : importeCierreVirtual;

        caja.idVendedor =
                rs.getInt("idvendedor");

        caja.nombreVendedor =
                rs.getString("nombrevendedor");

        caja.estado =
                rs.getInt("estado");

        caja.observacionApertura =
                rs.getString(
                        "observacionapertura"
                );

        caja.observacionCierre =
                rs.getString(
                        "observacioncierre"
                );

        return caja;
    }

    private String determinarOrigenPago(
            int tipoMovimiento,
            boolean tieneFisico,
            boolean tieneVirtual
    ) {
        if (tipoMovimiento != 40
                && tipoMovimiento != 50
                && tipoMovimiento != 51) {
            return "-";
        }

        if (tieneFisico && tieneVirtual) {
            return "Mixto";
        }

        if (tieneFisico) {
            return "Físico";
        }

        if (tieneVirtual) {
            return "Virtual";
        }

        return "-";
    }

    private String descripcionLegacy(
            int tipo
    ) {
        return switch (tipo) {
            case 1 -> "Efectivo";
            case 2 -> "Débito";
            case 3 -> "Crédito";
            case 4 -> "Transferencia";
            case 5 -> "Otros";
            default -> "Tipo " + tipo;
        };
    }

    private void setNullableString(
            PreparedStatement ps,
            int posicion,
            String valor
    ) throws SQLException {

        if (valor == null || valor.isBlank()) {
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
}