package com.util.api.mediopago;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;

@Path("/api/medios-pago")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MedioPagoController {

    @Inject
    MedioPagoService service;

    // -------------------------------------------------------------------------
    // LISTADO
    // -------------------------------------------------------------------------

    @GET
    public Response listar(
            @QueryParam("incluirInactivos")
            boolean incluirInactivos
    ) {

        try {
            return Response.ok(
                    service.listar(
                            incluirInactivos
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER POR ID
    // -------------------------------------------------------------------------

    @GET
    @Path("/{id}")
    public Response findById(
            @PathParam("id") int id
    ) {

        if (id <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            return Response.ok(
                    service.findById(id)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER POR CÓDIGO ENUM
    // -------------------------------------------------------------------------

    @GET
    @Path("/codigo-enum/{codigoEnum}")
    public Response findPorCodigoEnum(
            @PathParam("codigoEnum")
            String codigoEnum
    ) {

        if (codigoEnum == null
                || codigoEnum.isBlank()) {

            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            return Response.ok(
                    service.findPorCodigoEnum(
                            codigoEnum
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // INSERTAR
    // -------------------------------------------------------------------------

    @POST
    public Response insertar(
            MedioPagoDTO medioPago
    ) {

        if (medioPago == null) {
            return Response.ok(-1).build();
        }

        try {
            return Response.ok(
                    service.insertar(medioPago)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(-1)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    @PUT
    @Path("/{id}")
    public Response actualizar(
            @PathParam("id") int id,
            MedioPagoDTO medioPago
    ) {

        if (id <= 0 || medioPago == null) {
            return Response.ok(-1).build();
        }

        try {
            return Response.ok(
                    service.actualizar(
                            id,
                            medioPago
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(-1)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // OCULTAR
    // -------------------------------------------------------------------------

    @PUT
    @Path("/{id}/ocultar")
    public Response ocultar(
            @PathParam("id") int id
    ) {

        if (id <= 0) {
            return Response.ok(-1).build();
        }

        try {
            return Response.ok(
                    service.ocultar(id)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(-1)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // MOSTRAR
    // -------------------------------------------------------------------------

    @PUT
    @Path("/{id}/mostrar")
    public Response mostrar(
            @PathParam("id") int id
    ) {

        if (id <= 0) {
            return Response.ok(-1).build();
        }

        try {
            return Response.ok(
                    service.mostrar(id)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(-1)
                    .build();
        }
    }
}