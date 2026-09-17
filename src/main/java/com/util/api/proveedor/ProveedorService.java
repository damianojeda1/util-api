package com.util.api.proveedor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProveedorService {

    @Inject
    ProveedorRepository repository;

    public List<ProveedorDTO> obtenerCompleto(
            boolean incluirProvExterno,
            int codigoUsuario
    ) throws SQLException {

        List<ProveedorDTO> resultado =
                repository.obtenerCompleto(
                        incluirProvExterno,
                        codigoUsuario
                );

        return resultado == null
                ? new ArrayList<>()
                : resultado;
    }

    public List<ProveedorDTO.ComboDTO> obtenerParaCombo(
            boolean incluirTodos
    ) throws SQLException {

        List<ProveedorDTO.ComboDTO> resultado =
                repository.obtenerParaCombo(
                        incluirTodos
                );

        return resultado == null
                ? new ArrayList<>()
                : resultado;
    }

    public ProveedorDTO buscarCod(
            int codigo,
            int codigoUsuario
    ) throws SQLException {

        if (codigo < 0) {
            return null;
        }

        return repository.buscarCod(
                codigo,
                codigoUsuario
        );
    }

    public ProveedorDTO buscarPorCodigoIdentificacion(
            String codigoIdentificacion,
            int codigoUsuario
    ) throws SQLException {

        if (esVacio(codigoIdentificacion)) {
            return null;
        }

        return repository.buscarPorCodigoIdentificacion(
                codigoIdentificacion.trim(),
                codigoUsuario
        );
    }

    public String obtenerRazonSocial(
            int codigo,
            int habilitado
    ) throws SQLException {

        if (codigo <= 0) {
            return "";
        }

        return repository.obtenerRazonSocial(
                codigo,
                habilitado
        );
    }

    public double obtenerSaldo(
            int codigoProveedor
    ) throws SQLException {

        if (codigoProveedor < 0) {
            return 0;
        }

        return repository.obtenerSaldo(
                codigoProveedor
        );
    }

    public boolean insertar(
            ProveedorDTO.GuardarRequest request
    ) throws SQLException {

        if (!esValidoGuardar(request, false)) {
            return false;
        }

        return repository.insertar(request);
    }

    public boolean actualizar(
            int codigoProveedor,
            ProveedorDTO.GuardarRequest request
    ) throws SQLException {

        if (codigoProveedor < 0
                || !esValidoGuardar(request, true)) {

            return false;
        }

        request.proveedor.codigo =
                codigoProveedor;

        return repository.actualizar(
                codigoProveedor,
                request
        );
    }

    private boolean esValidoGuardar(
            ProveedorDTO.GuardarRequest request,
            boolean requiereCodigo
    ) {
        if (request == null
                || request.proveedor == null) {

            return false;
        }

        ProveedorDTO proveedor =
                request.proveedor;

        if (requiereCodigo
                && proveedor.codigo < 0) {

            return false;
        }

        if (esVacio(proveedor.razonSocial)) {
            return false;
        }

        if (proveedor.tipoIdentificacionCodigo <= 0) {
            return false;
        }

        if (esVacio(proveedor.codigoIdentificacion)) {
            return false;
        }

        if (proveedor.condicionIvaCodigo <= 0) {
            return false;
        }

        return proveedor.localidadId > 0;
    }

    private boolean esVacio(
            String valor
    ) {
        return valor == null
                || valor.trim().isEmpty();
    }
}