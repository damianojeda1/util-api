package com.util.api.tiendanube;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TiendanubeVinculacionService {

    private final Map<String, String> states =
            new ConcurrentHashMap<>();

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    @Inject
    TiendanubeConfig config;

    @Inject
    ObjectMapper objectMapper;

    public String generarState(
            String tenant
    ) {

        String state =
                UUID.randomUUID()
                        .toString();

        states.put(
                state,
                tenant
        );

        return state;
    }

    public String consumirState(
            String state
    ) {

        if (state == null) {
            return null;
        }

        return states.remove(
                state
        );
    }

    public TiendanubeTokenDTO obtenerToken(
            String code
    ) {

        try {

            Map<String, String> body =
                    new HashMap<>();

            body.put(
                    "client_id",
                    config.appId()
            );

            body.put(
                    "client_secret",
                    config.clientSecret()
            );

            body.put(
                    "grant_type",
                    "authorization_code"
            );

            body.put(
                    "code",
                    code
            );

            String json =
                    objectMapper.writeValueAsString(
                            body
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://www.tiendanube.com/apps/authorize/token"
                                    )
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
                        "Error obteniendo token Tiendanube HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    TiendanubeTokenDTO.class
            );

        } catch (RuntimeException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Error obteniendo token de Tiendanube: "
                            + ex.getMessage(),
                    ex
            );
        }
    }
}