package com.util.api.condicioniva;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/condiciones-iva")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CondicionIVAController {

    @Inject
    CondicionIVAService service;

    @GET
    public Response obtenerHabilitadas() {
        try {
            List<CondicionIVADTO> condiciones =
                    service.obtenerHabilitadas();

            return Response.ok(
                    condiciones
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo condiciones de IVA"
                    )
                    .build();
        }
    }
}