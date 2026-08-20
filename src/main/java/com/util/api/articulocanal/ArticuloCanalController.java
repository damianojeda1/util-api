package com.util.api.articulocanal;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/articulos-canales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArticuloCanalController {

    @Inject
    ArticuloCanalService service;

    @GET
    public Response obtener(
            @QueryParam("codigoArticulo")
            String codigoArticulo,

            @QueryParam("codigoProveedor")
            String codigoProveedor,

            @QueryParam("canal")
            String canal
    ) {
        try {

            ArticuloCanalDTO dto =
                    service.obtener(
                            codigoArticulo,
                            codigoProveedor,
                            canal
                    );

            if (dto == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .build();
            }

            return Response.ok(dto).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo canal del artículo"
                    )
                    .build();
        }
    }

    @GET
    @Path("/canal")
    public Response obtenerPorCanal(
            @QueryParam("canal")
            String canal
    ) {

        try {

            return Response.ok(
                    service.obtenerPorCanal(canal)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo artículos del canal"
                    )
                    .build();
        }
    }

    @POST
    @Path("/sincronizar")
    public Response sincronizar(
            ArticuloSincronizacionRequest request
    ) {

        try {

            if (
                    request == null
                            || request.articulos == null
                            || request.articulos.isEmpty()
            ) {

                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(
                                "No se enviaron artículos para sincronizar."
                        )
                        .build();
            }

            return Response.ok(
                    service.sincronizar(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error sincronizando artículos."
                    )
                    .build();
        }
    }

    @POST
    public Response guardar(
            ArticuloCanalDTO dto
    ) {
        try {

            if (dto == null) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Datos obligatorios")
                        .build();
            }

            boolean resultado =
                    service.guardar(dto);

            return Response.ok(resultado).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error guardando canal del artículo"
                    )
                    .build();
        }
    }

    @DELETE
    public Response eliminar(
            @QueryParam("codigoArticulo")
            String codigoArticulo,

            @QueryParam("codigoProveedor")
            String codigoProveedor,

            @QueryParam("canal")
            String canal
    ) {
        try {

            boolean resultado =
                    service.eliminar(
                            codigoArticulo,
                            codigoProveedor,
                            canal
                    );

            return Response.ok(resultado).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error eliminando canal del artículo"
                    )
                    .build();
        }
    }
}