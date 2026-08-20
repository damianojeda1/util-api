package com.util.api.presupuesto;

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

@Path("/api/presupuestos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PresupuestoController {

    @Inject
    PresupuestoService service;

    // -------------------------------------------------------------------------
    // GUARDAR
    // -------------------------------------------------------------------------

    @POST
    public Response guardarPresupuesto(
            TicketDTO.GuardarPresupuestoRequest request
    ) {

        if (request == null) {
            return Response.ok(
                    TicketDTO.GuardarPresupuestoResponse.error(
                            "Los datos del presupuesto son obligatorios"
                    )
            ).build();
        }

        try {
            return Response.ok(
                    service.guardarPresupuesto(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            TicketDTO.GuardarPresupuestoResponse.error(
                                    "Error inesperado guardando presupuesto"
                            )
                    )
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // LISTAR
    // -------------------------------------------------------------------------

    @GET
    public Response listarPresupuestos(
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
                    service.listarPresupuestos(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo:\\d+}")
    public Response obtenerPresupuesto(
            @PathParam("codigo") int codigoPresupuesto
    ) {

        if (codigoPresupuesto <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            return Response.ok(
                    service.obtenerPresupuesto(
                            codigoPresupuesto
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER CLIENTE
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo:\\d+}/cliente")
    public Response obtenerCliente(
            @PathParam("codigo") int codigoPresupuesto
    ) {

        if (codigoPresupuesto <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            return Response.ok(
                    service.obtenerCliente(
                            codigoPresupuesto
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // ELIMINAR
    // -------------------------------------------------------------------------

    @DELETE
    @Path("/{codigo:\\d+}")
    public Response eliminarPresupuesto(
            @PathParam("codigo") int codigoPresupuesto
    ) {

        if (codigoPresupuesto <= 0) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.eliminarPresupuesto(
                            codigoPresupuesto
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private boolean vacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }

    private Response fechaInvalida() {

        return Response.status(
                Response.Status.BAD_REQUEST
        ).entity(
                "Las fechas deben tener formato yyyy-MM-dd"
        ).build();
    }
}