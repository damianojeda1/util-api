package com.util.api.reportes.citi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class CitiService {

    @Inject
    CitiRepository repository;

    public List<CitiDTO.VentaDTO> obtenerVentas(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.obtenerVentas(
                desde,
                hasta
        );
    }

    public List<CitiDTO.CompraDTO> obtenerCompras(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.obtenerCompras(
                desde,
                hasta
        );
    }
}