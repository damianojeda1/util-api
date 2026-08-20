package com.util.api.recibo;

import com.util.api.ticket.TicketDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;

@Path("/api/recibos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReciboController {

    @Inject
    ReciboService service;

    @POST
    public Response guardarRecibo(
            TicketDTO.GuardarReciboRequest request
    ) {

        if (request == null) {
            return Response.ok(
                    TicketDTO.GuardarReciboResponse.error(
                            "Los datos del recibo son obligatorios"
                    )
            ).build();
        }

        try {
            return Response.ok(
                    service.guardarRecibo(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            TicketDTO.GuardarReciboResponse.error(
                                    "Error inesperado guardando recibo"
                            )
                    )
                    .build();
        }
    }

    @GET
    public Response listarRecibos(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (vacio(desde) || vacio(hasta)) {
            return Response.ok(
                    Collections.emptyList()
            ).build();
        }

        try {
            LocalDate fechaDesde =
                    LocalDate.parse(desde);

            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            if (fechaDesde.isAfter(fechaHasta)) {
                return Response.ok(
                        Collections.emptyList()
                ).build();
            }

            return Response.ok(
                    service.listarRecibos(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(
                    "Las fechas deben tener formato yyyy-MM-dd"
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    @GET
    @Path("/{codigo:\\d+}")
    public Response obtenerRecibo(
            @PathParam("codigo") int codigoRecibo
    ) {

        if (codigoRecibo <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            return Response.ok(
                    service.obtenerRecibo(
                            codigoRecibo
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/{codigo:\\d+}")
    public Response anularRecibo(
            @PathParam("codigo") int codigoRecibo,
            @QueryParam("movimientoCaja")
            int codigoMovimientoCaja
    ) {

        if (codigoRecibo <= 0
                || codigoMovimientoCaja <= 0) {

            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.anularRecibo(
                            codigoRecibo,
                            codigoMovimientoCaja
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    private boolean vacio(
            String valor
    ) {

        return valor == null || valor.isBlank();
    }
}