package com.util.api.tipoidentificacion;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

@Path("/api/tipos-identificacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoIdentificacionController {

    @Inject
    TipoIdentificacionService service;

    @GET
    public Response obtenerParaCombo() {
        try {
            List<TipoIdentificacionDTO> tipos =
                    service.obtenerParaCombo();

            return Response.ok(tipos).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }
}