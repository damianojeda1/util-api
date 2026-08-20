package com.util.api.recibo;

import com.util.api.ticket.TicketDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ReciboService {

    @Inject
    ReciboRepository repository;

    public TicketDTO.GuardarReciboResponse guardarRecibo(
            TicketDTO.GuardarReciboRequest request
    ) {

        return repository.guardarRecibo(
                request
        );
    }

    public TicketDTO.DetalleReciboDTO obtenerRecibo(
            int codigoRecibo
    ) throws SQLException {

        return repository.obtenerRecibo(
                codigoRecibo
        );
    }

    public List<TicketDTO.ResumenReciboDTO> listarRecibos(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarRecibos(
                desde,
                hasta
        );
    }

    public boolean anularRecibo(
            int codigoRecibo,
            int codigoMovimientoCaja
    ) {

        return repository.anularRecibo(
                codigoRecibo,
                codigoMovimientoCaja
        );
    }
}