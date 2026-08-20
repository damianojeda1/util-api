package com.util.api.impuestos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class ImpuestoService {

    @Inject
    ImpuestoRepository repository;

    public List<ImpuestoDTO> obtenerTodos()
            throws SQLException {

        return repository.obtenerTodos();
    }
}