package com.util.api.impuestos;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/impuestos")
@Produces(MediaType.APPLICATION_JSON)
public class ImpuestoController {

    @Inject
    ImpuestoService service;

    @GET
    public Response obtenerTodos() {

        try {

            return Response.ok(
                    service.obtenerTodos()
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo impuestos"
                    )
                    .build();
        }
    }
}