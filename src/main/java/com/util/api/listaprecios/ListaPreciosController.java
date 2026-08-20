package com.util.api.listaprecios;

import com.util.api.generico.ResInsertUpdate;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/listas-precios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListaPreciosController {

    @Inject
    ListaPreciosService service;

    @GET
    public Response obtenerCompletas() {
        try {
            List<ListaPreciosDTO.Response> resultado =
                    service.obtenerCompletas();

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo listas de precios",
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/siguiente-codigo")
    public Response nroNuevaLista() {
        try {
            int siguienteCodigo =
                    service.nroNuevaLista();

            return Response
                    .ok(siguienteCodigo)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo el siguiente código "
                            + "de lista de precios",
                    ex
            );

            return Response
                    .serverError()
                    .entity(-1)
                    .build();
        }
    }

    @GET
    @Path("/{codigo}")
    public Response findById(
            @PathParam("codigo")
            int codigo
    ) {
        try {
            ListaPreciosDTO.Response resultado =
                    service.findById(codigo);

            if (resultado == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error buscando lista de precios "
                            + codigo,
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @POST
    public Response insert(
            ListaPreciosDTO.CrearRequest request
    ) {
        try {
            ResInsertUpdate resultado =
                    service.insert(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error insertando lista de precios",
                    ex
            );

            return Response
                    .serverError()
                    .entity(resultadoError())
                    .build();
        }
    }

    @PUT
    @Path("/{codigo}")
    public Response update(
            @PathParam("codigo")
            int codigo,

            ListaPreciosDTO.ActualizarRequest request
    ) {
        try {
            ResInsertUpdate resultado =
                    service.update(
                            codigo,
                            request
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error actualizando lista de precios "
                            + codigo,
                    ex
            );

            return Response
                    .serverError()
                    .entity(resultadoError())
                    .build();
        }
    }

    private Response respuestaJsonNull() {
        return Response
                .ok("null")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private ResInsertUpdate resultadoError() {
        return new ResInsertUpdate(
                "-1",
                false
        );
    }

    private void registrarError(
            String mensaje,
            Exception ex
    ) {
        System.err.println(mensaje);
        ex.printStackTrace();
    }
}