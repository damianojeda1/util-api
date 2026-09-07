package com.util.api.tiendanube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.util.api.canalconfiguracion.CanalConfiguracionDTO;
import com.util.api.canalconfiguracion.CanalConfiguracionService;
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
    CanalConfiguracionService canalConfiguracionService;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private CanalConfiguracionDTO obtenerConfiguracion() {

        try {

            CanalConfiguracionDTO configuracion =
                    canalConfiguracionService.obtener(
                            "TIENDANUBE"
                    );

            if (configuracion == null) {
                throw new RuntimeException(
                        "Tiendanube no está vinculada"
                );
            }

            if (!configuracion.activo) {
                throw new RuntimeException(
                        "La vinculación con Tiendanube está deshabilitada"
                );
            }

            if (
                    configuracion.storeId == null
                            || configuracion.storeId.isBlank()
            ) {
                throw new RuntimeException(
                        "Tiendanube no tiene Store ID configurado"
                );
            }

            if (
                    configuracion.accessToken == null
                            || configuracion.accessToken.isBlank()
            ) {
                throw new RuntimeException(
                        "Tiendanube no tiene Access Token configurado"
                );
            }

            return configuracion;

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error obteniendo configuración de Tiendanube",
                    ex
            );
        }
    }

    public TiendanubeProductoDTO crearProducto(
            TiendanubeProductoRequest producto
    ) {

        try {

            CanalConfiguracionDTO configuracion =
                    obtenerConfiguracion();

            String url =
                    config.apiUrl()
                            + "/"
                            + configuracion.storeId
                            + "/products";

            String json =
                    objectMapper.writeValueAsString(
                            producto
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(url)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + configuracion.accessToken
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
            CanalConfiguracionDTO configuracion =
                    obtenerConfiguracion();

            String url =
                    config.apiUrl()
                            + "/"
                            + configuracion.storeId
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
                                    "Bearer " + configuracion.accessToken
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
            CanalConfiguracionDTO configuracion =
                    obtenerConfiguracion();

            String url =
                    config.apiUrl()
                            + "/"
                            + configuracion.storeId
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
                                    "Bearer " + configuracion.accessToken
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
            CanalConfiguracionDTO configuracion =
                    obtenerConfiguracion();

            String url =
                    config.apiUrl()
                            + "/"
                            + configuracion.storeId
                            + "/products/"
                            + productId
                            + "/images";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Authorization",
                                    "Bearer " + configuracion.accessToken
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
            CanalConfiguracionDTO configuracion =
                    obtenerConfiguracion();

            String url =
                    config.apiUrl()
                            + "/"
                            + configuracion.storeId
                            + "/products/"
                            + productId
                            + "/images/"
                            + imageId;

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Authorization",
                                    "Bearer " + configuracion.accessToken
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