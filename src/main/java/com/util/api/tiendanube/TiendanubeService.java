package com.util.api.tiendanube;

import com.util.api.articulocanal.ArticuloSincronizacionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TiendanubeService {

    @Inject
    TiendanubeClient client;

    public TiendanubeProductoDTO publicar(
            ArticuloSincronizacionDTO articulo
    ) {

        Map<String, Object> nombre =
                new HashMap<>();

        nombre.put(
                "es",
                articulo.descripcion
        );

        Map<String, Object> variante =
                new HashMap<>();

        variante.put(
                "price",
                articulo.precio
        );

        variante.put(
                "stock",
                articulo.stock
        );

        variante.put(
                "sku",
                generarSku(articulo)
        );

        Map<String, Object> producto =
                new HashMap<>();

        producto.put(
                "name",
                nombre
        );

        producto.put(
                "published",
                true
        );

        producto.put(
                "variants",
                List.of(variante)
        );

        return client.crearProducto(
                producto
        );
    }

    private String generarSku(
            ArticuloSincronizacionDTO articulo
    ) {

        return articulo.codigoArticulo
                + "-"
                + articulo.codigoProveedor;
    }
}