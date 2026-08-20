package com.util.api.articulocanal;

import com.util.api.tiendanube.TiendanubeProductoDTO;
import com.util.api.tiendanube.TiendanubeService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class ArticuloCanalService {

    @Inject
    ArticuloCanalRepository repository;

    @Inject
    TiendanubeService tiendanubeService;

    public ArticuloCanalDTO obtener(
            String codigoArticulo,
            String codigoProveedor,
            String canal
    ) throws SQLException {

        return repository.obtener(
                codigoArticulo,
                codigoProveedor,
                canal
        );
    }

    public List<ArticuloCanalDTO> obtenerPorCanal(
            String canal
    ) throws SQLException {

        return repository.obtenerPorCanal(canal);
    }

    public ArticuloSincronizacionResponse sincronizar(
            ArticuloSincronizacionRequest request
    ) {

        ArticuloSincronizacionResponse response =
                new ArticuloSincronizacionResponse();

        if (
                request == null
                        || request.articulos == null
        ) {
            return response;
        }

        for (ArticuloSincronizacionDTO articulo : request.articulos) {

            ArticuloSincronizacionResultadoDTO resultado =
                    new ArticuloSincronizacionResultadoDTO();

            resultado.codigoArticulo =
                    articulo.codigoArticulo;

            resultado.codigoProveedor =
                    articulo.codigoProveedor;

            try {

                TiendanubeProductoDTO producto =
                        tiendanubeService.publicar(
                                articulo
                        );

                ArticuloCanalDTO canal =
                        new ArticuloCanalDTO();

                canal.codigoArticulo =
                        articulo.codigoArticulo;

                canal.codigoProveedor =
                        String.valueOf(
                                articulo.codigoProveedor
                        );

                canal.canal =
                        "TIENDANUBE";

                canal.idExterno =
                        String.valueOf(
                                producto.id
                        );

                if (
                        producto.variants != null
                                && !producto.variants.isEmpty()
                ) {

                    canal.idVarianteExterna =
                            String.valueOf(
                                    producto.variants
                                            .get(0)
                                            .id
                            );
                }

                canal.publicado = true;

                repository.guardar(canal);

                resultado.correcto = true;

                resultado.mensaje =
                        "Publicado correctamente";

            } catch (Exception ex) {

                ex.printStackTrace();

                resultado.correcto = false;

                resultado.mensaje =
                        ex.getMessage();
            }

            response.resultados.add(
                    resultado
            );
        }

        return response;
    }

    public boolean guardar(
            ArticuloCanalDTO dto
    ) throws SQLException {

        return repository.guardar(dto);
    }

    public boolean eliminar(
            String codigoArticulo,
            String codigoProveedor,
            String canal
    ) throws SQLException {

        return repository.eliminar(
                codigoArticulo,
                codigoProveedor,
                canal
        );
    }
}