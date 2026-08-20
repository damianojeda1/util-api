package com.util.api.compcompra;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Path("/api/comprobantes-compra")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompCompraController {

    private static final String FORMATO_FECHA =
            "yyyy-MM-dd";

    @Inject
    CompCompraService service;

    @GET
    @Path("/existe")
    public Response existeComprobante(
            @QueryParam("codigoProveedor")
            int codigoProveedor,

            @QueryParam("letra")
            String letra,

            @QueryParam("centro")
            String centro,

            @QueryParam("numero")
            String numero
    ) {
        try {
            CompCompraDTO.ExisteResponse resultado =
                    service.existeComprobante(
                            codigoProveedor,
                            letra,
                            centro,
                            numero
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error verificando comprobante de compra",
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            new CompCompraDTO.ExisteResponse(false)
                    )
                    .build();
        }
    }

    @POST
    public Response insertarComprobante(
            CompCompraDTO.ComprobanteRequest request
    ) {
        try {
            CompCompraDTO.InsertResponse resultado =
                    service.insertarComprobante(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error insertando comprobante de compra",
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            insertError(
                                    "Error insertando el comprobante"
                            )
                    )
                    .build();
        }
    }

    @POST
    @Path("/{codigoComprobante}/pagos")
    public Response insertarPago(
            @PathParam("codigoComprobante")
            int codigoComprobante,

            CompCompraDTO.PagoRequest request
    ) {
        try {
            CompCompraDTO.InsertResponse resultado =
                    service.insertarPago(
                            codigoComprobante,
                            request
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error insertando pago de compra",
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            insertError(
                                    "Error insertando el pago"
                            )
                    )
                    .build();
        }
    }

    @GET
    @Path("/pagos/{codigoPago}")
    public Response obtenerPago(
            @PathParam("codigoPago")
            int codigoPago
    ) {
        try {
            CompCompraDTO.PagoDTO resultado =
                    service.obtenerPago(codigoPago);

            if (resultado == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo pago de compra "
                            + codigoPago,
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/cuenta-corriente")
    public Response obtenerCuentaCorriente(
            @QueryParam("codigoProveedor")
            int codigoProveedor,

            @QueryParam("desde")
            String desdeTexto,

            @QueryParam("hasta")
            String hastaTexto
    ) {
        try {
            Date desde =
                    parseFecha(desdeTexto);

            Date hasta =
                    parseFecha(hastaTexto);

            CompCompraDTO.CuentaCorrienteDTO resultado =
                    service.obtenerCuentaCorriente(
                            codigoProveedor,
                            desde,
                            hasta
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (ParseException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            new CompCompraDTO.CuentaCorrienteDTO()
                    )
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo cuenta corriente "
                            + "del proveedor "
                            + codigoProveedor,
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            new CompCompraDTO.CuentaCorrienteDTO()
                    )
                    .build();
        }
    }

    @GET
    @Path("/libro-iva")
    public Response listarLibroIva(
            @QueryParam("desde")
            String desdeTexto,

            @QueryParam("hasta")
            String hastaTexto
    ) {
        try {
            Date desde =
                    parseFecha(desdeTexto);

            Date hasta =
                    parseFecha(hastaTexto);

            List<CompCompraDTO.LibroIvaCompraDTO> resultado =
                    service.listarLibroIva(
                            desde,
                            hasta
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (ParseException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(List.of())
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo Libro IVA Compra",
                    ex
            );

            return Response
                    .serverError()
                    .entity(List.of())
                    .build();
        }
    }

    @GET
    @Path("/resumen-periodo")
    public Response obtenerResumenPeriodo(
            @QueryParam("desde")
            String desdeTexto,

            @QueryParam("hasta")
            String hastaTexto
    ) {
        try {
            Date desde =
                    parseFecha(desdeTexto);

            Date hasta =
                    parseFecha(hastaTexto);

            ResumenPeriodoDTO resultado =
                    service.obtenerResumenPeriodo(
                            desde,
                            hasta
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (ParseException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ResumenPeriodoDTO())
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo resumen de compras",
                    ex
            );

            return Response
                    .serverError()
                    .entity(new ResumenPeriodoDTO())
                    .build();
        }
    }

    @GET
    @Path("/{codigoComprobante}")
    public Response obtenerComprobante(
            @PathParam("codigoComprobante")
            int codigoComprobante
    ) {
        try {
            CompCompraDTO.ComprobanteDTO resultado =
                    service.obtenerComprobante(
                            codigoComprobante
                    );

            if (resultado == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo comprobante de compra "
                            + codigoComprobante,
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @DELETE
    @Path("/{codigoComprobante}")
    public Response anularComprobante(
            @PathParam("codigoComprobante")
            int codigoComprobante
    ) {
        try {
            CompCompraDTO.OperacionResponse resultado =
                    service.anularComprobante(
                            codigoComprobante
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error anulando comprobante de compra "
                            + codigoComprobante,
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            operacionError(
                                    "Error anulando el comprobante"
                            )
                    )
                    .build();
        }
    }

    @DELETE
    @Path("/pagos/{codigoPago}")
    public Response anularPago(
            @PathParam("codigoPago")
            int codigoPago,

            @QueryParam("codigoMovimientoCaja")
            int codigoMovimientoCaja
    ) {
        try {
            CompCompraDTO.OperacionResponse resultado =
                    service.anularPago(
                            codigoPago,
                            codigoMovimientoCaja
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error anulando pago de compra "
                            + codigoPago,
                    ex
            );

            return Response
                    .serverError()
                    .entity(
                            operacionError(
                                    "Error anulando el pago"
                            )
                    )
                    .build();
        }
    }

    private Date parseFecha(
            String valor
    ) throws ParseException {

        if (valor == null
                || valor.trim().isEmpty()) {

            throw new ParseException(
                    "Fecha vacía",
                    0
            );
        }

        SimpleDateFormat formato =
                new SimpleDateFormat(FORMATO_FECHA);

        formato.setLenient(false);

        return formato.parse(valor.trim());
    }

    private Response respuestaJsonNull() {
        return Response
                .ok("null")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private CompCompraDTO.InsertResponse insertError(
            String mensaje
    ) {
        return new CompCompraDTO.InsertResponse(
                -1,
                false,
                mensaje
        );
    }

    private CompCompraDTO.OperacionResponse operacionError(
            String mensaje
    ) {
        return new CompCompraDTO.OperacionResponse(
                false,
                mensaje
        );
    }

    private void registrarError(
            String mensaje,
            Exception ex
    ) {
        System.err.println(mensaje);
        ex.printStackTrace();
    }
}