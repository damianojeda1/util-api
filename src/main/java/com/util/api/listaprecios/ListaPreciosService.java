package com.util.api.listaprecios;

import com.util.api.generico.ResInsertUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ListaPreciosService {

    @Inject
    ListaPreciosRepository repository;

    public List<ListaPreciosDTO.Response> obtenerCompletas()
            throws SQLException {

        List<ListaPreciosDTO.Response> resultado =
                repository.obtenerCompletas();

        return resultado == null
                ? new ArrayList<>()
                : resultado;
    }

    public ListaPreciosDTO.Response findById(
            int codigo
    ) throws SQLException {

        if (codigo <= 0) {
            return null;
        }

        return repository.findById(codigo);
    }

    public ResInsertUpdate insert(
            ListaPreciosDTO.CrearRequest request
    ) {
        if (!esValidaParaInsertar(request)) {
            return resultadoError();
        }

        return repository.insert(request);
    }

    public ResInsertUpdate update(
            int codigo,
            ListaPreciosDTO.ActualizarRequest request
    ) {
        if (codigo <= 0 || !esValidaParaActualizar(request)) {
            return resultadoError();
        }

        request.setCodigo(codigo);

        return repository.update(
                codigo,
                request
        );
    }

    public int nroNuevaLista() throws SQLException {
        return repository.nroNuevaLista();
    }

    private boolean esValidaParaInsertar(
            ListaPreciosDTO.CrearRequest request
    ) {
        return request != null
                && !esVacio(request.getDescripcion());
    }

    private boolean esValidaParaActualizar(
            ListaPreciosDTO.ActualizarRequest request
    ) {
        return request != null
                && !esVacio(request.getDescripcion());
    }

    private boolean esVacio(String valor) {
        return valor == null
                || valor.trim().isEmpty();
    }

    private ResInsertUpdate resultadoError() {
        return new ResInsertUpdate(
                "-1",
                false
        );
    }
}