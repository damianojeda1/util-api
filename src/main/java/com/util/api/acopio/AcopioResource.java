package com.util.api.acopio;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/acopios")
@Produces(MediaType.APPLICATION_JSON)
public class AcopioResource {

    @Inject
    AcopioRepository acopioRepository;

    @GET
    public Response listarPorCliente(
            @QueryParam("cliente")
            int codigoCliente
    ) {

        if (codigoCliente <= 1) {
            return Response
                    .ok(List.of())
                    .build();
        }

        try {

            List<AcopioDTO.ResumenDTO> resultado =
                    acopioRepository.listarPorCliente(
                            codigoCliente
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response
                    .serverError()
                    .entity(
                            "No se pudieron obtener "
                                    + "los acopios del cliente"
                    )
                    .build();
        }
    }

    @GET
    @Path("/{id}/detalle")
    public Response obtenerDetalle(
            @jakarta.ws.rs.PathParam("id")
            int idAcopio
    ) {

        if (idAcopio <= 0) {
            return Response
                    .ok(List.of())
                    .build();
        }

        try {

            List<AcopioDTO.DetalleDTO> resultado =
                    acopioRepository.obtenerDetalle(
                            idAcopio
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response
                    .serverError()
                    .entity(
                            "No se pudo obtener "
                                    + "el detalle del acopio"
                    )
                    .build();
        }
    }

    @POST
    @Path("/retiros")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response guardarRetiro(
            AcopioDTO.GuardarRetiroRequest request
    ) {

        try {

            AcopioDTO.GuardarRetiroResponse response =
                    acopioRepository.guardarRetiro(
                            request
                    );

            if (!response.ok) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(response)
                        .build();
            }

            return Response
                    .ok(response)
                    .build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response
                    .serverError()
                    .entity(
                            AcopioDTO.GuardarRetiroResponse.error(
                                    "No se pudo guardar el retiro"
                            )
                    )
                    .build();
        }
    }
}