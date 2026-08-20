package com.util.api.impresora;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ImpresoraService {

    @Inject
    ImpresoraRepository repository;

    public List<ImpresoraDTO.Response> listarPorTerminal(
            String terminal
    ) throws SQLException {

        if (esVacio(terminal)) {
            return new ArrayList<>();
        }

        return repository.listarPorTerminal(
                terminal.trim()
        );
    }

    public ImpresoraDTO.Response buscarTerminalReporte(
            String terminal,
            String reporte
    ) throws SQLException {

        if (esVacio(terminal) || esVacio(reporte)) {
            return null;
        }

        return repository.buscarTerminalReporte(
                terminal.trim(),
                reporte.trim()
        );
    }

    public ImpresoraDTO.OperacionResponse insertar(
            ImpresoraDTO.CrearRequest request
    ) throws SQLException {

        if (!esValidaParaInsertar(request)) {
            return operacionFallida(
                    "Los datos de la impresora son inválidos"
            );
        }

        boolean insertada =
                repository.insertar(request);

        if (!insertada) {
            return operacionFallida(
                    "No se pudo insertar la impresora"
            );
        }

        return operacionExitosa(
                "Impresora insertada correctamente"
        );
    }

    public ImpresoraDTO.OperacionResponse guardarOActualizar(
            ImpresoraDTO.CrearRequest request
    ) throws SQLException {

        if (!esValidaParaGuardar(request)) {
            return operacionFallida(
                    "Los datos de la impresora son inválidos"
            );
        }

        boolean guardada =
                repository.guardarOActualizar(request);

        if (!guardada) {
            return operacionFallida(
                    "No se pudo guardar la impresora"
            );
        }

        return operacionExitosa(
                "Impresora guardada correctamente"
        );
    }

    public ImpresoraDTO.OperacionResponse eliminar(
            ImpresoraDTO.EliminarRequest request
    ) throws SQLException {

        if (!esValidaParaEliminar(request)) {
            return operacionFallida(
                    "Los datos para eliminar la impresora son inválidos"
            );
        }

        boolean eliminada =
                repository.eliminar(request);

        if (!eliminada) {
            return operacionFallida(
                    "No se encontró la impresora para eliminar"
            );
        }

        return operacionExitosa(
                "Impresora eliminada correctamente"
        );
    }

    private boolean esValidaParaInsertar(
            ImpresoraDTO.CrearRequest request
    ) {
        return request != null
                && !esVacio(request.getReporte())
                && !esVacio(request.getNombreImpresora())
                && !esVacio(request.getTerminal());
    }

    private boolean esValidaParaGuardar(
            ImpresoraDTO.CrearRequest request
    ) {
        return request != null
                && !esVacio(request.getReporte())
                && !esVacio(request.getTerminal());
    }

    private boolean esValidaParaEliminar(
            ImpresoraDTO.EliminarRequest request
    ) {
        return request != null
                && !esVacio(request.getReporte())
                && !esVacio(request.getNombreImpresora())
                && !esVacio(request.getTerminal());
    }

    private ImpresoraDTO.OperacionResponse operacionExitosa(
            String mensaje
    ) {
        return new ImpresoraDTO.OperacionResponse(
                true,
                mensaje
        );
    }

    private ImpresoraDTO.OperacionResponse operacionFallida(
            String mensaje
    ) {
        return new ImpresoraDTO.OperacionResponse(
                false,
                mensaje
        );
    }

    private boolean esVacio(String valor) {
        return valor == null
                || valor.trim().isEmpty();
    }
}