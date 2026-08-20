package com.util.api.actualizacion;

import com.util.api.configuracion.ConfiguracionDTO;
import com.util.api.configuracion.ConfiguracionService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;

@ApplicationScoped
public class ActualizacionService {

    @Inject
    ActualizacionRepository repository;

    @Inject
    ConfiguracionService configuracionService;

    public int actualizar(
            ActualizacionDTO request
    ) throws SQLException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request obligatorio"
            );
        }

        if (request.versionActual < 0) {
            throw new IllegalArgumentException(
                    "Versión inválida"
            );
        }

        if (request.terminal == null
                || request.terminal.isBlank()) {

            throw new IllegalArgumentException(
                    "Terminal obligatoria"
            );
        }

        asegurarConfiguracionFacturacion();

        return repository.actualizarDB(
                request.versionActual,
                request.terminal
        );
    }

    private void asegurarConfiguracionFacturacion()
            throws SQLException {

        ConfiguracionDTO cuit =
                configuracionService.obtener(
                        "facturacion_cuit"
                );

        if (cuit != null
                && cuit.valor != null
                && !cuit.valor.isBlank()) {

            return;
        }

        guardarConfiguracion(
                "facturacion_cuit",
                "Definir"
        );

        guardarConfiguracion(
                "facturacion_ptovta",
                "Definir"
        );

        guardarConfiguracion(
                "facturacion_inicioactividad",
                "Definir(yyyy-mm-dd)"
        );

        guardarConfiguracion(
                "facturacion_certificado",
                "certificado.crt"
        );

        guardarConfiguracion(
                "facturacion_privada",
                "privada.key"
        );

        guardarConfiguracion(
                "facturacion_WSFEV1_h",
                "https://wswhomo.afip.gov.ar/wsfev1/service.asmx?WSDL"
        );

        guardarConfiguracion(
                "facturacion_WSAA_h",
                "https://wsaahomo.afip.gov.ar/ws/services/LoginCms"
        );

        guardarConfiguracion(
                "facturacion_WSFEV1",
                "https://servicios1.afip.gov.ar/wsfev1/service.asmx?WSDL"
        );

        guardarConfiguracion(
                "facturacion_WSAA",
                "https://wsaa.afip.gov.ar/ws/services/LoginCms?wsdl"
        );
    }

    private void guardarConfiguracion(
            String clave,
            String valor
    ) throws SQLException {

        ConfiguracionDTO dto =
                new ConfiguracionDTO();

        dto.clave = clave;
        dto.valor = valor;

        boolean ok =
                configuracionService.guardar(
                        clave,
                        dto
                );

        if (!ok) {
            throw new SQLException(
                    "No se pudo guardar configuración: "
                            + clave
            );
        }
    }
}