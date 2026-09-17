package com.util.api.articulo;

import com.util.api.generico.ResInsertUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class ArticuloService {

    @Inject
    ArticuloRepository repository;

    public List<ArticuloDTO> obtenerCompletos(
            int habilitados,
            int categoria,
            int proveedor
    ) throws SQLException {
        return repository.obtenerCompletos(
                habilitados,
                categoria,
                proveedor
        );
    }

    public List<ArticuloDTO> buscarCodArticulo(
            String codigo,
            String proveedor
    ) throws SQLException {
        return repository.buscarCodArticulo(codigo, proveedor);
    }

    public ArticuloDTO buscarUnoPorCodigoYProveedor(
            String codigo,
            int proveedor
    ) throws SQLException {
        List<ArticuloDTO> articulos =
                repository.buscarCodArticulo(
                        codigo,
                        String.valueOf(proveedor)
                );

        if (articulos.isEmpty()) {
            return null;
        }

        return articulos.get(0);
    }

    public List<ArticuloDTO> buscarParaVenta(
            String filtro,
            int codigoUsuario,
            int limite
    ) throws SQLException {
        return repository.buscarParaVenta(
                filtro,
                codigoUsuario,
                limite
        );
    }

    public List<ArticuloDTO> buscarParaVentaPorCodigo(
            String codigo,
            int codigoUsuario
    ) throws SQLException {
        return repository.buscarParaVentaPorCodigo(
                codigo,
                codigoUsuario
        );
    }

    public ResInsertUpdate insert(
            ArticuloDTO articulo
    ) {
        return repository.insert(articulo);
    }

    public ResInsertUpdate update(
            String codigo,
            int proveedor,
            ArticuloDTO articulo
    ) {
        return repository.update(
                codigo,
                proveedor,
                articulo
        );
    }

    public ResInsertUpdate cambiarProveedor(
            ArticuloDTO.CambiarProveedorRequest request
    ) {
        return repository.cambiarProveedor(request);
    }

    public int establecerMargenPorProveedor(
            ArticuloDTO.EstablecerMargenRequest request
    ) throws SQLException {
        return repository.establecerMargenPorProveedor(request);
    }

    public int actualizarCostosPorPorcentaje(
            ArticuloDTO.ActualizarCostosRequest request
    ) throws SQLException {
        return repository.actualizarCostosPorPorcentaje(request);
    }

    public boolean actualizarCodigo(
            ArticuloDTO.CambiarCodigoRequest request
    ) throws SQLException {
        return repository.actualizarCodigo(request);
    }

    @Transactional
    public int actualizarCostosPorProveedor(
            int codigoProveedor,
            double porcentaje
    ) {
        if (codigoProveedor < 0) {
            throw new IllegalArgumentException(
                    "El proveedor no es válido"
            );
        }

        if (!Double.isFinite(porcentaje)) {
            throw new IllegalArgumentException(
                    "El porcentaje no es válido"
            );
        }

        return repository.actualizarCostosPorProveedor(
                codigoProveedor,
                porcentaje
        );
    }

    public int establecerMargenPorCategoria(
            int codigoCategoria,
            double margen
    ) {
        if (codigoCategoria <= 0) {
            return 0;
        }

        if (!Double.isFinite(margen)) {
            return 0;
        }

        return repository.establecerMargenPorCategoria(
                codigoCategoria,
                margen
        );
    }

    public ResInsertUpdate updateFromExcel(
            ArticuloDTO articulo,
            boolean actualizaStock,
            boolean actualizaMargen
    ) {
        return repository.updateFromExcel(
                articulo,
                actualizaStock,
                actualizaMargen
        );
    }

    public boolean delete(
            String codigo,
            int proveedor,
            int origen
    ) throws SQLException {
        return repository.delete(
                codigo,
                proveedor,
                origen
        );
    }
}