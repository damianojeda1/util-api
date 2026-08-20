package com.util.api.reportes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ReporteService {

    @Inject
    ReporteRepository repository;

    public ReporteComprobanteDTO obtenerComprobante(
            int codigoTicket
    ) throws SQLException {

        if (codigoTicket <= 0) {
            return new ReporteComprobanteDTO();
        }

        return repository.obtenerComprobante(
                codigoTicket
        );
    }

    public ReporteComprobanteDTO obtenerPresupuesto(
            int codigoPresupuesto
    ) throws SQLException {

        if (codigoPresupuesto <= 0) {
            return new ReporteComprobanteDTO();
        }

        return repository.obtenerPresupuesto(
                codigoPresupuesto
        );
    }

    public ReporteComprobanteDTO obtenerRecibo(
            int codigoRecibo
    ) throws SQLException {

        if (codigoRecibo <= 0) {
            return new ReporteComprobanteDTO();
        }

        return repository.obtenerRecibo(
                codigoRecibo
        );
    }

    public List<ReporteDTO.DeudaClienteDTO>
    listarClientesConDeuda(
            LocalDate hasta
    ) throws SQLException {

        return repository.listarClientesConDeuda(
                hasta
        );
    }

    public List<ReporteDTO.ProductoSinMovimientoDTO>
    listarProductosSinMovimiento(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarProductosSinMovimiento(
                desde,
                hasta
        );
    }

    public List<ReporteDTO.VentaProductoDTO>
    listarVentasPorProducto(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarVentasPorProducto(
                desde,
                hasta
        );
    }

    public List<ReporteDTO.ProductoRentableDTO>
    listarProductosRentables(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.listarProductosRentables(
                desde,
                hasta
        );
    }

    public List<ReporteDTO.EvolucionVentaDTO>
    obtenerEvolucionVentas(
            LocalDate desde,
            LocalDate hasta
    ) throws SQLException {

        return repository.obtenerEvolucionVentas(
                desde,
                hasta
        );
    }
}