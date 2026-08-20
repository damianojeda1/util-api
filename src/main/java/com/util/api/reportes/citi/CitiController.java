package com.util.api.reportes.citi;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;

@Path("/api/reportes/citi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CitiController {

    @Inject
    CitiService service;

    @GET
    @Path("/ventas")
    public Response obtenerVentas(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (desde == null
                || desde.isBlank()
                || hasta == null
                || hasta.isBlank()) {

            return Response.ok(
                    Collections.emptyList()
            ).build();
        }

        try {
            LocalDate fechaDesde =
                    LocalDate.parse(desde);

            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            return Response.ok(
                    service.obtenerVentas(
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

            return Response.serverError().build();
        }
    }

    @GET
    @Path("/compras")
    public Response obtenerCompras(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (desde == null
                || desde.isBlank()
                || hasta == null
                || hasta.isBlank()) {

            return Response.ok(
                    Collections.emptyList()
            ).build();
        }

        try {
            LocalDate fechaDesde =
                    LocalDate.parse(desde);

            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            return Response.ok(
                    service.obtenerCompras(
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

            return Response.serverError().build();
        }
    }
}