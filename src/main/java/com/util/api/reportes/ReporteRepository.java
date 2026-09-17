package com.util.api.reportes;

import com.util.api.cliente.ClienteDTO;
import com.util.api.cliente.ClienteService;
import com.util.api.configuracion.ConfiguracionDTO;
import com.util.api.configuracion.ConfiguracionService;
import com.util.api.localidad.LocalidadDTO;
import com.util.api.localidad.LocalidadService;
import com.util.api.reportes.dto.*;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ReporteRepository {

    @Inject
    DataSource dataSource;

    @Inject
    ConfiguracionService configuracionService;

    @Inject
    ClienteService clienteService;

    @Inject
    LocalidadService localidadService;

    // -------------------------------------------------------------------------
    // COMPROBANTE
    // -------------------------------------------------------------------------

    public ReporteComprobanteDTO obtenerComprobante(
            int codigoTicket
    ) throws SQLException {

        ReporteComprobanteDTO dto =
                new ReporteComprobanteDTO();

        if (codigoTicket <= 0) {
            return dto;
        }

        String sql = """
            SELECT
                codigo,
                fecha,
                subtotal,
                bonificacion,
                porcentajebonificacion,
                total,
                idvendedor,
                nombrevendedor,
                idcliente,
                estado,
                observacion,
                tipo
            FROM util.ticket
            WHERE codigo = ?
            """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(1, codigoTicket);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return dto;
                }

                ReporteCabeceraDTO cab =
                        new ReporteCabeceraDTO();

                cab.setCodigo(
                        rs.getInt("codigo")
                );

                cab.setFecha(
                        rs.getTimestamp("fecha")
                );

                cab.setObservacion(
                        rs.getString("observacion")
                );

                cab.setEstado(
                        rs.getInt("estado")
                );

                cab.setTipo(
                        rs.getInt("tipo")
                );

                cab.setVendedor(
                        rs.getInt("idvendedor")
                );

                cab.setNombreVendedor(
                        rs.getString("nombrevendedor")
                );

                dto.setComprobante(cab);

                Map<String, String> config =
                        obtenerConfiguracion();

                cargarEmpresa(
                        dto,
                        config
                );

                cargarCliente(
                        dto,
                        rs.getInt("idcliente")
                );

                cargarItemsTicket(
                        cn,
                        dto,
                        codigoTicket
                );

                cargarTotalesTicket(
                        dto,
                        rs
                );

                cargarComprobanteFiscal(
                        cn,
                        dto,
                        codigoTicket
                );

                completarParametros(
                        dto,
                        config
                );
            }
        }

        return dto;
    }

// -------------------------------------------------------------------------
// PRESUPUESTO
// -------------------------------------------------------------------------

    public ReporteComprobanteDTO obtenerPresupuesto(
            int codigoPresupuesto
    ) throws SQLException {

        ReporteComprobanteDTO dto =
                new ReporteComprobanteDTO();

        if (codigoPresupuesto <= 0) {
            return dto;
        }

        String sql = """
            SELECT
                codigo,
                fecha,
                cliente,
                usuario,
                listaprecios,
                total,
                vencimiento,
                estado,
                observacion
            FROM util.ventaspresupuestocab
            WHERE codigo = ?
            """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoPresupuesto
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return dto;
                }

                ReporteCabeceraDTO cab =
                        new ReporteCabeceraDTO();

                cab.setCodigo(
                        rs.getInt("codigo")
                );

                cab.setFecha(
                        rs.getTimestamp("fecha")
                );

                cab.setFechaVencimiento(
                        rs.getDate("vencimiento")
                );

                cab.setEstado(
                        rs.getInt("estado")
                );

                cab.setObservacion(
                        rs.getString("observacion")
                );

                cab.setVendedor(
                        rs.getInt("usuario")
                );

                dto.setComprobante(cab);

                Map<String, String> config =
                        obtenerConfiguracion();

                cargarEmpresa(
                        dto,
                        config
                );

                cargarCliente(
                        dto,
                        rs.getInt("cliente")
                );

                cargarItemsPresupuesto(
                        cn,
                        dto,
                        codigoPresupuesto
                );

                ReporteTotalesDTO totales =
                        new ReporteTotalesDTO();

                totales.setTotal(
                        rs.getDouble("total")
                );

                dto.setTotales(totales);

                completarParametros(
                        dto,
                        config
                );

                dto.getParametros().put(
                        "LISTA_PRECIOS",
                        rs.getInt("listaprecios")
                );
            }
        }

        return dto;
    }

    // -------------------------------------------------------------------------
// RECIBO
// -------------------------------------------------------------------------

    public ReporteComprobanteDTO obtenerRecibo(
            int codigoRecibo
    ) throws SQLException {

        ReporteComprobanteDTO dto =
                new ReporteComprobanteDTO();

        if (codigoRecibo <= 0) {
            return dto;
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
                observacion
            FROM util.recibo
            WHERE codigo = ?
            """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    codigoRecibo
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return dto;
                }

                ReporteCabeceraDTO cab =
                        new ReporteCabeceraDTO();

                cab.setCodigo(
                        rs.getInt("codigo")
                );

                cab.setFecha(
                        rs.getTimestamp("fecha")
                );

                cab.setEstado(
                        rs.getInt("estado")
                );

                cab.setObservacion(
                        rs.getString("observacion")
                );

                cab.setVendedor(
                        rs.getInt("idvendedor")
                );

                cab.setNombreVendedor(
                        rs.getString("nombrevendedor")
                );

                dto.setComprobante(cab);

                Map<String, String> config =
                        obtenerConfiguracion();

                cargarEmpresa(
                        dto,
                        config
                );

                cargarCliente(
                        dto,
                        rs.getInt("idcliente")
                );

                ReporteTotalesDTO totales =
                        new ReporteTotalesDTO();

                totales.setTotal(
                        rs.getDouble("total")
                );

                dto.setTotales(totales);

                completarParametros(
                        dto,
                        config
                );
            }
        }

        return dto;
    }

    private Map<String, String> obtenerConfiguracion()
            throws SQLException {

        Map<String, String> resultado =
                new HashMap<>();

        List<ConfiguracionDTO> configuraciones =
                configuracionService.obtenerTodas();

        if (configuraciones == null) {
            return resultado;
        }

        for (ConfiguracionDTO configuracion
                : configuraciones) {

            if (configuracion == null
                    || configuracion.getClave() == null) {
                continue;
            }

            resultado.put(
                    configuracion.getClave(),
                    configuracion.getValor() == null
                            ? ""
                            : configuracion.getValor()
            );
        }

        return resultado;
    }

    private void cargarEmpresa(
            ReporteComprobanteDTO dto,
            Map<String, String> config
    ) {

        ReporteEmpresaDTO empresa =
                new ReporteEmpresaDTO();

        empresa.setRazonSocial(
                config.getOrDefault(
                        "generales_empresa",
                        ""
                )
        );

        empresa.setDireccion(
                config.getOrDefault(
                        "generales_direccion",
                        ""
                )
        );

        empresa.setTelefono(
                config.getOrDefault(
                        "generales_telefono",
                        ""
                )
        );


        empresa.setCategoriaIVA(
                config.getOrDefault(
                        "generales_iva_desc",
                        ""
                )
        );

        empresa.setEmail(
                config.getOrDefault(
                        "generales_email",
                        ""
                )
        );

        empresa.setCuit(
                config.getOrDefault(
                        "facturacion_cuit",
                        ""
                )
        );

        empresa.setIngresosBrutos(
                config.getOrDefault(
                        "facturacion_iibb",
                        ""
                )
        );

        empresa.setInicioActividad(
                config.getOrDefault(
                        "facturacion_inicioactividad",
                        ""
                )
        );

        dto.setEmpresa(empresa);
    }

    private void cargarCliente(
            ReporteComprobanteDTO dto,
            int codigoCliente
    ) throws SQLException {

        ClienteDTO.Response cli =
                clienteService.findById(
                        codigoCliente
                );

        if (cli == null) {
            return;
        }

        ReporteClienteDTO cliente =
                new ReporteClienteDTO();

        cliente.setCodigo(
                cli.getCodigo()
        );

        cliente.setRazonSocial(
                cli.getRazonSocial()
        );

        cliente.setTipoIdentificacion(
                cli.getTipoIdentificacionNombre()
        );

        cliente.setIdentificacion(
                cli.getCodigoIdentificacion()
        );

        cliente.setIngresosBrutos(
                cli.getIngresosBrutos()
        );

        cliente.setCategoriaIVA(
                cli.getCondicionIvaNombre()
        );

        cliente.setCondicionIVA(
                cli.getCondicionIvaNombre()
        );

        cliente.setDomicilio(
                cli.getDomicilio()
        );

        cliente.setTelefono(
                cli.getTelefonoFijo()
        );

        cliente.setCelular(
                cli.getTelefonoMovil()
        );

        cliente.setEmail(
                cli.getEmailPersonal()
        );

        if (cli.getLocalidadId() > 0) {

            LocalidadDTO.Response localidad =
                    localidadService.buscarId(
                            cli.getLocalidadId()
                    );

            if (localidad != null) {

                cliente.setLocalidad(
                        localidad.getNombre()
                );

                cliente.setProvincia(
                        localidad.getProvinciaNombre()
                );

                cliente.setCodigoPostal(
                        localidad.getCodigoPostal()
                );
            }
        }

        dto.setCliente(cliente);
    }

    private void cargarItemsTicket(
            Connection cn,
            ReporteComprobanteDTO dto,
            int codigoTicket
    ) throws SQLException {

        String sql = """
            SELECT
                codigoarticulo,
                descripcion,
                cantidad,
                precio,
                neto,
                importeiva,
                alicuota,
                bonificacion,
                proveedor
            FROM util.item
            WHERE idticket = ?
            ORDER BY codigo
            """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoTicket
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteItemDTO item =
                            new ReporteItemDTO();

                    double cantidad =
                            rs.getDouble("cantidad");

                    double precio =
                            rs.getDouble("precio");

                    item.setCodigoArticulo(
                            rs.getString(
                                    "codigoarticulo"
                            )
                    );

                    item.setDescripcion(
                            rs.getString(
                                    "descripcion"
                            )
                    );

                    item.setCantidad(
                            cantidad
                    );

                    item.setPrecioUnitario(
                            precio
                    );

                    item.setBonificacion(
                            rs.getDouble(
                                    "bonificacion"
                            )
                    );

                    item.setAlicuota(
                            rs.getDouble("alicuota")
                    );

                    item.setNeto(
                            rs.getDouble("neto")
                    );

                    item.setIva(
                            rs.getDouble("importeiva")
                    );

                    item.setPrecioBonificado(
                            precio
                    );

                    item.setTotal(
                            redondear(
                                    precio * cantidad,
                                    2
                            )
                    );

                    dto.getItems().add(item);
                }
            }
        }
    }

    private void cargarItemsPresupuesto(
            Connection cn,
            ReporteComprobanteDTO dto,
            int codigoPresupuesto
    ) throws SQLException {

        String sql = """
            SELECT
                codigoarticulo,
                descripcionarticulo,
                cantidad,
                preciolista,
                bonificacion,
                precio,
                iva
            FROM util.ventaspresupuestoreng
            WHERE ventaspresupuestocab = ?
            ORDER BY codigo
            """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoPresupuesto
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteItemDTO item =
                            new ReporteItemDTO();

                    double cantidad =
                            rs.getDouble("cantidad");

                    double precio =
                            rs.getDouble("precio");

                    item.setCodigoArticulo(
                            rs.getString(
                                    "codigoarticulo"
                            )
                    );

                    item.setDescripcion(
                            rs.getString(
                                    "descripcionarticulo"
                            )
                    );

                    item.setCantidad(
                            cantidad
                    );

                    item.setPrecioUnitario(
                            rs.getDouble("preciolista")
                    );

                    item.setBonificacion(
                            rs.getDouble("bonificacion")
                    );

                    item.setPrecioBonificado(
                            precio
                    );

                    item.setAlicuota(
                            rs.getDouble("iva")
                    );

                    item.setTotal(
                            redondear(
                                    precio * cantidad,
                                    2
                            )
                    );

                    dto.getItems().add(item);
                }
            }
        }
    }

    private void cargarTotalesTicket(
            ReporteComprobanteDTO dto,
            ResultSet rsTicket
    ) throws SQLException {

        ReporteTotalesDTO totales =
                new ReporteTotalesDTO();

        double iva105 = 0;
        double iva21 = 0;

        double neto105 = 0;
        double neto21 = 0;

        for (ReporteItemDTO item
                : dto.getItems()) {

            double cantidad =
                    item.getCantidad();

            if (Math.abs(
                    item.getAlicuota() - 10.5
            ) < 0.001) {

                iva105 +=
                        item.getIva()
                                * cantidad;

                neto105 +=
                        item.getNeto()
                                * cantidad;

            } else if (Math.abs(
                    item.getAlicuota() - 21.0
            ) < 0.001) {

                iva21 +=
                        item.getIva()
                                * cantidad;

                neto21 +=
                        item.getNeto()
                                * cantidad;
            }
        }

        totales.setIva105(
                redondear(
                        iva105,
                        2
                )
        );

        totales.setIva21(
                redondear(
                        iva21,
                        2
                )
        );

        totales.setNeto105(
                redondear(
                        neto105,
                        2
                )
        );

        totales.setNeto21(
                redondear(
                        neto21,
                        2
                )
        );

        totales.setSubtotal(
                rsTicket.getDouble(
                        "subtotal"
                )
        );

        totales.setBonificacion(
                rsTicket.getDouble(
                        "bonificacion"
                )
        );

        totales.setPorcentajeBonificacion(
                rsTicket.getDouble(
                        "porcentajebonificacion"
                )
        );

        totales.setTotal(
                rsTicket.getDouble("total")
        );

        dto.setTotales(totales);
    }

    private void cargarComprobanteFiscal(
            Connection cn,
            ReporteComprobanteDTO dto,
            int codigoTicket
    ) throws SQLException {

        String sql = """
            SELECT
                ticket,
                tipocomprobante,
                tipocomprobantenombre,
                letra,
                puntoventa,
                numero,
                fechavencimiento,
                neto,
                iva,
                total,
                clientecuit,
                razonsocial,
                cae,
                caevencimiento,
                codigobarra
            FROM util.comprobantefiscal
            WHERE ticket = ?
            """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    codigoTicket
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return;
                }

                ReporteCabeceraDTO cab =
                        dto.getComprobante();

                if (cab == null) {
                    cab =
                            new ReporteCabeceraDTO();

                    dto.setComprobante(cab);
                }

                cab.setTicket(
                        rs.getInt("ticket")
                );

                cab.setTipoComprobante(
                        rs.getInt(
                                "tipocomprobante"
                        )
                );

                cab.setTipoComprobanteNombre(
                        rs.getString(
                                "tipocomprobantenombre"
                        )
                );

                cab.setLetra(
                        rs.getString("letra")
                );

                cab.setPuntoVenta(
                        rs.getInt("puntoventa")
                );

                cab.setNumero(
                        rs.getInt("numero")
                );

                cab.setFechaVencimiento(
                        rs.getDate(
                                "fechavencimiento"
                        )
                );

                cab.setCae(
                        rs.getString("cae")
                );

                cab.setVencimientoCAE(
                        rs.getDate(
                                "caevencimiento"
                        )
                );

                cab.setCodigoBarras(
                        rs.getString(
                                "codigobarra"
                        )
                );

                ReporteTotalesDTO totales =
                        dto.getTotales();

                if (totales == null) {
                    totales =
                            new ReporteTotalesDTO();
                }

                totales.setNeto(
                        rs.getDouble("neto")
                );

                totales.setIva(
                        rs.getDouble("iva")
                );

                totales.setTotal(
                        rs.getDouble("total")
                );

                dto.setTotales(totales);

                ReporteClienteDTO cliente =
                        dto.getCliente();

                if (cliente != null) {

                    cliente.setRazonSocial(
                            rs.getString(
                                    "razonsocial"
                            )
                    );

                    cliente.setIdentificacion(
                            rs.getString(
                                    "clientecuit"
                            )
                    );
                }
            }
        }
    }

    private void completarParametros(
            ReporteComprobanteDTO dto,
            Map<String, String> config
    ) {

        dto.getParametros().put(
                "DETALLE_PIE",
                config.getOrDefault(
                        "impresion_detalle_pie",
                        ""
                )
        );

        dto.getParametros().put(
                "DETALLE_RESUMEN",
                config.getOrDefault(
                        "impresion_detalle_resumen",
                        ""
                )
        );
    }

    private double redondear(
            double valor,
            int decimales
    ) {

        double factor =
                Math.pow(
                        10,
                        decimales
                );

        return Math.round(
                valor * factor
        ) / factor;
    }

    // -------------------------------------------------------------------------
    // CLIENTES CON DEUDA
    // -------------------------------------------------------------------------

    public List<ReporteDTO.DeudaClienteDTO> listarClientesConDeuda(
            LocalDate hasta
    ) throws SQLException {

        List<ReporteDTO.DeudaClienteDTO> result =
                new ArrayList<>();

        if (hasta == null) {
            return result;
        }

        String sql = """
                SELECT
                    cli.codigo,
                    cli.razonsocial,
                    cli.telfijo,
                    cli.telmovil,
                    COALESCE(
                        util.saldoHasta(cli.codigo, ?),
                        0
                    ) AS saldo
                FROM util.cliente cli
                WHERE cli.codigo > 1
                  AND COALESCE(
                        util.saldoHasta(cli.codigo, ?),
                        0
                  ) > 0
                ORDER BY cli.razonsocial ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            Timestamp fechaHasta =
                    Timestamp.valueOf(
                            hasta.atTime(23, 59, 59)
                    );

            ps.setTimestamp(1, fechaHasta);
            ps.setTimestamp(2, fechaHasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteDTO.DeudaClienteDTO dto =
                            new ReporteDTO.DeudaClienteDTO();

                    dto.codigoCliente =
                            rs.getInt("codigo");

                    dto.razonSocial =
                            texto(rs.getString("razonsocial"));

                    dto.telefonoFijo =
                            texto(rs.getString("telfijo"));

                    dto.telefonoMovil =
                            texto(rs.getString("telmovil"));

                    dto.saldo =
                            decimal(rs, "saldo");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // PRODUCTOS SIN MOVIMIENTO
    // -------------------------------------------------------------------------

    public List<ReporteDTO.ProductoSinMovimientoDTO>
    listarProductosSinMovimiento(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<ReporteDTO.ProductoSinMovimientoDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    art.codigo,
                    art.descripcionpropia AS descripcion,
                    prov.razonsocial AS proveedor,
                    COALESCE(art.stock, 0) AS stock,
                    COALESCE(art.stockminimo, 0) AS stockminimo
                FROM util.articulo art
                LEFT JOIN util.proveedor prov
                       ON prov.codigo = art.proveedor
                WHERE art.habilitado = true
                  AND NOT EXISTS (
                      SELECT 1
                      FROM util.item it
                      INNER JOIN util.ticket tic
                              ON tic.codigo = it.idticket
                      WHERE tic.tipo = 1
                        AND tic.fecha::date BETWEEN ? AND ?
                        AND it.codigoarticulo = art.codigo
                        AND it.proveedor = art.proveedor
                  )
                ORDER BY
                    prov.razonsocial ASC,
                    art.descripcionpropia ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(ps, desde, hasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteDTO.ProductoSinMovimientoDTO dto =
                            new ReporteDTO.ProductoSinMovimientoDTO();

                    dto.codigo =
                            rs.getString("codigo");

                    dto.descripcion =
                            texto(rs.getString("descripcion"));

                    dto.proveedor =
                            texto(rs.getString("proveedor"));

                    dto.stock =
                            decimal(rs, "stock");

                    dto.stockMinimo =
                            decimal(rs, "stockminimo");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // VENTAS POR PRODUCTO
    // -------------------------------------------------------------------------

    public List<ReporteDTO.VentaProductoDTO>
    listarVentasPorProducto(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<ReporteDTO.VentaProductoDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    it.codigoarticulo AS codigo,
                    it.descripcion,
                    COALESCE(SUM(it.cantidad), 0) AS cantidad,
                    COALESCE(
                        SUM(it.neto + it.importeiva),
                        0
                    ) AS total
                FROM util.item it
                INNER JOIN util.ticket tic
                        ON tic.codigo = it.idticket
                WHERE tic.tipo = 1
                  AND tic.fecha::date BETWEEN ? AND ?
                GROUP BY
                    it.codigoarticulo,
                    it.descripcion
                ORDER BY
                    cantidad DESC,
                    it.descripcion ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(ps, desde, hasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteDTO.VentaProductoDTO dto =
                            new ReporteDTO.VentaProductoDTO();

                    dto.codigo =
                            rs.getString("codigo");

                    dto.descripcion =
                            texto(rs.getString("descripcion"));

                    dto.cantidad =
                            decimal(rs, "cantidad");

                    dto.total =
                            decimal(rs, "total");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // PRODUCTOS RENTABLES
    // -------------------------------------------------------------------------

    public List<ReporteDTO.ProductoRentableDTO>
    listarProductosRentables(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<ReporteDTO.ProductoRentableDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    it.codigoarticulo AS codigo,
                    it.descripcion,
                    COALESCE(SUM(it.cantidad), 0) AS cantidad,
                    COALESCE(
                        SUM(it.neto + it.importeiva),
                        0
                    ) AS totalventa,
                    COALESCE(
                        SUM(it.costo * it.cantidad),
                        0
                    ) AS totalcosto,
                    COALESCE(
                        SUM(
                            (it.neto + it.importeiva)
                            - (it.costo * it.cantidad)
                        ),
                        0
                    ) AS ganancia
                FROM util.item it
                INNER JOIN util.ticket tic
                        ON tic.codigo = it.idticket
                WHERE tic.tipo = 1
                  AND tic.fecha::date BETWEEN ? AND ?
                GROUP BY
                    it.codigoarticulo,
                    it.descripcion
                ORDER BY
                    ganancia DESC,
                    it.descripcion ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(ps, desde, hasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteDTO.ProductoRentableDTO dto =
                            new ReporteDTO.ProductoRentableDTO();

                    dto.codigo =
                            rs.getString("codigo");

                    dto.descripcion =
                            texto(rs.getString("descripcion"));

                    dto.cantidad =
                            decimal(rs, "cantidad");

                    dto.totalVenta =
                            decimal(rs, "totalventa");

                    dto.totalCosto =
                            decimal(rs, "totalcosto");

                    dto.ganancia =
                            decimal(rs, "ganancia");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // EVOLUCIÓN DE VENTAS
    // -------------------------------------------------------------------------

    public List<ReporteDTO.EvolucionVentaDTO>
    obtenerEvolucionVentas(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        List<ReporteDTO.EvolucionVentaDTO> result =
                new ArrayList<>();

        if (!rangoValido(desde, hasta)) {
            return result;
        }

        String sql = """
                SELECT
                    tic.fecha::date AS fecha,
                    COUNT(*) AS tickets,
                    COALESCE(SUM(tic.subtotal), 0) AS subtotal,
                    COALESCE(
                        SUM(tic.bonificacion),
                        0
                    ) AS bonificacion,
                    COALESCE(SUM(tic.total), 0) AS total
                FROM util.ticket tic
                WHERE tic.tipo = 1
                  AND tic.fecha::date BETWEEN ? AND ?
                GROUP BY tic.fecha::date
                ORDER BY tic.fecha::date ASC
                """;

        try (
                Connection cn = dataSource.getConnection();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            setRango(ps, desde, hasta);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ReporteDTO.EvolucionVentaDTO dto =
                            new ReporteDTO.EvolucionVentaDTO();

                    dto.fecha =
                            rs.getDate("fecha");

                    dto.cantidadTickets =
                            rs.getInt("tickets");

                    dto.subtotal =
                            decimal(rs, "subtotal");

                    dto.bonificacion =
                            decimal(rs, "bonificacion");

                    dto.total =
                            decimal(rs, "total");

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // AUXILIARES
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
            ResultSet rs,
            String columna
    ) throws SQLException {

        BigDecimal valor =
                rs.getBigDecimal(columna);

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

}