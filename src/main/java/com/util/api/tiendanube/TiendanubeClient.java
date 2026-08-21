package com.util.api.tiendanube;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@ApplicationScoped
public class TiendanubeClient {

    @Inject
    TiendanubeConfig config;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TiendanubeProductoDTO crearProducto(
            TiendanubeProductoRequest producto
    ) {

        try {

            String url =
                    config.apiUrl()
                            + "/"
                            + config.storeId()
                            + "/products";

            String json = objectMapper.writeValueAsString(producto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(
                            "Authorization",
                            "Bearer " + config.token()
                    )
                    .header(
                            "User-Agent",
                            "Util Gestion (utilgestion)"
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(json)
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Error Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    TiendanubeProductoDTO.class
            );

        } catch (RuntimeException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Error creando producto en Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    public TiendanubeProductoDTO.Variante actualizarVariante(
            String productId,
            String variantId,
            TiendanubeProductoRequest.Variante variante
    ) {

        try {

            String url =
                    config.apiUrl()
                            + "/"
                            + config.storeId()
                            + "/products/"
                            + productId
                            + "/variants/"
                            + variantId;

            String json =
                    objectMapper.writeValueAsString(variante);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Authorization",
                                    "Bearer " + config.token()
                            )
                            .header(
                                    "User-Agent",
                                    "Util Gestion (utilgestion)"
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .PUT(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() == 404) {

                throw new TiendanubeNotFoundException(
                        "El producto ya no existe en Tiendanube"
                );
            }

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new RuntimeException(
                        "Error Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    TiendanubeProductoDTO.Variante.class
            );

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error actualizando variante en Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    public TiendanubeImagenDTO subirImagen(
            String productId,
            TiendanubeImagenRequest imagen
    ) {

        try {

            String url =
                    config.apiUrl()
                            + "/"
                            + config.storeId()
                            + "/products/"
                            + productId
                            + "/images";

            String json =
                    objectMapper.writeValueAsString(
                            imagen
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(url)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + config.token()
                            )
                            .header(
                                    "User-Agent",
                                    "Util Gestion (utilgestion)"
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new RuntimeException(
                        "Error Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    TiendanubeImagenDTO.class
            );

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error subiendo imagen a Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    public List<TiendanubeImagenDTO> obtenerImagenes(
            String productId
    ) {

        try {

            String url =
                    config.apiUrl()
                            + "/"
                            + config.storeId()
                            + "/products/"
                            + productId
                            + "/images";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Authorization",
                                    "Bearer " + config.token()
                            )
                            .header(
                                    "User-Agent",
                                    "Util Gestion (utilgestion)"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new RuntimeException(
                        "Error Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    TiendanubeImagenDTO.class
                            )
            );

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error obteniendo imágenes de Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    public void eliminarImagen(
            String productId,
            String imageId
    ) {

        try {

            String url =
                    config.apiUrl()
                            + "/"
                            + config.storeId()
                            + "/products/"
                            + productId
                            + "/images/"
                            + imageId;

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Authorization",
                                    "Bearer " + config.token()
                            )
                            .header(
                                    "User-Agent",
                                    "Util Gestion (utilgestion)"
                            )
                            .DELETE()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new RuntimeException(
                        "Error Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error eliminando imagen de Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }
}