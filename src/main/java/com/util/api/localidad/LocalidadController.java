package com.util.api.localidad;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/localidades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocalidadController {

    @Inject
    LocalidadService service;

    @GET
    public Response obtenerCompleto(
            @QueryParam("codigoPostal")
            String codigoPostal
    ) {
        try {
            List<LocalidadDTO.Response> localidades =
                    service.obtenerCompleto(codigoPostal);

            return Response.ok(localidades).build();

        } catch (Exception e) {
            registrarError(
                    "Error obteniendo localidades",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new LocalidadDTO.OperacionResponse(
                                    false,
                                    0,
                                    "Error obteniendo localidades"
                            )
                    )
                    .build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscarCodPostalNombre(
            @QueryParam("codigoPostal")
            String codigoPostal,

            @QueryParam("nombre")
            String nombre
    ) {
        try {
            LocalidadDTO.Response localidad =
                    service.buscarCodPostalNombre(
                            codigoPostal,
                            nombre
                    );

            if (localidad == null) {
                return respuestaJsonNull();
            }

            return Response.ok(localidad).build();

        } catch (Exception e) {
            registrarError(
                    "Error buscando localidad por código postal y nombre",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new LocalidadDTO.OperacionResponse(
                                    false,
                                    0,
                                    "Error buscando la localidad"
                            )
                    )
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response buscarId(
            @PathParam("id")
            int id
    ) {
        try {
            LocalidadDTO.Response localidad =
                    service.buscarId(id);

            if (localidad == null) {
                return respuestaJsonNull();
            }

            return Response.ok(localidad).build();

        } catch (Exception e) {
            registrarError(
                    "Error buscando localidad con ID " + id,
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new LocalidadDTO.OperacionResponse(
                                    false,
                                    0,
                                    "Error buscando la localidad"
                            )
                    )
                    .build();
        }
    }

    @POST
    public Response insertar(
            LocalidadDTO.CrearRequest request
    ) {
        try {
            LocalidadDTO.OperacionResponse resultado =
                    service.insertar(request);

            return Response.ok(resultado).build();

        } catch (Exception e) {
            registrarError(
                    "Error insertando localidad",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new LocalidadDTO.OperacionResponse(
                                    false,
                                    0,
                                    "Error insertando la localidad"
                            )
                    )
                    .build();
        }
    }

    private Response respuestaJsonNull() {
        return Response
                .ok("null")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private void registrarError(
            String mensaje,
            Exception e
    ) {
        System.err.println(mensaje);
        e.printStackTrace();
    }
}