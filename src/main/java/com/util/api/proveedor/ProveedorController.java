package com.util.api.proveedor;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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

@Path("/api/proveedores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProveedorController {

    @Inject
    ProveedorService service;

    @GET
    public Response obtenerCompleto(
            @QueryParam("incluirProvExterno")
            boolean incluirProvExterno,

            @QueryParam("usuario")
            int codigoUsuario
    ) {
        try {
            List<ProveedorDTO> resultado =
                    service.obtenerCompleto(
                            incluirProvExterno,
                            codigoUsuario
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo proveedores",
                    ex
            );

            return Response
                    .serverError()
                    .entity(List.of())
                    .build();
        }
    }

    @GET
    @Path("/combo")
    public Response obtenerParaCombo(
            @QueryParam("incluirTodos")
            boolean incluirTodos
    ) {
        try {
            List<ProveedorDTO.ComboDTO> resultado =
                    service.obtenerParaCombo(
                            incluirTodos
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo proveedores para combo",
                    ex
            );

            return Response
                    .serverError()
                    .entity(List.of())
                    .build();
        }
    }

    @GET
    @Path("/identificacion/{codigoIdentificacion}")
    public Response buscarPorCodigoIdentificacion(
            @PathParam("codigoIdentificacion")
            String codigoIdentificacion,

            @QueryParam("usuario")
            int codigoUsuario
    ) {
        try {
            ProveedorDTO resultado =
                    service.buscarPorCodigoIdentificacion(
                            codigoIdentificacion,
                            codigoUsuario
                    );

            if (resultado == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error buscando proveedor por identificación "
                            + codigoIdentificacion,
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/{codigo}/razon-social")
    public Response obtenerRazonSocial(
            @PathParam("codigo")
            int codigo,

            @QueryParam("habilitado")
            int habilitado
    ) {
        try {
            String resultado =
                    service.obtenerRazonSocial(
                            codigo,
                            habilitado
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo razón social del proveedor "
                            + codigo,
                    ex
            );

            return Response
                    .serverError()
                    .entity("")
                    .build();
        }
    }

    @GET
    @Path("/{codigo}/saldo")
    public Response obtenerSaldo(
            @PathParam("codigo")
            int codigoProveedor
    ) {
        try {
            double resultado =
                    service.obtenerSaldo(
                            codigoProveedor
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error obteniendo saldo del proveedor "
                            + codigoProveedor,
                    ex
            );

            return Response
                    .serverError()
                    .entity(0)
                    .build();
        }
    }

    @GET
    @Path("/{codigo}")
    public Response buscarCod(
            @PathParam("codigo")
            int codigo,

            @QueryParam("usuario")
            int codigoUsuario
    ) {
        try {
            ProveedorDTO resultado =
                    service.buscarCod(
                            codigo,
                            codigoUsuario
                    );

            if (resultado == null) {
                return respuestaJsonNull();
            }

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error buscando proveedor "
                            + codigo,
                    ex
            );

            return Response
                    .serverError()
                    .build();
        }
    }

    @POST
    public Response insertar(
            ProveedorDTO.GuardarRequest request
    ) {
        try {
            boolean resultado =
                    service.insertar(request);

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error insertando proveedor",
                    ex
            );

            return Response
                    .serverError()
                    .entity(false)
                    .build();
        }
    }

    @PUT
    @Path("/{codigo}")
    public Response actualizar(
            @PathParam("codigo")
            int codigo,

            ProveedorDTO.GuardarRequest request
    ) {
        try {
            boolean resultado =
                    service.actualizar(
                            codigo,
                            request
                    );

            return Response
                    .ok(resultado)
                    .build();

        } catch (Exception ex) {
            registrarError(
                    "Error actualizando proveedor "
                            + codigo,
                    ex
            );

            return Response
                    .serverError()
                    .entity(false)
                    .build();
        }
    }

    private Response respuestaJsonNull() {
        return Response
                .ok("null")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private void registrarError(
            String mensaje,
            Exception ex
    ) {
        System.err.println(mensaje);
        ex.printStackTrace();
    }
}