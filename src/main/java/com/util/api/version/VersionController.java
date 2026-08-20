package com.util.api.version;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/versiones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VersionController {

    @Inject
    VersionService service;

    // -------------------------------------------------------------------------
    // OBTENER ACTUAL
    // -------------------------------------------------------------------------

    @GET
    @Path("/actual")
    public Response obtenerActual(
            @QueryParam("idApp") int idApp,
            @QueryParam("terminal") String terminal,
            @QueryParam("terminalPrincipal")
            String terminalPrincipal,
            @QueryParam("terminalAlternativa")
            String terminalAlternativa
    ) {

        if (idApp <= 0) {
            return Response.ok()
                    .entity(null)
                    .build();
        }

        try {
            VersionDTO resultado;

            if (!vacio(terminalPrincipal)
                    || !vacio(terminalAlternativa)) {

                resultado =
                        service.obtenerActual(
                                idApp,
                                terminalPrincipal,
                                terminalAlternativa
                        );

            } else {

                resultado =
                        service.obtenerActual(
                                idApp,
                                terminal
                        );
            }

            /*
             * No devolver 404. El Desktop espera null cuando
             * la terminal todavía no tiene registro.
             */
            return Response.ok(resultado).build();

        } catch (Exception ex) {

            ex.printStackTrace();
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------
    // EXISTE
    // -------------------------------------------------------------------------

    @GET
    @Path("/existe")
    public Response existe(
            @QueryParam("idApp") int idApp,
            @QueryParam("terminal")
            List<String> terminales
    ) {

        if (idApp <= 0
                || terminales == null
                || terminales.isEmpty()) {

            return Response.ok(
                    new VersionDTO.ExisteResponse(false)
            ).build();
        }

        try {
            boolean existe =
                    service.existe(
                            idApp,
                            terminales
                    );

            return Response.ok(
                    new VersionDTO.ExisteResponse(existe)
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            new VersionDTO.ExisteResponse(false)
                    )
                    .build();
        }
    }

    // -------------------------------------------------------------------------
    // ASEGURAR REGISTRO INICIAL
    // -------------------------------------------------------------------------

    @POST
    @Path("/inicial")
    public Response asegurarRegistroInicial(
            VersionDTO.IdentificacionRequest request
    ) {

        if (request == null) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.asegurarRegistroInicial(
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
    // ACTUALIZAR
    // -------------------------------------------------------------------------

    @PUT
    public Response actualizarVersion(
            VersionDTO.ActualizacionRequest request
    ) {

        if (request == null) {
            return Response.ok(false).build();
        }

        try {
            return Response.ok(
                    service.actualizarVersion(
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

    private boolean vacio(
            String valor
    ) {

        return valor == null
                || valor.trim().isEmpty();
    }
}