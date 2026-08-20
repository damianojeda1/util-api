package com.util.api.actualizacion;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/actualizacion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActualizacionController {

    @Inject
    ActualizacionService service;

    @POST
    public Response actualizar(
            ActualizacionDTO request
    ) {

        try {

            int versionFinal =
                    service.actualizar(request);

            return Response.ok(
                    versionFinal
            ).build();

        } catch (IllegalArgumentException ex) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ex.getMessage())
                    .build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response
                    .serverError()
                    .entity(
                            "Error actualizando base de datos"
                    )
                    .build();
        }
    }
}