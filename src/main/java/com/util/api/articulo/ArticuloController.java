package com.util.api.articulo;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
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

@Path("/api/articulos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArticuloController {

    @Inject
    ArticuloService service;

    @GET
    public Response obtenerCompletos(
            @QueryParam("habilitados")
            @DefaultValue("0")
            int habilitados,

            @QueryParam("categoria")
            @DefaultValue("-1")
            int categoria,

            @QueryParam("proveedor")
            @DefaultValue("-1")
            int proveedor
    ) {
        try {
            List<ArticuloDTO> articulos =
                    service.obtenerCompletos(
                            habilitados,
                            categoria,
                            proveedor
                    );

            return Response.ok(articulos).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error obteniendo artículos")
                    .build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscarCodArticulo(
            @QueryParam("codigo")
            String codigo,

            @QueryParam("proveedor")
            @DefaultValue("")
            String proveedor
    ) {
        try {
            List<ArticuloDTO> articulos =
                    service.buscarCodArticulo(
                            codigo,
                            proveedor
                    );

            return Response.ok(articulos).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error buscando artículo")
                    .build();
        }
    }

    @GET
    @Path("/{codigo}/proveedor/{proveedor}")
    public Response buscarUnoPorCodigoYProveedor(
            @PathParam("codigo")
            String codigo,

            @PathParam("proveedor")
            int proveedor
    ) {
        try {
            ArticuloDTO articulo =
                    service.buscarUnoPorCodigoYProveedor(
                            codigo,
                            proveedor
                    );

            if (articulo == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Artículo no encontrado")
                        .build();
            }

            return Response.ok(articulo).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error buscando artículo")
                    .build();
        }
    }

    @GET
    @Path("/venta")
    public Response buscarParaVenta(
            @QueryParam("filtro")
            @DefaultValue("")
            String filtro,

            @QueryParam("usuario")
            int usuario,

            @QueryParam("limite")
            @DefaultValue("15")
            int limite
    ) {
        try {
            List<ArticuloDTO> articulos =
                    service.buscarParaVenta(
                            filtro,
                            usuario,
                            limite
                    );

            return Response.ok(articulos).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error buscando artículos para venta")
                    .build();
        }
    }

    @GET
    @Path("/venta/codigo/{codigo}")
    public Response buscarParaVentaPorCodigo(
            @PathParam("codigo")
            String codigo,

            @QueryParam("usuario")
            int usuario
    ) {
        try {
            List<ArticuloDTO> articulos =
                    service.buscarParaVentaPorCodigo(
                            codigo,
                            usuario
                    );

            return Response.ok(articulos).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error buscando artículos para venta por código")
                    .build();
        }
    }

    @POST
    public Response insert(
            ArticuloDTO articulo
    ) {
        try {
            return Response.ok(
                    service.insert(articulo)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error insertando artículo")
                    .build();
        }
    }

    @PUT
    @Path("/{codigo}/proveedor/{proveedor}")
    public Response update(
            @PathParam("codigo")
            String codigo,

            @PathParam("proveedor")
            int proveedor,

            ArticuloDTO articulo
    ) {
        try {
            return Response.ok(
                    service.update(
                            codigo,
                            proveedor,
                            articulo
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error actualizando artículo")
                    .build();
        }
    }

    @PUT
    @Path("/proveedor")
    public Response cambiarProveedor(
            ArticuloDTO.CambiarProveedorRequest request
    ) {
        try {
            return Response.ok(
                    service.cambiarProveedor(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error cambiando proveedor del artículo")
                    .build();
        }
    }

    @PUT
    @Path("/margen/proveedor")
    public Response establecerMargenPorProveedor(
            ArticuloDTO.EstablecerMargenRequest request
    ) {
        try {
            return Response.ok(
                    service.establecerMargenPorProveedor(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error estableciendo margen por proveedor")
                    .build();
        }
    }

    @PUT
    @Path("/costos/porcentaje")
    public Response actualizarCostosPorPorcentaje(
            ArticuloDTO.ActualizarCostosRequest request
    ) {
        try {
            return Response.ok(
                    service.actualizarCostosPorPorcentaje(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error actualizando costos por porcentaje")
                    .build();
        }
    }

    @PUT
    @Path("/codigo")
    public Response actualizarCodigo(
            ArticuloDTO.CambiarCodigoRequest request
    ) {
        try {
            return Response.ok(
                    service.actualizarCodigo(request)
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error actualizando código del artículo")
                    .build();
        }
    }

    @POST
    @Path("/actualizar-costos-proveedor")
    public Response actualizarCostosPorProveedor(
            ArticuloDTO.ActualizarCostosProveedorRequest request
    ) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La solicitud es obligatoria")
                    .build();
        }

        if (request.codigoProveedor <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El proveedor no es válido")
                    .build();
        }

        int actualizados =
                service.actualizarCostosPorProveedor(
                        request.codigoProveedor,
                        request.porcentaje
                );

        return Response.ok(actualizados).build();
    }

    @POST
    @Path("/margen/categoria")
    public Response establecerMargenPorCategoria(
            ArticuloDTO.EstablecerMargenCategoriaRequest request
    ) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La solicitud es obligatoria")
                    .build();
        }

        if (request.codigoCategoria <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La categoría no es válida")
                    .build();
        }

        if (!Double.isFinite(request.margen)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El margen no es válido")
                    .build();
        }

        int actualizados =
                service.establecerMargenPorCategoria(
                        request.codigoCategoria,
                        request.margen
                );

        return Response.ok(actualizados).build();
    }

    @PUT
    @Path("/excel")
    public Response updateFromExcel(
            @QueryParam("actualizaStock")
            @DefaultValue("false")
            boolean actualizaStock,

            @QueryParam("actualizaMargen")
            @DefaultValue("false")
            boolean actualizaMargen,

            ArticuloDTO articulo
    ) {
        try {
            return Response.ok(
                    service.updateFromExcel(
                            articulo,
                            actualizaStock,
                            actualizaMargen
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error actualizando artículo desde Excel")
                    .build();
        }
    }

    @DELETE
    @Path("/{codigo}/proveedor/{proveedor}")
    public Response delete(
            @PathParam("codigo")
            String codigo,

            @PathParam("proveedor")
            int proveedor,

            @QueryParam("origen")
            @DefaultValue("0")
            int origen
    ) {
        try {
            return Response.ok(
                    service.delete(
                            codigo,
                            proveedor,
                            origen
                    )
            ).build();

        } catch (Exception ex) {
            ex.printStackTrace();

            return Response.serverError()
                    .entity("Error eliminando artículo")
                    .build();
        }
    }
}