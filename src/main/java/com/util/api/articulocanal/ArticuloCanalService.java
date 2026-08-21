package com.util.api.articulocanal;

import com.util.api.tiendanube.TiendanubeNotFoundException;
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
                ArticuloCanalDTO existente =
                        repository.obtener(
                                articulo.codigoArticulo,
                                String.valueOf(
                                        articulo.codigoProveedor
                                ),
                                request.canal
                        );

                if (existente == null) {

                    TiendanubeProductoDTO producto =
                            tiendanubeService.publicar(
                                    articulo
                            );

                    if (
                            producto == null
                                    || producto.id == null
                    ) {
                        throw new RuntimeException(
                                "Tiendanube no devolvió productId"
                        );
                    }

                    if (
                            producto.variants == null
                                    || producto.variants.isEmpty()
                                    || producto.variants.get(0).id == null
                    ) {
                        throw new RuntimeException(
                                "Tiendanube no devolvió variantId"
                        );
                    }

                    ArticuloCanalDTO canal =
                            new ArticuloCanalDTO();

                    canal.codigoArticulo =
                            articulo.codigoArticulo;

                    canal.codigoProveedor =
                            String.valueOf(
                                    articulo.codigoProveedor
                            );

                    canal.canal =
                            request.canal;

                    canal.idExterno =
                            String.valueOf(
                                    producto.id
                            );

                    canal.idVarianteExterna =
                            String.valueOf(
                                    producto.variants
                                            .get(0)
                                            .id
                            );

                    canal.publicado = true;

                    repository.guardar(
                            canal
                    );

                    if (articulo.sincronizarImagen) {
                        tiendanubeService.sincronizarImagen(
                                canal.idExterno,
                                articulo
                        );
                    }

                    resultado.mensaje =
                            "Publicado correctamente";

                } else {

                    try {

                        tiendanubeService.actualizar(
                                articulo,
                                existente
                        );

                        if (articulo.sincronizarImagen) {

                            tiendanubeService.sincronizarImagen(
                                    existente.idExterno,
                                    articulo
                            );
                        }

                        repository.guardar(
                                existente
                        );

                        resultado.mensaje =
                                "Actualizado correctamente";

                    } catch (TiendanubeNotFoundException ex) {

                        TiendanubeProductoDTO producto =
                                tiendanubeService.publicar(
                                        articulo
                                );

                        if (
                                producto == null
                                        || producto.id == null
                                        || producto.variants == null
                                        || producto.variants.isEmpty()
                                        || producto.variants.get(0).id == null
                        ) {

                            throw new RuntimeException(
                                    "Tiendanube no devolvió los IDs del producto recreado"
                            );
                        }

                        existente.idExterno =
                                String.valueOf(
                                        producto.id
                                );

                        existente.idVarianteExterna =
                                String.valueOf(
                                        producto.variants
                                                .get(0)
                                                .id
                                );

                        existente.publicado =
                                true;

                        repository.guardar(
                                existente
                        );

                        if (articulo.sincronizarImagen) {

                            tiendanubeService.sincronizarImagen(
                                    existente.idExterno,
                                    articulo
                            );
                        }

                        resultado.mensaje =
                                "Producto recreado correctamente";
                    }

                    repository.guardar(
                            existente
                    );

                    resultado.mensaje =
                            "Actualizado correctamente";
                }

                resultado.correcto = true;

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