package com.util.api.mediopago;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class MedioPagoService {

    @Inject
    MedioPagoRepository repository;

    public List<MedioPagoDTO> listar(
            boolean incluirInactivos
    ) throws SQLException {

        return repository.listar(
                incluirInactivos
        );
    }

    public int insertar(
            MedioPagoDTO medioPago
    ) throws SQLException {

        return repository.insertar(
                medioPago
        );
    }

    public int actualizar(
            int id,
            MedioPagoDTO medioPago
    ) throws SQLException {

        return repository.actualizar(
                id,
                medioPago
        );
    }

    public int ocultar(
            int id
    ) throws SQLException {

        return repository.ocultar(id);
    }

    public int mostrar(
            int id
    ) throws SQLException {

        return repository.mostrar(id);
    }

    public MedioPagoDTO findById(
            int id
    ) throws SQLException {

        return repository.findById(id);
    }

    public MedioPagoDTO findPorCodigoEnum(
            String codigoEnum
    ) throws SQLException {

        return repository.findPorCodigoEnum(
                codigoEnum
        );
    }
}