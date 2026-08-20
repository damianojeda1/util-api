package com.util.api.ticket;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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

@Path("/api/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketController {

    @Inject
    TicketService service;

    // -------------------------------------------------------------------------
    // GUARDAR TICKET
    // -------------------------------------------------------------------------

    @POST
    public Response guardarTicket(
            TicketDTO.GuardarTicketRequest request
    ) {

        if (request == null) {
            return Response.ok(
                    TicketDTO.GuardarTicketResponse.error(
                            "Los datos del ticket son obligatorios"
                    )
            ).build();
        }

        try {
            return Response.ok(
                    service.guardarTicket(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            TicketDTO.GuardarTicketResponse.error(
                                    "Error inesperado guardando ticket"
                            )
                    )
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // LISTAR TICKETS
    // -------------------------------------------------------------------------

    @GET
    public Response listarTickets(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("soloFacturados")
            boolean soloFacturados
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
                    service.listarTickets(
                            fechaDesde,
                            fechaHasta,
                            soloFacturados
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
    // DETALLE DEL TICKET
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo}")
    public Response obtenerDetalle(
            @PathParam("codigo") int codigoTicket
    ) {

        if (codigoTicket <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            TicketDTO.DetalleTicketDTO resultado =
                    service.obtenerDetalle(
                            codigoTicket
                    );

            return Response.ok(resultado).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // DATOS PARA FACTURACIÓN
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo}/facturacion")
    public Response obtenerParaFacturacion(
            @PathParam("codigo") int codigoTicket
    ) {

        if (codigoTicket <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            TicketDTO.TicketFacturacionDTO resultado =
                    service.obtenerParaFacturacion(
                            codigoTicket
                    );

            return Response.ok(resultado).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // GUARDAR COMPROBANTE FISCAL
    // -------------------------------------------------------------------------

    @POST
    @Path("/{codigo}/comprobante-fiscal")
    public Response guardarComprobanteFiscal(
            @PathParam("codigo") int codigoTicket,
            TicketDTO.GuardarComprobanteFiscalRequest request
    ) {

        if (codigoTicket <= 0 || request == null) {
            return Response.ok(false).build();
        }

        /*
         * El path es la fuente principal del código.
         * Evita inconsistencias si el body trae otro ticket.
         */
        request.ticket = codigoTicket;

        try {
            return Response.ok(
                    service.guardarComprobanteFiscal(
                            request
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
    // OBTENER COMPROBANTE FISCAL
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo}/comprobante-fiscal")
    public Response obtenerComprobanteFiscal(
            @PathParam("codigo") int codigoTicket
    ) {

        if (codigoTicket <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            TicketDTO.ComprobanteFiscalDTO resultado =
                    service.obtenerComprobanteFiscal(
                            codigoTicket
                    );

            /*
             * No devolver 404: ApiClient.get(...) puede
             * lanzar una excepción cuando no hay comprobante.
             */
            return Response.ok(resultado).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // COMPROBANTES ASOCIADOS AL CLIENTE
    // -------------------------------------------------------------------------

    @GET
    @Path("/comprobantes-cliente")
    public Response listarComprobantesCliente(
            @QueryParam("cliente") int codigoCliente,
            @QueryParam("cuit") String cuit
    ) {

        if (codigoCliente <= 0
                && (cuit == null || cuit.isBlank())) {

            return Response.ok(
                    Collections.emptyList()
            ).build();
        }

        try {
            return Response.ok(
                    service.listarComprobantesCliente(
                            codigoCliente,
                            cuit
                    )
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(Collections.emptyList())
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // LIBRO IVA
    // -------------------------------------------------------------------------

    @GET
    @Path("/libro-iva")
    public Response listarLibroIva(
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
                    service.listarLibroIva(
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
    // RESUMEN DEL PERÍODO
    // -------------------------------------------------------------------------

    @GET
    @Path("/resumen-periodo")
    public Response obtenerResumenPeriodo(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta
    ) {

        if (rangoVacio(desde, hasta)) {
            return Response.ok(
                    new ResumenPeriodoDTO()
            ).build();
        }

        try {
            LocalDate fechaDesde =
                    LocalDate.parse(desde);

            LocalDate fechaHasta =
                    LocalDate.parse(hasta);

            if (fechaDesde.isAfter(fechaHasta)) {
                return Response.ok(
                        new ResumenPeriodoDTO()
                ).build();
            }

            return Response.ok(
                    service.obtenerResumenPeriodo(
                            fechaDesde,
                            fechaHasta
                    )
            ).build();

        } catch (DateTimeParseException ex) {

            return fechaInvalida();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(new ResumenPeriodoDTO())
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private boolean rangoVacio(
            String desde,
            String hasta
    ) {

        return vacio(desde) || vacio(hasta);
    }

    private boolean vacio(
            String valor
    ) {

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