package com.util.api.presupuesto;

import com.util.api.ticket.TicketDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PresupuestoService {

    @Inject
    PresupuestoRepository repository;

    public TicketDTO.GuardarPresupuestoResponse guardarPresupuesto(
            TicketDTO.GuardarPresupuestoRequest request
    ) {

        return repository.guardarPresupuesto(
                request
        );
    }

    public TicketDTO.PresupuestoDTO obtenerPresupuesto(
            int codigoPresupuesto
    ) throws SQLException {

        return repository.obtenerPresupuesto(
                codigoPresupuesto
        );
    }

    public TicketDTO.ClientePresupuestoDTO obtenerCliente(
            int codigoPresupuesto
    ) throws SQLException {

        return repository.obtenerCliente(
                codigoPresupuesto
        );
    }

    public List<TicketDTO.ResumenPresupuestoDTO> listarPresupuestos(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarPresupuestos(
                desde,
                hasta
        );
    }

    public boolean eliminarPresupuesto(
            int codigoPresupuesto
    ) {

        return repository.eliminarPresupuesto(
                codigoPresupuesto
        );
    }
}