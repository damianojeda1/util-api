package com.util.api.ticket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TicketRepository {

    @Inject
    DataSource dataSource;

    // -------------------------------------------------------------------------
    // GUARDAR TICKET
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarTicketResponse guardarTicket(
            TicketDTO.GuardarTicketRequest request
    ) {

        String error = validarTicket(request);

        if (error != null) {
            return TicketDTO.GuardarTicketResponse.error(error);
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                TicketDTO.GuardarTicketResponse response =
                        guardarTicket(
                                cn,
                                request
                        );

                cn.commit();

                return response;

            } catch (Exception ex) {

                rollback(cn);

                return TicketDTO.GuardarTicketResponse.error(
                        "Error guardando ticket: "
                                + mensaje(ex)
                );

            } finally {

                restaurarAutoCommit(
                        cn,
                        autoCommitOriginal
                );
            }

        } catch (Exception ex) {

            return TicketDTO.GuardarTicketResponse.error(
                    "Error obteniendo conexión: "
                            + mensaje(ex)
            );
        }
    }

    public TicketDTO.GuardarTicketResponse guardarTicket(
            Connection cn,
            TicketDTO.GuardarTicketRequest request
    ) throws SQLException {

        String error = validarTicket(request);

        if (error != null) {
            throw new SQLException(error);
        }

        boolean mayorista =
                obtenerClienteMayorista(
                        cn,
                        request.cliente
                );

        int codigoTicket =
                insertarTicket(
                        cn,
                        request,
                        mayorista
                );

        insertarItemsTicket(
                cn,
                codigoTicket,
                request.items
        );

        return TicketDTO.GuardarTicketResponse.ok(
                codigoTicket
        );
    }

    private boolean obtenerClienteMayorista(
            Connection cn,
            int codigoCliente
    ) throws SQLException {

        String sql = """
            SELECT mayorista
            FROM util.cliente
            WHERE codigo = ?
            """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoCliente
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                return rs.getBoolean("mayorista");
            }
        }
    }

    private int insertarTicket(
            Connection cn,
            TicketDTO.GuardarTicketRequest request,
            boolean mayorista
    ) throws SQLException {

        String sql = """
            INSERT INTO util.ticket (
                tipo,
                fecha,
                subtotal,
                bonificacion,
                porcentajebonificacion,
                total,
                idvendedor,
                nombrevendedor,
                idcliente,
                nombrecliente,
                estado,
                observacion,
                mayorista,
                acopio
            )
            VALUES (
                ?,
                NOW(),
                ?,
                0,
                0,
                ?,
                ?,
                ?,
                ?,
                ?,
                0,
                ?,
                ?,
                ?
            )
            RETURNING codigo
            """;

        double total = request.tipo == 1
                ? request.total
                : -Math.abs(request.total);

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    request.tipo
            );

            ps.setBigDecimal(
                    2,
                    decimal(total)
            );

            ps.setBigDecimal(
                    3,
                    decimal(total)
            );

            ps.setInt(
                    4,
                    request.vendedor
            );

            ps.setString(
                    5,
                    texto(request.nombreVendedor)
            );

            ps.setInt(
                    6,
                    request.cliente
            );

            ps.setString(
                    7,
                    texto(request.nombreCliente)
            );

            ps.setString(
                    8,
                    texto(request.observacion)
            );

            ps.setBoolean(
                    9,
                    mayorista
            );

            ps.setBoolean(
                    10,
                    request.acopio
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el código del ticket"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private void insertarItemsTicket(
            Connection cn,
            int codigoTicket,
            List<TicketDTO.ItemRequest> items
    ) throws SQLException {

        String sql = """
                INSERT INTO util.item (
                    idticket,
                    proveedor,
                    codigoarticulo,
                    cantidad,
                    costo,
                    margen,
                    margenpropio,
                    neto,
                    alicuota,
                    importeiva,
                    precio,
                    bonificacion,
                    descripcion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            for (TicketDTO.ItemRequest item : items) {

                if (item == null) {
                    continue;
                }

                ps.setInt(
                        1,
                        codigoTicket
                );

                ps.setInt(
                        2,
                        item.proveedor
                );

                ps.setString(
                        3,
                        texto(item.codigoArticulo)
                );

                ps.setBigDecimal(
                        4,
                        decimal(item.cantidad)
                );

                ps.setBigDecimal(
                        5,
                        decimal(item.costo)
                );

                ps.setBigDecimal(
                        6,
                        decimal(item.margen)
                );

                ps.setBigDecimal(
                        7,
                        decimal(item.margenPropio)
                );

                ps.setBigDecimal(
                        8,
                        decimal(item.neto)
                );

                ps.setBigDecimal(
                        9,
                        decimal(item.alicuota)
                );

                ps.setBigDecimal(
                        10,
                        decimal(item.importeIVA)
                );

                ps.setBigDecimal(
                        11,
                        decimal(item.precio)
                );

                ps.setBigDecimal(
                        12,
                        decimal(item.bonificacion)
                );

                ps.setString(
                        13,
                        texto(item.descripcion)
                );

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private String validarTicket(
            TicketDTO.GuardarTicketRequest request
    ) {

        if (request == null) {
            return "Los datos del ticket son obligatorios";
        }

        if (request.cliente <= 0) {
            return "El cliente es obligatorio";
        }

        if (request.items == null
                || request.items.isEmpty()) {

            return "El ticket no contiene artículos";
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // LISTAR TICKETS
    // -------------------------------------------------------------------------

    public List<TicketDTO.ResumenTicketDTO> listarTickets(
            LocalDate desde,
            LocalDate hasta,
            boolean soloFacturados
    ) throws SQLException {

        List<TicketDTO.ResumenTicketDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        StringBuilder sql = new StringBuilder(
                """
                SELECT
                    t.tipo,
                    t.codigo,
                    t.fecha,
                    t.total,
                    t.idcliente,
                    t.nombrecliente,
                    cf.descripcion AS comprobante_fiscal,
                    cf.neto AS neto_comprobante,
                    cf.iva AS iva_comprobante,
                    cf.total AS total_comprobante
                FROM util.ticket t
                LEFT JOIN util.comprobantefiscal cf
                       ON cf.ticket = t.codigo
                WHERE CAST(t.fecha AS DATE) >= ?
                  AND CAST(t.fecha AS DATE) <= ?
                """
        );

        if (soloFacturados) {
            sql.append(
                    " AND cf.descripcion IS NOT NULL "
            );
        }

        sql.append(" ORDER BY t.fecha DESC ");

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps =
                        cn.prepareStatement(sql.toString())
        ) {

            setRango(
                    ps,
                    desde,
                    hasta
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.ResumenTicketDTO dto =
                            new TicketDTO.ResumenTicketDTO();

                    dto.codigo =
                            rs.getInt("codigo");

                    dto.tipo =
                            rs.getInt("tipo");

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.idCliente =
                            rs.getInt("idcliente");

                    dto.nombreCliente =
                            rs.getString("nombrecliente");

                    dto.total =
                            rs.getDouble("total");

                    dto.comprobanteFiscal =
                            rs.getString(
                                    "comprobante_fiscal"
                            );

                    dto.netoComprobante =
                            getDoubleNullable(
                                    rs,
                                    "neto_comprobante"
                            );

                    dto.ivaComprobante =
                            getDoubleNullable(
                                    rs,
                                    "iva_comprobante"
                            );

                    dto.totalComprobante =
                            getDoubleNullable(
                                    rs,
                                    "total_comprobante"
                            );

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // DETALLE DEL TICKET
    // -------------------------------------------------------------------------

    public TicketDTO.DetalleTicketDTO obtenerDetalle(
            int codigoTicket
    ) throws SQLException {

        if (codigoTicket <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    codigo,
                    fecha,
                    total,
                    idvendedor,
                    nombrevendedor,
                    idcliente,
                    nombrecliente,
                    estado,
                    observacion,
                    tipo
                FROM util.ticket
                WHERE codigo = ?
                """;

        try (Connection cn = dataSource.getConnection()) {

            TicketDTO.DetalleTicketDTO dto;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setInt(
                        1,
                        codigoTicket
                );

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return null;
                    }

                    dto =
                            new TicketDTO.DetalleTicketDTO();

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

                    dto.idCliente =
                            rs.getInt("idcliente");

                    dto.nombreCliente =
                            rs.getString("nombrecliente");

                    dto.estado =
                            rs.getInt("estado");

                    dto.observacion =
                            rs.getString("observacion");

                    dto.tipo =
                            rs.getInt("tipo");
                }
            }

            dto.items =
                    obtenerItemsDetalle(
                            cn,
                            codigoTicket
                    );

            return dto;
        }
    }

    private List<TicketDTO.DetalleItemDTO> obtenerItemsDetalle(
            Connection cn,
            int codigoTicket
    ) throws SQLException {

        List<TicketDTO.DetalleItemDTO> result =
                new ArrayList<>();

        String sql = """
                SELECT
                    codigoarticulo,
                    precio,
                    cantidad,
                    descripcion,
                    alicuota,
                    bonificacion,
                    ROUND(
                        (precio * cantidad)::numeric,
                        3
                    ) AS total_calculado
                FROM util.item
                WHERE idticket = ?
                ORDER BY codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoTicket
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.DetalleItemDTO dto =
                            new TicketDTO.DetalleItemDTO();

                    dto.codigoArticulo =
                            rs.getString("codigoarticulo");

                    dto.descripcion =
                            rs.getString("descripcion");

                    dto.precio =
                            rs.getDouble("precio");

                    dto.cantidad =
                            rs.getDouble("cantidad");

                    dto.alicuota =
                            rs.getDouble("alicuota");

                    dto.bonificacion =
                            rs.getDouble("bonificacion");

                    dto.totalCalculado =
                            rs.getDouble("total_calculado");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // DATOS PARA FACTURACIÓN
    // -------------------------------------------------------------------------

    public TicketDTO.TicketFacturacionDTO obtenerParaFacturacion(
            int codigoTicket
    ) throws SQLException {

        if (codigoTicket <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    t.codigo,
                    t.tipo,
                    t.fecha,
                    t.total,
                    t.idvendedor,
                    t.nombrevendedor,
                    t.idcliente,
                    t.nombrecliente,
                    t.listaprecios,
                    t.estado,
                    t.observacion,
                    c.tipoidentificacion,
                    c.codigoidentificacion,
                    c.condicioniva,
                    c.razonsocial,
                    c.localidad
                FROM util.ticket t
                INNER JOIN util.cliente c
                        ON c.codigo = t.idcliente
                WHERE t.codigo = ?
                """;

        try (Connection cn = dataSource.getConnection()) {

            TicketDTO.TicketFacturacionDTO dto;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setInt(
                        1,
                        codigoTicket
                );

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return null;
                    }

                    dto = mapTicketFacturacion(rs);
                }
            }

            dto.items =
                    obtenerItemsFacturacion(
                            cn,
                            codigoTicket
                    );

            return dto;
        }
    }

    private TicketDTO.TicketFacturacionDTO mapTicketFacturacion(
            ResultSet rs
    ) throws SQLException {

        TicketDTO.TicketFacturacionDTO dto =
                new TicketDTO.TicketFacturacionDTO();

        dto.codigo =
                rs.getInt("codigo");

        dto.tipo =
                rs.getInt("tipo");

        dto.fecha =
                rs.getTimestamp("fecha");

        dto.total =
                rs.getDouble("total");

        dto.idVendedor =
                rs.getInt("idvendedor");

        dto.nombreVendedor =
                rs.getString("nombrevendedor");

        dto.idCliente =
                rs.getInt("idcliente");

        dto.nombreCliente =
                rs.getString("nombrecliente");

        dto.listaPrecios =
                rs.getInt("listaprecios");

        dto.estado =
                rs.getInt("estado");

        dto.observacion =
                rs.getString("observacion");

        TicketDTO.ClienteFiscalDTO cliente =
                new TicketDTO.ClienteFiscalDTO();

        cliente.codigo =
                dto.idCliente;

        cliente.tipoIdentificacion =
                rs.getString("tipoidentificacion");

        cliente.codigoIdentificacion =
                rs.getString("codigoidentificacion");

        cliente.condicionIVA =
                rs.getString("condicioniva");

        cliente.razonSocial =
                rs.getString("razonsocial");

        cliente.localidad =
                rs.getInt("localidad");

        dto.cliente = cliente;

        return dto;
    }

    private List<TicketDTO.ItemFacturacionDTO>
    obtenerItemsFacturacion(
            Connection cn,
            int codigoTicket
    ) throws SQLException {

        List<TicketDTO.ItemFacturacionDTO> result =
                new ArrayList<>();

        String sql = """
                SELECT
                    item.codigoarticulo,
                    item.descripcion,
                    item.cantidad,
                    item.margen,
                    item.neto,
                    item.alicuota,
                    item.importeiva,
                    item.precio,
                    item.bonificacion,
                    imp.codigo AS codigoiva
                FROM util.item item
                INNER JOIN util.impuesto imp
                        ON imp.alicuota = item.alicuota
                WHERE item.idticket = ?
                ORDER BY item.alicuota
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoTicket
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.ItemFacturacionDTO dto =
                            new TicketDTO.ItemFacturacionDTO();

                    dto.codigoArticulo =
                            rs.getString("codigoarticulo");

                    dto.descripcion =
                            rs.getString("descripcion");

                    dto.cantidad =
                            rs.getDouble("cantidad");

                    dto.margen =
                            rs.getDouble("margen");

                    dto.neto =
                            rs.getDouble("neto");

                    dto.alicuota =
                            rs.getDouble("alicuota");

                    dto.importeIVA =
                            rs.getDouble("importeiva");

                    dto.precio =
                            rs.getDouble("precio");

                    dto.bonificacion =
                            rs.getDouble("bonificacion");

                    dto.codigoIVA =
                            rs.getString("codigoiva");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // GUARDAR COMPROBANTE FISCAL
    // -------------------------------------------------------------------------

    public boolean guardarComprobanteFiscal(
            TicketDTO.GuardarComprobanteFiscalRequest request
    ) {

        if (request == null || request.ticket <= 0) {
            return false;
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                int codigoComprobante =
                        insertarComprobanteFiscal(
                                cn,
                                request
                        );

                insertarImpuestosComprobante(
                        cn,
                        codigoComprobante,
                        request.impuestos
                );

                cn.commit();

                return true;

            } catch (Exception ex) {

                rollback(cn);
                ex.printStackTrace();

                return false;

            } finally {
                restaurarAutoCommit(
                        cn,
                        autoCommitOriginal
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    private int insertarComprobanteFiscal(
            Connection cn,
            TicketDTO.GuardarComprobanteFiscalRequest request
    ) throws SQLException {

        String sql = """
                INSERT INTO util.comprobantefiscal (
                    ticket,
                    tipocomprobante,
                    tipocomprobantenombre,
                    letra,
                    puntoventa,
                    numero,
                    descripcion,
                    clientecategoriaiva,
                    clientecuit,
                    clientelocalidad,
                    fecha,
                    fechavencimiento,
                    neto,
                    nogravado,
                    iva,
                    total,
                    moneda,
                    cae,
                    caevencimiento,
                    codigobarra,
                    observacion,
                    estado,
                    razonsocial
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                RETURNING codigo
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = 1;

            ps.setInt(
                    i++,
                    request.ticket
            );

            ps.setInt(
                    i++,
                    entero(request.tipoComprobante)
            );

            ps.setString(
                    i++,
                    texto(request.tipoComprobanteNombre)
            );

            ps.setString(
                    i++,
                    texto(request.letra)
            );

            ps.setInt(
                    i++,
                    request.puntoVenta
            );

            ps.setInt(
                    i++,
                    request.numero
            );

            ps.setString(
                    i++,
                    texto(request.descripcion)
            );

            ps.setInt(
                    i++,
                    entero(request.clienteCategoriaIVA)
            );

            ps.setString(
                    i++,
                    texto(request.clienteCUIT)
            );

            ps.setInt(
                    i++,
                    request.clienteLocalidad
            );

            setTimestamp(
                    ps,
                    i++,
                    request.fecha
            );

            setTimestamp(
                    ps,
                    i++,
                    request.fechaVencimiento
            );

            ps.setBigDecimal(
                    i++,
                    decimal(request.neto)
            );

            ps.setBigDecimal(
                    i++,
                    decimal(request.noGravado)
            );

            ps.setBigDecimal(
                    i++,
                    decimal(request.iva)
            );

            ps.setBigDecimal(
                    i++,
                    decimal(request.total)
            );

            ps.setInt(
                    i++,
                    request.moneda
            );

            ps.setString(
                    i++,
                    texto(request.cae)
            );

            setTimestamp(
                    ps,
                    i++,
                    request.caeVencimiento
            );

            ps.setString(
                    i++,
                    texto(request.codigoBarra)
            );

            ps.setString(
                    i++,
                    texto(request.observacion)
            );

            ps.setInt(
                    i++,
                    request.estado
            );

            ps.setString(
                    i,
                    texto(request.razonSocial)
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el código "
                                    + "del comprobante fiscal"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private void insertarImpuestosComprobante(
            Connection cn,
            int codigoComprobante,
            List<TicketDTO.ComprobanteImpuestoRequest> impuestos
    ) throws SQLException {

        if (impuestos == null || impuestos.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO util.comprobanteimpuesto (
                    comprobante,
                    impuesto,
                    baseimponible,
                    monto,
                    porcentaje
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            for (
                    TicketDTO.ComprobanteImpuestoRequest impuesto
                    : impuestos
            ) {

                if (impuesto == null) {
                    continue;
                }

                ps.setInt(
                        1,
                        codigoComprobante
                );

                ps.setInt(
                        2,
                        entero(impuesto.impuesto)
                );

                ps.setBigDecimal(
                        3,
                        decimal(impuesto.baseImponible)
                );

                ps.setBigDecimal(
                        4,
                        decimal(impuesto.monto)
                );

                ps.setBigDecimal(
                        5,
                        decimal(impuesto.porcentaje)
                );

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER COMPROBANTE FISCAL
    // -------------------------------------------------------------------------

    public TicketDTO.ComprobanteFiscalDTO obtenerComprobanteFiscal(
            int codigoTicket
    ) throws SQLException {

        if (codigoTicket <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    comp.codigo,
                    comp.ticket,
                    comp.letra,
                    comp.puntoventa,
                    comp.numero,
                    comp.tipocomprobante,
                    UPPER(comp.tipocomprobantenombre)
                        AS tipocomprobantenombre,
                    comp.fecha,
                    cliente.codigo AS clientecodigo,
                    cliente.razonsocial,
                    cliente.domicilio,
                    CONCAT(
                        localidad.nombre,
                        ' - ',
                        provincia.nombre
                    ) AS clienteloc,
                    condicioniva.descripcion
                        AS clientecondicioniva,
                    comp.clientecuit,
                    comp.clientecategoriaiva,
                    cliente.ingresosbrutos,
                    tipoide.descripcion
                        AS clientetipoidentificacion,
                    comp.neto,
                    comp.nogravado,
                    comp.iva,
                    comp.neto + comp.nogravado AS subtotal,
                    comp.observacion,
                    comp.total,
                    comp.cae,
                    comp.caevencimiento,
                    comp.codigobarra
                FROM util.comprobantefiscal comp
                INNER JOIN util.tipocomprobante
                        ON tipocomprobante.codigo =
                           comp.tipocomprobante
                INNER JOIN util.condicioniva
                        ON condicioniva.codigo =
                           comp.clientecategoriaiva
                INNER JOIN util.ticket
                        ON ticket.codigo = comp.ticket
                INNER JOIN util.cliente
                        ON cliente.codigo = ticket.idcliente
                INNER JOIN util.localidad
                        ON localidad.id = comp.clientelocalidad
                INNER JOIN util.provincia
                        ON provincia.id = localidad.provincia
                INNER JOIN util.tipoidentificacion tipoide
                        ON tipoide.codigo =
                           cliente.tipoidentificacion
                WHERE comp.ticket = ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoTicket
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                TicketDTO.ComprobanteFiscalDTO dto =
                        new TicketDTO.ComprobanteFiscalDTO();

                dto.codigo =
                        rs.getInt("codigo");

                dto.ticket =
                        rs.getInt("ticket");

                dto.letra =
                        rs.getString("letra");

                dto.puntoVenta =
                        rs.getInt("puntoventa");

                dto.numero =
                        rs.getLong("numero");

                dto.tipoComprobante =
                        rs.getInt("tipocomprobante");

                dto.tipoComprobanteNombre =
                        rs.getString(
                                "tipocomprobantenombre"
                        );

                dto.fecha =
                        rs.getTimestamp("fecha");

                dto.clienteCodigo =
                        rs.getInt("clientecodigo");

                dto.clienteRazonSocial =
                        rs.getString("razonsocial");

                dto.clienteDomicilio =
                        rs.getString("domicilio");

                dto.clienteLocalidad =
                        rs.getString("clienteloc");

                dto.clienteCondicionIVA =
                        rs.getString(
                                "clientecondicioniva"
                        );

                dto.clienteCuit =
                        rs.getString("clientecuit");

                dto.clienteCategoriaIVA =
                        rs.getInt(
                                "clientecategoriaiva"
                        );

                dto.clienteIngresosBrutos =
                        rs.getString("ingresosbrutos");

                dto.clienteTipoIdentificacion =
                        rs.getString(
                                "clientetipoidentificacion"
                        );

                dto.neto =
                        rs.getDouble("neto");

                dto.noGravado =
                        rs.getDouble("nogravado");

                dto.iva =
                        rs.getDouble("iva");

                dto.subtotal =
                        rs.getDouble("subtotal");

                dto.observacion =
                        rs.getString("observacion");

                dto.total =
                        rs.getDouble("total");

                dto.cae =
                        rs.getString("cae");

                dto.caeVencimiento =
                        rs.getTimestamp("caevencimiento");

                dto.codigoBarra =
                        rs.getString("codigobarra");

                return dto;
            }
        }
    }

    // -------------------------------------------------------------------------
    // COMPROBANTES ASOCIADOS AL CLIENTE
    // -------------------------------------------------------------------------

    public List<TicketDTO.ComprobanteAsociadoDTO>
    listarComprobantesCliente(
            int codigoCliente,
            String cuit
    ) throws SQLException {

        List<TicketDTO.ComprobanteAsociadoDTO> result =
                new ArrayList<>();

        if (codigoCliente <= 0
                && (cuit == null || cuit.isBlank())) {

            return result;
        }

        String sql = """
                SELECT
                    cf.fecha,
                    cf.descripcion,
                    cf.tipocomprobante,
                    cf.puntoventa,
                    cf.numero,
                    cf.neto,
                    cf.iva,
                    cf.total
                FROM util.ticket t
                INNER JOIN util.comprobantefiscal cf
                        ON cf.ticket = t.codigo
                WHERE (
                    t.idcliente = ?
                    OR cf.clientecuit = ?
                )
                ORDER BY
                    cf.fecha DESC,
                    cf.codigo DESC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoCliente
            );

            ps.setString(
                    2,
                    normalizarCuit(cuit)
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.ComprobanteAsociadoDTO dto =
                            new TicketDTO.ComprobanteAsociadoDTO();

                    dto.fecha =
                            rs.getTimestamp("fecha");

                    dto.descripcion =
                            rs.getString("descripcion");

                    dto.tipoComprobante =
                            rs.getString("tipocomprobante");

                    dto.puntoVenta =
                            rs.getInt("puntoventa");

                    dto.numero =
                            rs.getLong("numero");

                    dto.neto =
                            rs.getDouble("neto");

                    dto.iva =
                            rs.getDouble("iva");

                    dto.total =
                            rs.getDouble("total");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // LIBRO IVA
    // -------------------------------------------------------------------------

    public List<TicketDTO.LibroIvaVentaDTO> listarLibroIva(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<TicketDTO.LibroIvaVentaDTO> resultado =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return resultado;
        }

        String sql = """
                WITH impuestos AS (
                    SELECT
                        item.idticket,
                        SUM(
                            CASE
                                WHEN item.alicuota = 10.5
                                THEN item.importeiva
                                     * item.cantidad
                                ELSE 0
                            END
                        ) AS iva105,
                        SUM(
                            CASE
                                WHEN item.alicuota = 21
                                THEN item.importeiva
                                     * item.cantidad
                                ELSE 0
                            END
                        ) AS iva21
                    FROM util.item item
                    GROUP BY item.idticket
                )
                SELECT
                    TO_CHAR(
                        fiscal.fecha,
                        'DD/MM/YYYY'
                    ) AS fecha,
                    fiscal.descripcion,
                    fiscal.cae,
                    iva.descripcion AS condicion,
                    fiscal.clientecuit AS cuit,
                    ticket.nombrecliente AS cliente,
                    COALESCE(fiscal.neto, 0) AS neto,
                    COALESCE(impuestos.iva105, 0) AS iva105,
                    COALESCE(impuestos.iva21, 0) AS iva21,
                    COALESCE(fiscal.total, 0) AS total
                FROM util.comprobantefiscal fiscal
                INNER JOIN util.ticket ticket
                        ON ticket.codigo = fiscal.ticket
                INNER JOIN util.condicioniva iva
                        ON iva.codigo =
                           fiscal.clientecategoriaiva
                LEFT JOIN impuestos
                       ON impuestos.idticket =
                          fiscal.ticket
                WHERE CAST(fiscal.fecha AS DATE) >= ?
                  AND CAST(fiscal.fecha AS DATE) <= ?
                ORDER BY
                    fiscal.fecha ASC,
                    fiscal.ticket ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(
                    ps,
                    desde,
                    hasta
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TicketDTO.LibroIvaVentaDTO dto =
                            new TicketDTO.LibroIvaVentaDTO();

                    dto.setFecha(
                            rs.getString("fecha")
                    );

                    dto.setDescripcion(
                            rs.getString("descripcion")
                    );

                    dto.setCae(
                            rs.getString("cae")
                    );

                    dto.setCondicion(
                            rs.getString("condicion")
                    );

                    dto.setCuit(
                            rs.getString("cuit")
                    );

                    dto.setCliente(
                            rs.getString("cliente")
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
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        ResumenPeriodoDTO resultado =
                new ResumenPeriodoDTO();

        if (!rangoValido(desde, hasta)) {
            return resultado;
        }

        String sql = """
                WITH impuestos AS (
                    SELECT
                        item.idticket,
                        SUM(
                            CASE
                                WHEN item.alicuota IN (10.5, 21)
                                THEN item.importeiva
                                     * item.cantidad
                                ELSE 0
                            END
                        ) AS iva
                    FROM util.item item
                    GROUP BY item.idticket
                )
                SELECT
                    COALESCE(
                        SUM(fiscal.neto),
                        0
                    ) AS neto,
                    COALESCE(
                        SUM(impuestos.iva),
                        0
                    ) AS iva,
                    COALESCE(
                        SUM(fiscal.total),
                        0
                    ) AS total
                FROM util.comprobantefiscal fiscal
                LEFT JOIN impuestos
                       ON impuestos.idticket =
                          fiscal.ticket
                WHERE CAST(fiscal.fecha AS DATE) >= ?
                  AND CAST(fiscal.fecha AS DATE) <= ?
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(
                    ps,
                    desde,
                    hasta
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
    // HELPERS
    // -------------------------------------------------------------------------

    private void setRango(
            PreparedStatement ps,
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        ps.setDate(
                1,
                Date.valueOf(desde)
        );

        ps.setDate(
                2,
                Date.valueOf(hasta)
        );
    }

    private boolean rangoValido(
            LocalDate desde,
            LocalDate hasta
    ) {

        return desde != null
                && hasta != null
                && !desde.isAfter(hasta);
    }

    private BigDecimal decimal(
            double value
    ) {

        return BigDecimal.valueOf(value);
    }

    private String texto(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private int entero(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void setTimestamp(
            PreparedStatement ps,
            int indice,
            java.util.Date value
    ) throws SQLException {

        if (value == null) {
            ps.setNull(
                    indice,
                    Types.TIMESTAMP
            );

            return;
        }

        ps.setTimestamp(
                indice,
                new Timestamp(value.getTime())
        );
    }

    private Double getDoubleNullable(
            ResultSet rs,
            String columna
    ) throws SQLException {

        double value =
                rs.getDouble(columna);

        return rs.wasNull()
                ? null
                : value;
    }

    private String normalizarCuit(
            String cuit
    ) {

        if (cuit == null) {
            return "";
        }

        return cuit
                .trim()
                .replace("-", "");
    }

    private void rollback(
            Connection cn
    ) {

        if (cn == null) {
            return;
        }

        try {
            cn.rollback();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void restaurarAutoCommit(
            Connection cn,
            boolean autoCommitOriginal
    ) {

        if (cn == null) {
            return;
        }

        try {
            cn.setAutoCommit(
                    autoCommitOriginal
            );

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String mensaje(
            Exception ex
    ) {

        if (ex == null
                || ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return "error inesperado";
        }

        return ex.getMessage();
    }
}