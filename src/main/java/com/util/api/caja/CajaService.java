package com.util.api.caja;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class CajaService {

    @Inject
    CajaRepository repository;

    public CajaDTO obtenerUltimoCierre()
            throws SQLException {

        return repository.obtenerUltimoCierre();
    }

    public int abrirCaja(
            CajaDTO.AperturaRequest request
    ) throws SQLException {

        return repository.abrirCaja(request);
    }

    public List<CajaDTO> obtenerHistoricas()
            throws SQLException {

        return repository.obtenerHistoricas();
    }

    public CajaDTO obtenerDetalle(
            int codigoCaja,
            boolean incluirPagosProveedor
    ) throws SQLException {

        return repository.obtenerDetalle(
                codigoCaja,
                incluirPagosProveedor
        );
    }

    public CajaDTO.ImpresionCajaDTO obtenerParaImpresion(
            int codigoCaja
    ) throws SQLException {

        return repository.obtenerParaImpresion(
                codigoCaja
        );
    }

    public List<CajaDTO.PagoDTO> obtenerPagosMovimiento(
            int codigoMovimiento
    ) throws SQLException {

        return repository.obtenerPagosMovimiento(
                codigoMovimiento
        );
    }

    public boolean insertarMovimiento(
            CajaDTO.MovimientoRequest request
    ) throws SQLException {

        return repository.insertarMovimiento(request);
    }

    public boolean cerrarCaja(
            CajaDTO.CierreRequest request
    ) throws SQLException {

        return repository.cerrarCaja(request);
    }

    public List<CajaDTO> obtenerAbiertas()
            throws SQLException {

        return repository.obtenerAbiertas();
    }

    public CajaDTO.OperacionResponse cerrarAbiertasUsuario(
            CajaDTO.CerrarAbiertasUsuarioRequest request
    ) throws SQLException {

        if (request == null || request.codigoUsuario <= 0) {
            return new CajaDTO.OperacionResponse(
                    false,
                    "Usuario inválido"
            );
        }

        boolean resultado =
                repository.cerrarAbiertasUsuario(
                        request.codigoUsuario
                );

        return new CajaDTO.OperacionResponse(
                resultado,
                resultado
                        ? "Cajas cerradas correctamente"
                        : "No se pudieron cerrar las cajas"
        );
    }
}