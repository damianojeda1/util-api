package com.util.api.unidadventa;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/unidades-venta")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UnidadVentaController {

    @Inject
    UnidadVentaService service;

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
                            "Error obteniendo unidades de venta"
                    )
                    .build();
        }
    }

    @POST
    public Response insertar(
            UnidadVentaDTO.GuardarRequest request
    ) {

        try {

            int codigo =
                    service.insertar(request);

            return Response.ok(
                    codigo
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error insertando unidad de venta"
                    )
                    .build();
        }
    }
}