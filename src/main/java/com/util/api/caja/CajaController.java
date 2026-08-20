package com.util.api.caja;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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

@Path("/api/cajas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CajaController {

    @Inject
    CajaService service;

    @GET
    @Path("/ultimo-cierre")
    public Response obtenerUltimoCierre() {
        try {
            CajaDTO caja = service.obtenerUltimoCierre();

            if (caja == null) {
                /*
                 * Se devuelve 200 con un objeto vacío para evitar que
                 * ApiClient.get() transforme un 404 en excepción.
                 */
                return Response.ok(new CajaDTO()).build();
            }

            return Response.ok(caja).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo último cierre")
                    .build();
        }
    }

    @POST
    public Response abrirCaja(
            CajaDTO.AperturaRequest request
    ) {
        try {
            int codigo = service.abrirCaja(request);

            return Response.ok(codigo).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error abriendo caja")
                    .build();
        }
    }

    @GET
    public Response obtenerHistoricas() {
        try {
            List<CajaDTO> cajas =
                    service.obtenerHistoricas();

            return Response.ok(cajas).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo cajas históricas")
                    .build();
        }
    }

    @GET
    @Path("/{codigoCaja}")
    public Response obtenerDetalle(
            @PathParam("codigoCaja")
            int codigoCaja,

            @QueryParam("incluirPagosProveedor")
            @DefaultValue("false")
            boolean incluirPagosProveedor
    ) {
        try {
            CajaDTO caja = service.obtenerDetalle(
                    codigoCaja,
                    incluirPagosProveedor
            );

            if (caja == null) {
                return Response.status(
                                Response.Status.NOT_FOUND
                        )
                        .entity("Caja no encontrada")
                        .build();
            }

            return Response.ok(caja).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo detalle de caja")
                    .build();
        }
    }

    @GET
    @Path("/{codigoCaja}/impresion")
    public Response obtenerParaImpresion(
            @PathParam("codigoCaja")
            int codigoCaja
    ) {
        try {
            CajaDTO.ImpresionCajaDTO caja =
                    service.obtenerParaImpresion(codigoCaja);

            if (caja == null) {
                return Response.status(
                                Response.Status.NOT_FOUND
                        )
                        .entity("Caja no encontrada")
                        .build();
            }

            return Response.ok(caja).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo impresión de caja")
                    .build();
        }
    }

    @GET
    @Path("/movimientos/{codigoMovimiento}/pagos")
    public Response obtenerPagosMovimiento(
            @PathParam("codigoMovimiento")
            int codigoMovimiento
    ) {
        try {
            return Response.ok(
                    service.obtenerPagosMovimiento(
                            codigoMovimiento
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo pagos del movimiento")
                    .build();
        }
    }

    @POST
    @Path("/{codigoCaja}/movimientos")
    public Response insertarMovimiento(
            @PathParam("codigoCaja")
            int codigoCaja,

            CajaDTO.MovimientoRequest request
    ) {
        try {
            if (request == null) {
                return Response.status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity("Movimiento inválido")
                        .build();
            }

            request.codigoCaja = codigoCaja;

            return Response.ok(
                    service.insertarMovimiento(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error insertando movimiento")
                    .build();
        }
    }

    @PUT
    @Path("/{codigoCaja}/cierre")
    public Response cerrarCaja(
            @PathParam("codigoCaja")
            int codigoCaja,

            CajaDTO.CierreRequest request
    ) {
        try {
            if (request == null) {
                return Response.status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity("Cierre inválido")
                        .build();
            }

            request.codigoCaja = codigoCaja;

            return Response.ok(
                    service.cerrarCaja(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error cerrando caja")
                    .build();
        }
    }

    @GET
    @Path("/abiertas")
    public Response obtenerAbiertas() {
        try {
            return Response.ok(
                    service.obtenerAbiertas()
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo cajas abiertas")
                    .build();
        }
    }

    @POST
    @Path("/cerrar-abiertas-usuario")
    public Response cerrarAbiertasUsuario(
            CajaDTO.CerrarAbiertasUsuarioRequest request
    ) {
        try {
            CajaDTO.OperacionResponse response =
                    service.cerrarAbiertasUsuario(request);

            return Response.ok(response).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error cerrando cajas abiertas")
                    .build();
        }
    }
}