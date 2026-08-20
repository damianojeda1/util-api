package com.util.api.tipoidentificacion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class TipoIdentificacionService {

    @Inject
    TipoIdentificacionRepository repository;

    public List<TipoIdentificacionDTO>
    obtenerParaCombo() throws SQLException {

        return repository.obtenerParaCombo();
    }
}