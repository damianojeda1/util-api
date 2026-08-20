package com.util.api.categoriaarticulo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/categorias-articulo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaArticuloController {

    @Inject
    CategoriaArticuloService service;

    @GET
    public Response obtenerCompletos() {
        try {
            List<CategoriaArticuloDTO> categorias =
                    service.obtenerCompletos();

            return Response.ok(
                    categorias
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo categorías de artículos"
                    )
                    .build();
        }
    }

    @GET
    @Path("/{codigo}")
    public Response findById(
            @PathParam("codigo") int codigo
    ) {
        try {
            CategoriaArticuloDTO categoria =
                    service.findById(codigo);

            /*
             * No devolvemos 404.
             *
             * Una respuesta 200 sin entidad permite que el
             * Desktop obtenga null sin que ApiClient lance
             * una excepción HTTP.
             */
            return Response.ok(
                    categoria
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error buscando categoría de artículo"
                    )
                    .build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscarPorDescripcion(
            @QueryParam("nombre") String nombre
    ) {
        try {
            CategoriaArticuloDTO categoria =
                    service.buscarPorDescripcion(
                            nombre
                    );

            return Response.ok(
                    categoria
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error buscando categoría por descripción"
                    )
                    .build();
        }
    }

    @POST
    public Response insert(
            CategoriaArticuloDTO categoria
    ) {
        try {
            boolean resultado =
                    service.insert(categoria);

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
    @Path("/{codigo}")
    public Response update(
            @PathParam("codigo") int codigo,
            CategoriaArticuloDTO categoria
    ) {
        try {
            boolean resultado =
                    service.update(
                            codigo,
                            categoria
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
    @Path("/{codigo}")
    public Response delete(
            @PathParam("codigo") int codigo
    ) {
        try {
            boolean resultado =
                    service.delete(codigo);

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