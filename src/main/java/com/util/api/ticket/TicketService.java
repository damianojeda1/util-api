package com.util.api.ticket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TicketService {

    @Inject
    TicketRepository repository;

    @Inject
    VentaRepository ventaRepository;

    // -------------------------------------------------------------------------
    // VENTA
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarVentaResponse guardarVenta(
            TicketDTO.GuardarVentaRequest request
    ) {
        return ventaRepository.guardarVenta(request);
    }

    // -------------------------------------------------------------------------
    // TICKETS
    // -------------------------------------------------------------------------

    public TicketDTO.GuardarTicketResponse guardarTicket(
            TicketDTO.GuardarTicketRequest request
    ) {

        return repository.guardarTicket(request);
    }

    public List<TicketDTO.ResumenTicketDTO> listarTickets(
            LocalDate desde,
            LocalDate hasta,
            boolean soloFacturados
    ) throws SQLException {

        return repository.listarTickets(
                desde,
                hasta,
                soloFacturados
        );
    }

    public TicketDTO.DetalleTicketDTO obtenerDetalle(
            int codigoTicket
    ) throws SQLException {

        return repository.obtenerDetalle(
                codigoTicket
        );
    }

    // -------------------------------------------------------------------------
    // FACTURACIÓN
    // -------------------------------------------------------------------------

    public TicketDTO.TicketFacturacionDTO obtenerParaFacturacion(
            int codigoTicket
    ) throws SQLException {

        return repository.obtenerParaFacturacion(
                codigoTicket
        );
    }

    public boolean guardarComprobanteFiscal(
            TicketDTO.GuardarComprobanteFiscalRequest request
    ) {

        return repository.guardarComprobanteFiscal(
                request
        );
    }

    public TicketDTO.ComprobanteFiscalDTO obtenerComprobanteFiscal(
            int codigoTicket
    ) throws SQLException {

        return repository.obtenerComprobanteFiscal(
                codigoTicket
        );
    }

    public List<TicketDTO.ComprobanteAsociadoDTO>
    listarComprobantesCliente(
            int codigoCliente,
            String cuit
    ) throws SQLException {

        return repository.listarComprobantesCliente(
                codigoCliente,
                cuit
        );
    }

    // -------------------------------------------------------------------------
    // REPORTES
    // -------------------------------------------------------------------------

    public List<TicketDTO.LibroIvaVentaDTO> listarLibroIva(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarLibroIva(
                desde,
                hasta
        );
    }

    public ResumenPeriodoDTO obtenerResumenPeriodo(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.obtenerResumenPeriodo(
                desde,
                hasta
        );
    }
}