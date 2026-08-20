package com.util.api.configuracion;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("/api/configuraciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfiguracionController {

    @Inject
    ConfiguracionService service;

    @GET
    public Response obtenerTodas() {
        try {
            List<ConfiguracionDTO> configuraciones =
                    service.obtenerTodas();

            return Response.ok(
                    configuraciones
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("/{clave}")
    public Response obtener(
            @PathParam("clave") String clave
    ) {
        try {
            ConfiguracionDTO configuracion =
                    service.obtener(clave);

            /*
             * No se devuelve 404 porque una clave inexistente
             * es una búsqueda normal y ApiClient.get puede
             * convertir el 404 en una excepción.
             */
            return Response.ok(
                    configuracion
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .build();
        }
    }

    @PUT
    @Path("/{clave}")
    public Response guardar(
            @PathParam("clave") String clave,
            ConfiguracionDTO request
    ) {
        try {
            boolean resultado =
                    service.guardar(
                            clave,
                            request
                    );

            return Response.ok(
                    resultado
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    @PUT
    public Response guardarTodas(
            Map<String, String> configuraciones
    ) {
        try {
            boolean resultado =
                    service.guardarTodas(
                            configuraciones
                    );

            return Response.ok(
                    resultado
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    @DELETE
    @Path("/{clave}")
    public Response eliminar(
            @PathParam("clave") String clave
    ) {
        try {
            boolean resultado =
                    service.eliminar(clave);

            return Response.ok(
                    resultado
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }
}