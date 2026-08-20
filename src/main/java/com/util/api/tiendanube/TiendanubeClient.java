package com.util.api.tiendanube;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class TiendanubeClient {

    @Inject
    TiendanubeConfig config;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    public TiendanubeProductoDTO crearProducto(
            Object body
    ) {

        try {

            String json =
                    objectMapper.writeValueAsString(body);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            config.apiUrl()
                                                    + "/"
                                                    + config.storeId()
                                                    + "/products"
                                    )
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + config.token()
                            )
                            .header(
                                    "User-Agent",
                                    "Util Gestion (soporte@utilgestion.com)"
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            json
                                    )
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
                        "Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    TiendanubeProductoDTO.class
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error creando producto en Tiendanube",
                    ex
            );
        }
    }
}