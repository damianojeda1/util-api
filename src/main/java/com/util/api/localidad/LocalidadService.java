package com.util.api.localidad;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class LocalidadService {

    @Inject
    LocalidadRepository repository;

    public List<LocalidadDTO.Response> obtenerCompleto(
            String codigoPostal
    ) throws SQLException {

        if (esVacio(codigoPostal)) {
            return repository.obtenerCompleto();
        }

        return repository.buscarCodPostal(codigoPostal);
    }

    public LocalidadDTO.Response buscarId(
            int id
    ) throws SQLException {

        if (id <= 0) {
            return null;
        }

        return repository.buscarId(id);
    }

    public LocalidadDTO.Response buscarCodPostalNombre(
            String codigoPostal,
            String nombre
    ) throws SQLException {

        if (esVacio(codigoPostal) || esVacio(nombre)) {
            return null;
        }

        return repository.buscarCodPostalNombre(
                codigoPostal,
                nombre
        );
    }

    public LocalidadDTO.OperacionResponse insertar(
            LocalidadDTO.CrearRequest request
    ) throws SQLException {

        if (!esValidaParaInsertar(request)) {
            return new LocalidadDTO.OperacionResponse(
                    false,
                    0,
                    "Los datos de la localidad son inválidos"
            );
        }

        return repository.insertar(request);
    }

    private boolean esValidaParaInsertar(
            LocalidadDTO.CrearRequest request
    ) {
        return request != null
                && !esVacio(request.getNombre())
                && !esVacio(request.getCodigoPostal())
                && request.getProvinciaId() > 0;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}