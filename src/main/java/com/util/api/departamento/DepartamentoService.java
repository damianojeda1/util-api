package com.util.api.departamento;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class DepartamentoService {
    @Inject
    DepartamentoRepository repository;

    public DepartamentoDTO findById(int codigo) throws SQLException {
        return repository.findById(codigo);
    }

    public DepartamentoDTO buscarPorDescripcion(String nombre) throws SQLException {
        return repository.buscarPorDescripcion(nombre);
    }

    public List<DepartamentoDTO> obtenerCompletos() throws SQLException {
        return repository.obtenerCompletos();
    }

    public List<DepartamentoDTO> obtenerHabilitados() throws SQLException {
        return repository.obtenerHabilitados();
    }

    public boolean insert(DepartamentoDTO departamento) throws SQLException {
        return repository.insert(departamento);
    }

    public boolean update(int codigo, DepartamentoDTO departamento) throws SQLException {
        if (departamento == null || codigo <= 1) return false;
        departamento.codigo = codigo;
        return repository.update(departamento);
    }

    public boolean delete(int codigo) throws SQLException {
        return repository.delete(codigo);
    }
}
