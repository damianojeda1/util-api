package com.util.api.parametrosconexion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ParametrosConexionService {

    @Inject
    ParametrosConexionRepository repository;

    public ParametrosConexionDTO obtenerConexionUtil() {
        return repository.obtenerConexionUtil();
    }

    public ParametrosConexionDTO obtenerConexionProveedor(
            int codigoProveedor
    ) {
        if (codigoProveedor <= 0) {
            return null;
        }

        return repository.obtenerConexionProveedor(
                codigoProveedor
        );
    }
}