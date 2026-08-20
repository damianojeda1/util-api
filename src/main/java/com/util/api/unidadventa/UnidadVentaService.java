package com.util.api.unidadventa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class UnidadVentaService {

    @Inject
    UnidadVentaRepository repository;

    public int insertar(
            UnidadVentaDTO.GuardarRequest request
    ) throws SQLException {

        if (request == null
                || request.nombre == null
                || request.nombre.trim().isEmpty()) {

            return 0;
        }

        return repository.insertar(
                request.nombre.trim()
        );
    }

    public List<UnidadVentaDTO> obtenerTodos()
            throws SQLException {

        return repository.obtenerTodos();
    }
}