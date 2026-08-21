package com.util.api.tiendanube;

import com.util.api.articulocanal.ArticuloCanalDTO;
import com.util.api.articulocanal.ArticuloSincronizacionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TiendanubeService {

    @Inject
    TiendanubeClient client;

    public TiendanubeProductoDTO publicar(
            ArticuloSincronizacionDTO articulo
    ) {

        TiendanubeProductoRequest request =
                new TiendanubeProductoRequest();

        request.name = Map.of(
                "es",
                articulo.descripcion
        );

        request.published = true;

        TiendanubeProductoRequest.Variante variante =
                new TiendanubeProductoRequest.Variante();

        variante.sku =
                articulo.codigoArticulo
                        + "-"
                        + articulo.codigoProveedor;

        variante.price =
                BigDecimal.valueOf(articulo.precio)
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();

        variante.stock = articulo.stock;

        request.variants = List.of(variante);

        return client.crearProducto(request);
    }

    public void actualizar(
            ArticuloSincronizacionDTO articulo,
            ArticuloCanalDTO canal
    ) {

        TiendanubeProductoRequest.Variante variante =
                new TiendanubeProductoRequest.Variante();

        variante.sku =
                articulo.codigoArticulo
                        + "-"
                        + articulo.codigoProveedor;

        variante.price =
                BigDecimal.valueOf(articulo.precio)
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();

        variante.stock =
                articulo.stock;

        System.out.println(
                "Actualizando Tiendanube: productId="
                        + canal.idExterno
                        + " variantId="
                        + canal.idVarianteExterna
        );

        client.actualizarVariante(
                canal.idExterno,
                canal.idVarianteExterna,
                variante
        );
    }

    public void subirImagen(
            String productId,
            ArticuloSincronizacionDTO articulo
    ) {

        if (
                articulo.imagenBase64 == null
                        || articulo.imagenBase64.isBlank()
        ) {
            return;
        }

        TiendanubeImagenRequest imagen =
                new TiendanubeImagenRequest();

        imagen.filename =
                articulo.nombreImagen;

        imagen.position =
                1;

        imagen.attachment =
                articulo.imagenBase64;

        client.subirImagen(
                productId,
                imagen
        );
    }

    public void sincronizarImagen(
            String productId,
            ArticuloSincronizacionDTO articulo
    ) {

        if (!articulo.sincronizarImagen) {
            return;
        }

        if (
                articulo.imagenBase64 == null
                        || articulo.imagenBase64.isBlank()
        ) {
            return;
        }

        List<TiendanubeImagenDTO> imagenes =
                client.obtenerImagenes(
                        productId
                );

        if (
                imagenes != null
                        && !imagenes.isEmpty()
        ) {

            TiendanubeImagenDTO principal =
                    imagenes.stream()
                            .filter(
                                    img ->
                                            img.position != null
                                                    && img.position == 1
                            )
                            .findFirst()
                            .orElse(
                                    imagenes.get(0)
                            );

            client.eliminarImagen(
                    productId,
                    String.valueOf(
                            principal.id
                    )
            );
        }

        TiendanubeImagenRequest imagen =
                new TiendanubeImagenRequest();

        imagen.filename =
                articulo.nombreImagen;

        imagen.position =
                1;

        imagen.attachment =
                articulo.imagenBase64;

        client.subirImagen(
                productId,
                imagen
        );
    }
}