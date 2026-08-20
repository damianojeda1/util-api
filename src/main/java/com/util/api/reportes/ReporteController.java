package com.util.api.reportes;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;

@Path("/api/reportes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReporteController {

    @Inject
    ReporteService service;

    @GET
    @Path("/comprobante/{codigoTicket}")
    public Response obtenerComprobante(
            @PathParam("codigoTicket")
            int codigoTicket
    ) {
        try {
            return Response.ok(
                    service.obtenerComprobante(
                            codigoTicket
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo comprobante")
                    .build();
        }
    }

    @GET
    @Path("/presupuesto/{codigoPresupuesto}")
    public Response obtenerPresupuesto(
            @PathParam("codigoPresupuesto")
            int codigoPresupuesto
    ) {
        try {
            return Response.ok(
                    service.obtenerPresupuesto(
                            codigoPresupuesto
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo presupuesto")
                    .build();
        }
    }

    @GET
    @Path("/recibo/{codigoRecibo}")
    public Response obtenerRecibo(
            @PathParam("codigoRecibo")
            int codigoRecibo
    ) {
        try {
            return Response.ok(
                    service.obtenerRecibo(
                            codigoRecibo
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo recibo")
                    .build();
        }
    }


// -------------------------------------------------------------------------
// CLIENTES CON DEUDA
// -------------------------------------------------------------------------

    @GET
    @Path("/clientes-con-deuda")
    public Response listarClientesConDeuda(
            @QueryParam("hasta") String hasta
    ) {

        if (vacia(hasta)) {
            return Response.ok(
                    Collections.emptyList()
            ).build();
        }

        try {
            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            return Response.ok(
                    service.listarClientesConDeuda(
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // PRODUCTOS SIN MOVIMIENTO
    // -------------------------------------------------------------------------

    @GET
    @Path("/productos-sin-movimiento")
    public Response listarProductosSinMovimiento(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (rangoVacio(desde, hasta)) {
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
                    service.listarProductosSinMovimiento(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // VENTAS POR PRODUCTO
    // -------------------------------------------------------------------------

    @GET
    @Path("/ventas-por-producto")
    public Response listarVentasPorProducto(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (rangoVacio(desde, hasta)) {
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
                    service.listarVentasPorProducto(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // PRODUCTOS RENTABLES
    // -------------------------------------------------------------------------

    @GET
    @Path("/productos-rentables")
    public Response listarProductosRentables(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (rangoVacio(desde, hasta)) {
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
                    service.listarProductosRentables(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // EVOLUCIÓN DE VENTAS
    // -------------------------------------------------------------------------

    @GET
    @Path("/evolucion-ventas")
    public Response obtenerEvolucionVentas(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (rangoVacio(desde, hasta)) {
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
                    service.obtenerEvolucionVentas(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // AUXILIARES
    // -------------------------------------------------------------------------

    private boolean rangoVacio(
            String desde,
            String hasta
    ) {

        return vacia(desde) || vacia(hasta);
    }

    private boolean vacia(String valor) {
        return valor == null || valor.isBlank();
    }

    private Response fechaInvalida() {

        return Response.status(
                Response.Status.BAD_REQUEST
        ).entity(
                "Las fechas deben tener formato yyyy-MM-dd"
        ).build();
    }
}