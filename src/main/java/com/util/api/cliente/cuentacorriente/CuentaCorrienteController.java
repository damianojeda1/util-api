package com.util.api.cliente.cuentacorriente;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/clientes/cuenta-corriente")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuentaCorrienteController {

    @Inject
    CuentaCorrienteService service;

    @GET
    public Response obtenerResumen(
            @QueryParam("cliente") int cliente,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {
        try {
            CuentaCorrienteDTO.ResumenDTO resultado =
                    service.obtenerResumen(
                            cliente,
                            desde,
                            hasta
                    );

            return Response.ok(
                    resultado
            ).build();

        } catch (Exception e) {
            e.printStackTrace();

            return Response.serverError()
                    .entity(
                            new CuentaCorrienteDTO.ResumenDTO()
                    )
                    .build();
        }
    }
}