package com.util.api.compcompra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class CompCompraService {

    @Inject
    CompCompraRepository repository;

    public CompCompraDTO.ExisteResponse existeComprobante(
            int codigoProveedor,
            String letra,
            String centro,
            String numero
    ) throws SQLException {

        if (codigoProveedor <= 0
                || esVacio(letra)
                || esVacio(centro)
                || esVacio(numero)) {

            return new CompCompraDTO.ExisteResponse(false);
        }

        boolean existe =
                repository.existeComprobante(
                        codigoProveedor,
                        letra.trim(),
                        centro.trim(),
                        numero.trim()
                );

        return new CompCompraDTO.ExisteResponse(existe);
    }

    public CompCompraDTO.InsertResponse insertarComprobante(
            CompCompraDTO.ComprobanteRequest request
    ) throws SQLException {

        if (!esValidoComprobante(request)) {
            return insertError(
                    "Los datos del comprobante son inválidos"
            );
        }

        int codigo =
                repository.insertarComprobante(request);

        if (codigo <= 0) {
            return insertError(
                    "No se pudo insertar el comprobante"
            );
        }

        return new CompCompraDTO.InsertResponse(
                codigo,
                true,
                "Comprobante insertado correctamente"
        );
    }

    public CompCompraDTO.InsertResponse insertarPago(
            int codigoComprobante,
            CompCompraDTO.PagoRequest request
    ) throws SQLException {

        if (request == null) {
            return insertError(
                    "Los datos del pago son inválidos"
            );
        }

        request.codigoComprobante =
                codigoComprobante;

        if (!esValidoPago(request)) {
            return insertError(
                    "Los datos del pago son inválidos"
            );
        }

        int codigoPago =
                repository.insertarPago(request);

        if (codigoPago <= 0) {
            return insertError(
                    "No se pudo insertar el pago"
            );
        }

        return new CompCompraDTO.InsertResponse(
                codigoPago,
                true,
                "Pago insertado correctamente"
        );
    }

    public CompCompraDTO.PagoDTO obtenerPago(
            int codigoPago
    ) throws SQLException {

        if (codigoPago <= 0) {
            return null;
        }

        return repository.obtenerPago(codigoPago);
    }

    public CompCompraDTO.CuentaCorrienteDTO obtenerCuentaCorriente(
            int codigoProveedor,
            Date desde,
            Date hasta
    ) throws SQLException {

        if (codigoProveedor <= 0
                || desde == null
                || hasta == null) {

            return new CompCompraDTO.CuentaCorrienteDTO();
        }

        return repository.obtenerCuentaCorriente(
                codigoProveedor,
                desde,
                hasta
        );
    }

    public CompCompraDTO.ComprobanteDTO obtenerComprobante(
            int codigoComprobante
    ) throws SQLException {

        if (codigoComprobante <= 0) {
            return null;
        }

        return repository.obtenerComprobante(
                codigoComprobante
        );
    }

    public List<CompCompraDTO.LibroIvaCompraDTO> listarLibroIva(
            Date desde,
            Date hasta
    ) throws SQLException {

        if (desde == null || hasta == null) {
            return new ArrayList<>();
        }

        List<CompCompraDTO.LibroIvaCompraDTO> resultado =
                repository.listarLibroIva(
                        desde,
                        hasta
                );

        return resultado == null
                ? new ArrayList<>()
                : resultado;
    }

    public ResumenPeriodoDTO obtenerResumenPeriodo(
            Date desde,
            Date hasta
    ) throws SQLException {

        if (desde == null || hasta == null) {
            return new ResumenPeriodoDTO();
        }

        ResumenPeriodoDTO resultado =
                repository.obtenerResumenPeriodo(
                        desde,
                        hasta
                );

        return resultado == null
                ? new ResumenPeriodoDTO()
                : resultado;
    }

    public CompCompraDTO.OperacionResponse anularComprobante(
            int codigoComprobante
    ) throws SQLException {

        if (codigoComprobante <= 0) {
            return operacionError(
                    "El código del comprobante es inválido"
            );
        }

        boolean anulado =
                repository.anularComprobante(
                        codigoComprobante
                );

        if (!anulado) {
            return operacionError(
                    "No se pudo anular el comprobante"
            );
        }

        return new CompCompraDTO.OperacionResponse(
                true,
                "Comprobante anulado correctamente"
        );
    }

    public CompCompraDTO.OperacionResponse anularPago(
            int codigoPago,
            int codigoMovimientoCaja
    ) throws SQLException {

        if (codigoPago <= 0
                || codigoMovimientoCaja <= 0) {

            return operacionError(
                    "Los datos del pago son inválidos"
            );
        }

        boolean anulado =
                repository.anularPago(
                        codigoPago,
                        codigoMovimientoCaja
                );

        if (!anulado) {
            return operacionError(
                    "No se pudo anular el pago"
            );
        }

        return new CompCompraDTO.OperacionResponse(
                true,
                "Pago anulado correctamente"
        );
    }

    private boolean esValidoComprobante(
            CompCompraDTO.ComprobanteRequest request
    ) {
        if (request == null) {
            return false;
        }

        if (request.codigoProveedor <= 0) {
            return false;
        }

        if (request.codigoTipoComprobante <= 0) {
            return false;
        }

        if (request.fechaComprobante == null) {
            return false;
        }

        if (esVacio(request.letra)) {
            return false;
        }

        if (request.centro < 0) {
            return false;
        }

        if (request.numero < 0) {
            return false;
        }

        return request.items != null
                && !request.items.isEmpty();
    }

    private boolean esValidoPago(
            CompCompraDTO.PagoRequest request
    ) {
        if (request == null) {
            return false;
        }

        if (request.codigoComprobante <= 0) {
            return false;
        }

        if (request.codigoProveedor <= 0) {
            return false;
        }

        if (request.codigoCaja <= 0) {
            return false;
        }

        if (request.total <= 0) {
            return false;
        }

        return request.pagos != null
                && !request.pagos.isEmpty();
    }

    private CompCompraDTO.InsertResponse insertError(
            String mensaje
    ) {
        return new CompCompraDTO.InsertResponse(
                -1,
                false,
                mensaje
        );
    }

    private CompCompraDTO.OperacionResponse operacionError(
            String mensaje
    ) {
        return new CompCompraDTO.OperacionResponse(
                false,
                mensaje
        );
    }

    private boolean esVacio(String valor) {
        return valor == null
                || valor.trim().isEmpty();
    }
}