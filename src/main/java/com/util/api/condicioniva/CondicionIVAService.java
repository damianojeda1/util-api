package com.util.api.condicioniva;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class CondicionIVAService {

    @Inject
    CondicionIVARepository repository;

    public List<CondicionIVADTO>
    obtenerHabilitadas() throws SQLException {

        return repository.obtenerHabilitadas();
    }
}