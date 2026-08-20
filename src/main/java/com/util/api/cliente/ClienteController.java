package com.util.api.cliente;

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

import java.util.List;

@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteController {

    @Inject
    ClienteService service;

    @GET
    public Response listar(
            @QueryParam("habilitado")
            Boolean habilitado,

            @QueryParam("excluirGenericos")
            boolean excluirGenericos
    ) {
        try {
            List<ClienteDTO.Response> clientes =
                    service.listar(
                            habilitado,
                            excluirGenericos
                    );

            return Response.ok(clientes).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(List.of())
                    .build();
        }
    }

    @GET
    @Path("/proximo-codigo")
    public Response obtenerProximoCodigo() {
        try {
            return Response.ok(
                    service.obtenerProximoCodigoEstimado()
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            new ClienteDTO.CodigoResponse(-1)
                    )
                    .build();
        }
    }

    @GET
    @Path("/{codigo}/saldo")
    public Response obtenerSaldo(
            @PathParam("codigo") int codigo
    ) {
        try {
            return Response.ok(
                    service.obtenerSaldo(codigo)
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            new ClienteDTO.SaldoResponse(
                                    codigo,
                                    0
                            )
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
            ClienteDTO.Response cliente =
                    service.findById(codigo);

            /*
             * Se evita responder 404 porque ApiClient.get()
             * puede transformar una búsqueda normal sin resultado
             * en una excepción.
             */
            return Response.ok(cliente).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .build();
        }
    }

    @POST
    public Response insert(
            ClienteDTO.CrearRequest request
    ) {
        try {
            ClienteDTO.OperacionResponse resultado =
                    service.insert(request);

            return Response.ok(resultado).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            new ClienteDTO.OperacionResponse(
                                    false,
                                    "-1"
                            )
                    )
                    .build();
        }
    }

    @PUT
    @Path("/{codigo}")
    public Response update(
            @PathParam("codigo") int codigo,
            ClienteDTO.ActualizarRequest request
    ) {
        try {
            ClienteDTO.OperacionResponse resultado =
                    service.update(
                            codigo,
                            request
                    );

            return Response.ok(resultado).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            new ClienteDTO.OperacionResponse(
                                    false,
                                    "-1"
                            )
                    )
                    .build();
        }
    }
}