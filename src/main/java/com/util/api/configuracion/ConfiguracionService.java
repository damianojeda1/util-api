package com.util.api.configuracion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ConfiguracionService {

    @Inject
    ConfiguracionRepository repository;

    public List<ConfiguracionDTO> obtenerTodas()
            throws SQLException {

        return repository.obtenerTodas();
    }

    public ConfiguracionDTO obtener(
            String clave
    ) throws SQLException {

        return repository.obtener(clave);
    }

    public boolean guardar(
            String clave,
            ConfiguracionDTO request
    ) throws SQLException {

        if (clave == null
                || clave.isBlank()
                || request == null) {

            return false;
        }

        /*
         * La clave del path tiene prioridad sobre cualquier
         * clave recibida dentro del JSON.
         */
        return repository.guardar(
                clave,
                request.valor
        );
    }

    public boolean guardarTodas(
            Map<String, String> configuraciones
    ) throws SQLException {

        return repository.guardarTodas(
                configuraciones
        );
    }

    public boolean eliminar(
            String clave
    ) throws SQLException {

        return repository.eliminar(clave);
    }
}