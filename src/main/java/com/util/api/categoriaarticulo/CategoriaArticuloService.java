package com.util.api.categoriaarticulo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class CategoriaArticuloService {

    private static final int CATEGORIA_SIN_CATEGORIA = 1;

    @Inject
    CategoriaArticuloRepository repository;

    public CategoriaArticuloDTO findById(
            int codigo
    ) throws SQLException {

        return repository.findById(codigo);
    }

    public CategoriaArticuloDTO buscarPorDescripcion(
            String nombre
    ) throws SQLException {

        return repository.buscarPorDescripcion(
                nombre
        );
    }

    public boolean insert(
            CategoriaArticuloDTO categoria
    ) throws SQLException {

        return repository.insert(categoria);
    }

    public boolean update(
            int codigo,
            CategoriaArticuloDTO categoria
    ) throws SQLException {

        if (categoria == null
                || codigo == CATEGORIA_SIN_CATEGORIA) {

            return false;
        }

        /*
         * El código indicado por el path tiene prioridad
         * sobre el código recibido en el JSON.
         */
        categoria.codigo = codigo;

        return repository.update(categoria);
    }

    public List<CategoriaArticuloDTO>
    obtenerCompletos() throws SQLException {

        return repository.obtenerCompletos();
    }

    public boolean delete(
            int codigo
    ) throws SQLException {

        return repository.delete(codigo);
    }
}