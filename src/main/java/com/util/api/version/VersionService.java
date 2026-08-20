package com.util.api.version;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class VersionService {

    @Inject
    VersionRepository repository;

    public VersionDTO obtenerActual(
            int idApp,
            String terminal
    ) throws SQLException {

        return repository.obtenerActual(
                idApp,
                terminal
        );
    }

    public VersionDTO obtenerActual(
            int idApp,
            String terminalPrincipal,
            String terminalAlternativa
    ) throws SQLException {

        return repository.obtenerActual(
                idApp,
                terminalPrincipal,
                terminalAlternativa
        );
    }

    public boolean existe(
            int idApp,
            List<String> terminales
    ) throws SQLException {

        return repository.existe(
                idApp,
                terminales
        );
    }

    public boolean asegurarRegistroInicial(
            VersionDTO.IdentificacionRequest request
    ) throws SQLException {

        return repository.asegurarRegistroInicial(
                request
        );
    }

    public boolean actualizarVersion(
            VersionDTO.ActualizacionRequest request
    ) throws SQLException {

        if (request == null
                || request.idApp <= 0
                || request.terminal == null
                || request.terminal.isBlank()
                || request.versionActual == null) {

            return false;
        }

        return repository.actualizarVersion(
                request.idApp,
                request.terminal,
                request.versionActual,
                request.observacion
        );
    }
}