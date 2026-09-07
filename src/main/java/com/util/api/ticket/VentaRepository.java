package com.util.api.ticket;

import com.util.api.recibo.ReciboRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
public class VentaRepository {

    @Inject
    DataSource dataSource;

    @Inject
    TicketRepository ticketRepository;

    @Inject
    ReciboRepository reciboRepository;

    // -------------------------------------------------------------------------
    // GUARDAR VENTA COMPLETA
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarVentaResponse guardarVenta(
            TicketDTO.GuardarVentaRequest request
    ) {

        if (request == null) {
            return TicketDTO.GuardarVentaResponse.error(
                    "Los datos de la venta son obligatorios"
            );
        }

        if (request.ticket == null) {
            return TicketDTO.GuardarVentaResponse.error(
                    "Los datos del ticket son obligatorios"
            );
        }

        if (request.recibo == null) {
            return TicketDTO.GuardarVentaResponse.error(
                    "Los datos del recibo son obligatorios"
            );
        }

        try (Connection cn = dataSource.getConnection()) {

            boolean autoCommitOriginal =
                    cn.getAutoCommit();

            try {
                cn.setAutoCommit(false);

                // -------------------------------------------------------------
                // TICKET
                // -------------------------------------------------------------

                TicketDTO.GuardarTicketResponse ticketResponse =
                        ticketRepository.guardarTicket(
                                cn,
                                request.ticket
                        );

                if (ticketResponse == null
                        || !ticketResponse.ok) {

                    throw new SQLException(
                            ticketResponse != null
                                    ? ticketResponse.mensaje
                                    : "Error guardando ticket"
                    );
                }

                // -------------------------------------------------------------
                // RECIBO
                // -------------------------------------------------------------

                TicketDTO.GuardarReciboResponse reciboResponse =
                        reciboRepository.guardarRecibo(
                                cn,
                                request.recibo
                        );

                if (reciboResponse == null
                        || !reciboResponse.ok) {

                    throw new SQLException(
                            reciboResponse != null
                                    ? reciboResponse.mensaje
                                    : "Error guardando recibo"
                    );
                }

                // -------------------------------------------------------------
                // TODO OK
                // -------------------------------------------------------------

                cn.commit();

                return TicketDTO.GuardarVentaResponse.ok(
                        ticketResponse.codigo,
                        reciboResponse.codigo,
                        reciboResponse.codigoMovimientoCaja
                );

            } catch (Exception ex) {

                rollback(cn);

                ex.printStackTrace();

                return TicketDTO.GuardarVentaResponse.error(
                        "Error guardando venta: "
                                + mensaje(ex)
                );

            } finally {

                restaurarAutoCommit(
                        cn,
                        autoCommitOriginal
                );
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            return TicketDTO.GuardarVentaResponse.error(
                    "Error obteniendo conexión: "
                            + mensaje(ex)
            );
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

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