package com.util.api.parametrosconexion;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/parametros-conexion")
@Produces(MediaType.APPLICATION_JSON)
public class ParametrosConexionController {

    @Inject
    ParametrosConexionService service;

    @GET
    @Path("/util")
    public Response obtenerConexionUtil() {
        ParametrosConexionDTO parametros =
                service.obtenerConexionUtil();

        if (parametros == null) {
            return Response.status(
                            Response.Status.NOT_FOUND
                    )
                    .entity(
                            "No se encontró la configuración de UTIL"
                    )
                    .build();
        }

        return Response.ok(parametros).build();
    }

    @GET
    @Path("/proveedores/{codigoProveedor}")
    public Response obtenerConexionProveedor(
            @PathParam("codigoProveedor")
            int codigoProveedor
    ) {
        if (codigoProveedor <= 0) {
            return Response.status(
                            Response.Status.BAD_REQUEST
                    )
                    .entity(
                            "El código de proveedor no es válido"
                    )
                    .build();
        }

        ParametrosConexionDTO parametros =
                service.obtenerConexionProveedor(
                        codigoProveedor
                );

        if (parametros == null) {
            return Response.status(
                            Response.Status.NOT_FOUND
                    )
                    .entity(
                            "No se encontró la configuración "
                                    + "del proveedor"
                    )
                    .build();
        }

        return Response.ok(parametros).build();
    }
}