package com.util.api.cliente.cuentacorriente;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class CuentaCorrienteService {

    @Inject
    CuentaCorrienteRepository repository;

    public CuentaCorrienteDTO.ResumenDTO obtenerResumen(
            int codigoCliente,
            String desde,
            String hasta
    ) throws SQLException {

        CuentaCorrienteDTO.ResumenDTO resumenVacio =
                new CuentaCorrienteDTO.ResumenDTO();

        if (codigoCliente <= 1
                || desde == null
                || desde.isBlank()
                || hasta == null
                || hasta.isBlank()) {

            return resumenVacio;
        }

        try {
            LocalDate fechaDesde =
                    LocalDate.parse(desde);

            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            if (fechaDesde.isAfter(fechaHasta)) {
                return resumenVacio;
            }

            return repository.obtenerResumen(
                    codigoCliente,
                    fechaDesde,
                    fechaHasta
            );

        } catch (DateTimeParseException e) {
            return resumenVacio;
        }
    }
}