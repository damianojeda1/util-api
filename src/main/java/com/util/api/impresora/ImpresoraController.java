package com.util.api.impresora;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/impresoras")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImpresoraController {

    @Inject
    ImpresoraService service;

    @GET
    public Response listarPorTerminal(
            @QueryParam("terminal")
            String terminal
    ) {
        try {
            List<ImpresoraDTO.Response> impresoras =
                    service.listarPorTerminal(terminal);

            return Response
                    .ok(impresoras)
                    .build();

        } catch (Exception e) {
            registrarError(
                    "Error listando impresoras",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new ImpresoraDTO.OperacionResponse(
                                    false,
                                    "Error listando impresoras"
                            )
                    )
                    .build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscarTerminalReporte(
            @QueryParam("terminal")
            String terminal,

            @QueryParam("reporte")
            String reporte
    ) {
        try {
            ImpresoraDTO.Response impresora =
                    service.buscarTerminalReporte(
                            terminal,
                            reporte
                    );

            if (impresora == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(impresora)
                    .build();

        } catch (Exception e) {
            registrarError(
                    "Error buscando impresora para el reporte "
                            + reporte,
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new ImpresoraDTO.OperacionResponse(
                                    false,
                                    "Error buscando la impresora"
                            )
                    )
                    .build();
        }
    }

    @POST
    public Response insertar(
            ImpresoraDTO.CrearRequest request
    ) {
        try {
            ImpresoraDTO.OperacionResponse resultado =
                    service.insertar(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception e) {
            registrarError(
                    "Error insertando impresora",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new ImpresoraDTO.OperacionResponse(
                                    false,
                                    "Error insertando la impresora"
                            )
                    )
                    .build();
        }
    }

    @POST
    @Path("/guardar")
    public Response guardarOActualizar(
            ImpresoraDTO.CrearRequest request
    ) {
        try {
            ImpresoraDTO.OperacionResponse resultado =
                    service.guardarOActualizar(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception e) {
            registrarError(
                    "Error guardando impresora",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new ImpresoraDTO.OperacionResponse(
                                    false,
                                    "Error guardando la impresora"
                            )
                    )
                    .build();
        }
    }

    @POST
    @Path("/eliminar")
    public Response eliminar(
            ImpresoraDTO.EliminarRequest request
    ) {
        try {
            ImpresoraDTO.OperacionResponse resultado =
                    service.eliminar(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception e) {
            registrarError(
                    "Error eliminando impresora",
                    e
            );

            return Response
                    .serverError()
                    .entity(
                            new ImpresoraDTO.OperacionResponse(
                                    false,
                                    "Error eliminando la impresora"
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