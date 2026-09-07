package com.util.api.cliente;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ClienteRepository {

    private static final String CODIGO_ERROR_GENERICO = "-1";
    private static final String CODIGO_ERROR_VALIDACION =
            "VALIDATION_ERROR";
    private static final String CODIGO_ERROR_NO_ENCONTRADO =
            "NOT_FOUND";

    private static final String SELECT_CLIENTE_COMPLETO = """
        SELECT
            cli.codigo,
            cli.razonsocial,
            cli.codigoidentificacion,
            cli.ingresosbrutos,
            cli.telfijo,
            cli.telmovil,
            cli.emailpersonal,
            cli.emailfacturacion,
            cli.domicilio,
            cli.localidad,
            cli.observacion,
            cli.limitectacte,
            cli.ctacte,
            cli.habilitado,

            COALESCE(civa.codigo, -1) AS condicioniva,
            COALESCE(civa.descripcion, '') AS nomcondicioniva,

            COALESCE(usr.codigo, -1) AS vendedor,
            COALESCE(usr.nombre, '') AS nomvendedor,

            COALESCE(tipoident.codigo, -1) AS codident,
            COALESCE(tipoident.descripcion, '') AS nomident,

            loc.cp AS localidad_codigopostal,
            loc.nombre AS localidad_nombre,
            prov.id AS provincia_id,
            prov.nombre AS provincia_nombre,
            pais.id AS pais_id,
            pais.nombre AS pais_nombre

        FROM util.cliente cli

        LEFT JOIN util.tipoidentificacion tipoident
               ON tipoident.codigo = cli.tipoidentificacion

        LEFT JOIN util.condicioniva civa
               ON civa.codigo = cli.condicioniva

        LEFT JOIN util.usuario usr
               ON usr.codigo = cli.vendedor

        LEFT JOIN util.localidad loc
               ON loc.id = cli.localidad

       LEFT JOIN util.provincia prov
                  ON prov.id = loc.provincia

       LEFT JOIN util.pais pais
              ON pais.id = prov.pais
       """;

    private static final String INSERT_CLIENTE = """
            INSERT INTO util.cliente (
                codigo,
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
                vendedor,
                localidad,
                observacion,
                ctacte,
                limitectacte,
                habilitado
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            """;

    private static final String UPDATE_CLIENTE = """
            UPDATE util.cliente
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
                vendedor = ?,
                observacion = ?,
                ctacte = ?,
                limitectacte = ?,
                habilitado = ?
            WHERE codigo = ?
            """;

    private static final String INSERT_TICKET_SALDO_INICIAL = """
            INSERT INTO util.ticket (
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
                observacion
            )
            VALUES (
                now(),
                ?,
                0,
                0,
                ?,
                ?,
                ?,
                ?,
                ?,
                0,
                'Inicial CtaCte'
            )
            RETURNING codigo
            """;

    private static final String INSERT_ITEM_SALDO_INICIAL = """
            INSERT INTO util.item (
                idticket,
                proveedor,
                codigoarticulo,
                cantidad,
                costo,
                margen,
                neto,
                alicuota,
                importeiva,
                precio,
                bonificacion,
                descripcion
            )
            VALUES (
                ?,
                0,
                '0',
                1.0,
                ?,
                0.0,
                ?,
                0,
                0,
                ?,
                0.0,
                'Inicial CtaCte'
            )
            """;

    @Inject
    DataSource dataSource;

    public List<ClienteDTO.Response> listar(
            Boolean habilitado,
            boolean excluirGenericos
    ) throws SQLException {

        String sql;

        if (Boolean.TRUE.equals(habilitado)
                && excluirGenericos) {

            sql = SELECT_CLIENTE_COMPLETO + """
                    WHERE cli.habilitado = TRUE
                      AND cli.codigo > 1
                    ORDER BY cli.codigo ASC
                    """;

        } else if (Boolean.TRUE.equals(habilitado)) {

            /*
             * Replica listarHabilitados() del DBDAO.
             * No es simplemente habilitado = TRUE:
             * también excluye identificaciones iguales a "0".
             */
            sql = SELECT_CLIENTE_COMPLETO + """
                    WHERE cli.habilitado = TRUE
                      AND cli.codigoidentificacion <> '0'
                    ORDER BY cli.codigo ASC
                    """;

        } else if (Boolean.FALSE.equals(habilitado)) {

            sql = SELECT_CLIENTE_COMPLETO + """
                    WHERE cli.habilitado = FALSE
                    ORDER BY cli.codigo ASC
                    """;

        } else {
            sql = SELECT_CLIENTE_COMPLETO + """
                    ORDER BY cli.codigo ASC
                    """;
        }

        List<ClienteDTO.Response> clientes =
                new ArrayList<>();

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {
            while (rs.next()) {
                clientes.add(mapCliente(rs));
            }
        }

        return clientes;
    }

    public ClienteDTO.Response findById(
            int codigo
    ) throws SQLException {

        if (codigo < 0) {
            return null;
        }

        String sql =
                SELECT_CLIENTE_COMPLETO
                        + " WHERE cli.codigo = ? ";

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(sql)
        ) {
            ps.setInt(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCliente(rs);
                }
            }
        }

        return null;
    }

    public ClienteDTO.OperacionResponse insert(
            ClienteDTO.CrearRequest request
    ) {
        if (!esValido(request)) {
            return error(CODIGO_ERROR_VALIDACION);
        }

        Connection connection = null;
        boolean autoCommitAnterior = true;

        try {
            connection = dataSource.getConnection();

            autoCommitAnterior =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            /*
             * Se mantiene el LOCK porque el esquema genera
             * códigos mediante MAX(codigo) + 1.
             */
            bloquearTablaClientes(connection);

            int nuevoCodigo =
                    obtenerProximoCodigo(connection);

            insertarCliente(
                    connection,
                    nuevoCodigo,
                    request
            );

            if (request.montoInicialCuentaCorriente > 0) {
                validarUsuarioActual(request);

                insertarSaldoInicial(
                        connection,
                        nuevoCodigo,
                        request
                );
            }

            connection.commit();

            return exito();

        } catch (SQLException e) {
            rollbackSilencioso(connection);
            e.printStackTrace();

            return error(obtenerCodigoSql(e));

        } catch (Exception e) {
            rollbackSilencioso(connection);
            e.printStackTrace();

            return error(CODIGO_ERROR_GENERICO);

        } finally {
            restaurarAutoCommit(
                    connection,
                    autoCommitAnterior
            );

            cerrarSilencioso(connection);
        }
    }

    public ClienteDTO.OperacionResponse update(
            int codigo,
            ClienteDTO.ActualizarRequest request
    ) {
        if (codigo < 0 || !esValido(request)) {
            return error(CODIGO_ERROR_VALIDACION);
        }

        try (
                Connection connection =
                        dataSource.getConnection();
                PreparedStatement ps =
                        connection.prepareStatement(
                                UPDATE_CLIENTE
                        )
        ) {
            cargarParametrosUpdate(
                    ps,
                    codigo,
                    request
            );

            int filasAfectadas =
                    ps.executeUpdate();

            if (filasAfectadas != 1) {
                return error(
                        CODIGO_ERROR_NO_ENCONTRADO
                );
            }

            return exito();

        } catch (SQLException e) {
            e.printStackTrace();

            return error(
                    obtenerCodigoSql(e)
            );

        } catch (Exception e) {
            e.printStackTrace();

            return error(
                    CODIGO_ERROR_GENERICO
            );
        }
    }

    public int obtenerProximoCodigoEstimado()
            throws SQLException {

        try (
                Connection connection =
                        dataSource.getConnection()
        ) {
            return obtenerProximoCodigo(connection);
        }
    }

    public double obtenerSaldo(
            int clienteId
    ) throws SQLException {

        if (clienteId <= 0) {
            return 0;
        }

        String sql = """
                SELECT
                    util.saldoHasta(
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
            ps.setInt(1, clienteId);

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

    private ClienteDTO.Response mapCliente(
            ResultSet rs
    ) throws SQLException {

        ClienteDTO.Response cliente =
                new ClienteDTO.Response();

        cliente.codigo =
                rs.getInt("codigo");

        cliente.razonSocial =
                rs.getString("razonsocial");

        cliente.tipoIdentificacionId =
                rs.getInt("codident");

        cliente.tipoIdentificacionNombre =
                rs.getString("nomident");

        cliente.codigoIdentificacion =
                rs.getString("codigoidentificacion");

        cliente.condicionIvaId =
                rs.getInt("condicioniva");

        cliente.condicionIvaNombre =
                rs.getString("nomcondicioniva");

        cliente.ingresosBrutos =
                rs.getString("ingresosbrutos");

        cliente.telefonoFijo =
                rs.getString("telfijo");

        cliente.telefonoMovil =
                rs.getString("telmovil");

        cliente.emailPersonal =
                rs.getString("emailpersonal");

        cliente.emailFacturacion =
                rs.getString("emailfacturacion");

        cliente.domicilio =
                rs.getString("domicilio");

        cliente.vendedorId =
                rs.getInt("vendedor");

        cliente.vendedorNombre =
                rs.getString("nomvendedor");

        cliente.localidadId =
                rs.getInt("localidad");

        cliente.localidadCodigoPostal =
                rs.getString("localidad_codigopostal");

        cliente.localidadNombre =
                rs.getString("localidad_nombre");

        cliente.provinciaId =
                rs.getInt("provincia_id");

        cliente.provinciaNombre =
                rs.getString("provincia_nombre");

        cliente.paisId =
                rs.getInt("pais_id");

        cliente.paisNombre =
                rs.getString("pais_nombre");

        cliente.observacion =
                rs.getString("observacion");

        cliente.limiteCuentaCorriente =
                rs.getDouble("limitectacte");

        cliente.cuentaCorriente =
                rs.getBoolean("ctacte");

        cliente.habilitado =
                rs.getBoolean("habilitado");

        return cliente;
    }

    private void bloquearTablaClientes(
            Connection connection
    ) throws SQLException {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                "LOCK TABLE util.cliente "
                                        + "IN EXCLUSIVE MODE"
                        )
        ) {
            ps.execute();
        }
    }

    private int obtenerProximoCodigo(
            Connection connection
    ) throws SQLException {

        String sql = """
                SELECT
                    COALESCE(MAX(codigo), 0) + 1
                        AS codigo
                FROM util.cliente
                """;

        try (
                PreparedStatement ps =
                        connection.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {
            if (!rs.next()) {
                throw new SQLException(
                        "No se pudo obtener el próximo "
                                + "código de cliente"
                );
            }

            return rs.getInt("codigo");
        }
    }

    private void insertarCliente(
            Connection connection,
            int codigo,
            ClienteDTO.CrearRequest request
    ) throws SQLException {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                INSERT_CLIENTE
                        )
        ) {
            ps.setInt(1, codigo);
            ps.setString(2, request.razonSocial);
            ps.setInt(3, request.tipoIdentificacionId);
            ps.setString(4, request.codigoIdentificacion);
            ps.setInt(5, request.condicionIvaId);
            ps.setString(6, request.ingresosBrutos);
            ps.setString(7, request.telefonoFijo);
            ps.setString(8, request.telefonoMovil);
            ps.setString(9, request.emailPersonal);
            ps.setString(10, request.emailFacturacion);
            ps.setString(11, request.domicilio);
            ps.setInt(12, request.vendedorId);
            ps.setInt(13, request.localidadId);
            ps.setString(14, request.observacion);
            ps.setBoolean(15, request.cuentaCorriente);
            ps.setDouble(
                    16,
                    request.limiteCuentaCorriente
            );
            ps.setBoolean(17, request.habilitado);

            int filasAfectadas =
                    ps.executeUpdate();

            if (filasAfectadas != 1) {
                throw new SQLException(
                        "No se pudo insertar el cliente"
                );
            }
        }
    }

    private void insertarSaldoInicial(
            Connection connection,
            int codigoCliente,
            ClienteDTO.CrearRequest request
    ) throws SQLException {

        double monto =
                request.montoInicialCuentaCorriente;

        int codigoTicket =
                insertarTicketSaldoInicial(
                        connection,
                        codigoCliente,
                        request.razonSocial,
                        request.usuarioActualId,
                        request.usuarioActualNombre,
                        monto
                );

        insertarItemSaldoInicial(
                connection,
                codigoTicket,
                monto
        );
    }

    private int insertarTicketSaldoInicial(
            Connection connection,
            int codigoCliente,
            String razonSocial,
            int usuarioId,
            String usuarioNombre,
            double monto
    ) throws SQLException {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                INSERT_TICKET_SALDO_INICIAL
                        )
        ) {
            ps.setDouble(1, monto);
            ps.setDouble(2, monto);
            ps.setInt(3, usuarioId);
            ps.setString(4, usuarioNombre);
            ps.setInt(5, codigoCliente);
            ps.setString(6, razonSocial);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo crear el ticket "
                                    + "del saldo inicial"
                    );
                }

                return rs.getInt("codigo");
            }
        }
    }

    private void insertarItemSaldoInicial(
            Connection connection,
            int codigoTicket,
            double monto
    ) throws SQLException {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                INSERT_ITEM_SALDO_INICIAL
                        )
        ) {
            ps.setInt(1, codigoTicket);
            ps.setDouble(2, monto);
            ps.setDouble(3, monto);
            ps.setDouble(4, monto);

            int filasAfectadas =
                    ps.executeUpdate();

            if (filasAfectadas != 1) {
                throw new SQLException(
                        "No se pudo crear el ítem "
                                + "del saldo inicial"
                );
            }
        }
    }

    private void cargarParametrosUpdate(
            PreparedStatement ps,
            int codigo,
            ClienteDTO.ActualizarRequest request
    ) throws SQLException {

        ps.setString(1, request.razonSocial);
        ps.setInt(2, request.tipoIdentificacionId);
        ps.setString(3, request.codigoIdentificacion);
        ps.setInt(4, request.condicionIvaId);
        ps.setString(5, request.ingresosBrutos);
        ps.setString(6, request.telefonoFijo);
        ps.setString(7, request.telefonoMovil);
        ps.setString(8, request.emailPersonal);
        ps.setString(9, request.emailFacturacion);
        ps.setString(10, request.domicilio);
        ps.setInt(11, request.localidadId);
        ps.setInt(12, request.vendedorId);
        ps.setString(13, request.observacion);
        ps.setBoolean(14, request.cuentaCorriente);
        ps.setDouble(
                15,
                request.limiteCuentaCorriente
        );
        ps.setBoolean(16, request.habilitado);
        ps.setInt(17, codigo);
    }

    private boolean esValido(
            ClienteDTO.CrearRequest request
    ) {
        return request != null
                && textoValido(request.razonSocial)
                && request.tipoIdentificacionId >= 0
                && request.condicionIvaId >= 0
                && request.vendedorId >= 0
                && request.localidadId >= 0;
    }

    private boolean esValido(
            ClienteDTO.ActualizarRequest request
    ) {
        return request != null
                && textoValido(request.razonSocial)
                && request.tipoIdentificacionId >= 0
                && request.condicionIvaId >= 0
                && request.vendedorId >= 0
                && request.localidadId >= 0;
    }

    private boolean textoValido(String valor) {
        return valor != null
                && !valor.trim().isEmpty();
    }

    private void validarUsuarioActual(
            ClienteDTO.CrearRequest request
    ) throws SQLException {

        if (request.usuarioActualId <= 0
                || !textoValido(
                request.usuarioActualNombre
        )) {

            throw new SQLException(
                    "No hay un usuario activo para "
                            + "registrar el saldo inicial"
            );
        }
    }

    private ClienteDTO.OperacionResponse exito() {
        return new ClienteDTO.OperacionResponse(
                true,
                CODIGO_ERROR_GENERICO
        );
    }

    private ClienteDTO.OperacionResponse error(
            String codigo
    ) {
        return new ClienteDTO.OperacionResponse(
                false,
                codigo
        );
    }

    private String obtenerCodigoSql(
            SQLException e
    ) {
        return e.getSQLState() != null
                ? e.getSQLState()
                : CODIGO_ERROR_GENERICO;
    }

    private double redondear(
            double valor,
            int decimales
    ) {
        double factor =
                Math.pow(10, decimales);

        return Math.round(valor * factor)
                / factor;
    }

    private void rollbackSilencioso(
            Connection connection
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void restaurarAutoCommit(
            Connection connection,
            boolean autoCommitAnterior
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(
                    autoCommitAnterior
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cerrarSilencioso(
            Connection connection
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}