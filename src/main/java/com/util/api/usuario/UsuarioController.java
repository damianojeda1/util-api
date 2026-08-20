package com.util.api.usuario;

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

import java.util.Collections;

@Path("/api/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioController {

    @Inject
    UsuarioService service;

    // -------------------------------------------------------------------------
    // LISTADO
    // -------------------------------------------------------------------------

    @GET
    public Response obtenerUsuarios(
            @QueryParam("soloHabilitados")
            boolean soloHabilitados,

            @QueryParam("incluirAdmin")
            boolean incluirAdmin
    ) {

        try {
            return Response.ok(
                    service.obtenerUsuarios(
                            soloHabilitados,
                            incluirAdmin
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
    // LOGIN
    // -------------------------------------------------------------------------

    @POST
    @Path("/login")
    public Response login(
            UsuarioDTO.LoginRequest request
    ) {

        if (request == null) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.login(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER POR CÓDIGO
    // -------------------------------------------------------------------------

    @GET
    @Path("/{codigo}")
    public Response obtenerUsuario(
            @PathParam("codigo") int codigo
    ) {

        if (codigo <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            UsuarioDTO.Response usuario =
                    service.obtenerUsuario(codigo);

            return Response.ok(usuario).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // USUARIO RECORDADO
    // -------------------------------------------------------------------------

    @GET
    @Path("/recordado")
    public Response obtenerUsuarioRecordado(
            @QueryParam("terminal") String terminal
    ) {

        if (terminal == null || terminal.isBlank()) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            UsuarioDTO.Response usuario =
                    service.obtenerUsuarioRecordado(
                            terminal
                    );

            return Response.ok(usuario).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // OBTENER POR NOMBRE
    // -------------------------------------------------------------------------

    @GET
    @Path("/nombre/{nombre}")
    public Response obtenerUsuarioNombre(
            @PathParam("nombre") String nombre
    ) {

        if (nombre == null || nombre.isBlank()) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            UsuarioDTO.Response usuario =
                    service.obtenerUsuarioNombre(
                            nombre
                    );

            return Response.ok(usuario).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // INSERTAR
    // -------------------------------------------------------------------------

    @POST
    public Response insertarUsuario(
            UsuarioDTO.GuardarRequest request
    ) {

        if (request == null) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.insertarUsuario(request)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    @PUT
    @Path("/{codigo}")
    public Response actualizarUsuario(
            @PathParam("codigo") int codigo,
            UsuarioDTO.GuardarRequest request
    ) {

        if (codigo <= 0 || request == null) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.actualizarUsuario(
                            codigo,
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
}