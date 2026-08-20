package com.util.api.tipocomprobante;

import com.util.api.ticket.TicketDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;

@ApplicationScoped
public class TipoComprobanteService {

    @Inject
    TipoComprobanteRepository repository;

    public TicketDTO.TipoComprobanteDTO obtenerPorCodigo(
            String codigo
    ) throws SQLException {

        return repository.obtenerPorCodigo(
                codigo
        );
    }
}