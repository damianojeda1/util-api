package com.util.api.tipocomprobante;

import com.util.api.ticket.TicketDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/tipos-comprobante")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoComprobanteController {

    @Inject
    TipoComprobanteService service;

    @GET
    @Path("/{codigo}")
    public Response obtenerPorCodigo(
            @PathParam("codigo") String codigo
    ) {

        if (codigo == null || codigo.isBlank()) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            TicketDTO.TipoComprobanteDTO resultado =
                    service.obtenerPorCodigo(codigo);

            /*
             * No devolver 404 porque ApiClient.get(...)
             * puede lanzar una excepción ante una búsqueda normal
             * sin resultados.
             */
            return Response.ok(resultado).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }
}