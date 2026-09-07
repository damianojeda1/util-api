package com.util.api.tiendanube;

import com.util.api.canalconfiguracion.CanalConfiguracionDTO;
import com.util.api.canalconfiguracion.CanalConfiguracionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/tiendanube")
public class TiendanubeController {

    @Inject
    CanalConfiguracionService canalConfiguracionService;

    @Inject
    TiendanubeVinculacionService vinculacionService;

    @Inject
    TiendanubeConfig config;

    @ConfigProperty(name = "util.tenant-id")
    String tenantId;

    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public Response callback(
            @QueryParam("code") String code,
            @QueryParam("state") String state
    ) {

        try {

            if (
                    code == null
                            || code.isBlank()
            ) {

                return Response
                        .status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity(
                                paginaError(
                                        "No se recibió el código de autorización."
                                )
                        )
                        .build();
            }

            if (
                    state == null
                            || state.isBlank()
            ) {

                return Response
                        .status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity(
                                paginaError(
                                        "No se recibió el estado de vinculación."
                                )
                        )
                        .build();
            }

            String tenantState =
                    vinculacionService.consumirState(
                            state
                    );

            if (tenantState == null) {

                return Response
                        .status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity(
                                paginaError(
                                        "La vinculación venció o ya fue utilizada."
                                )
                        )
                        .build();
            }

            if (!tenantState.equals(tenantId)) {

                return Response
                        .status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity(
                                paginaError(
                                        "La vinculación no corresponde a este cliente."
                                )
                        )
                        .build();
            }

            TiendanubeTokenDTO token =
                    vinculacionService.obtenerToken(
                            code
                    );

            if (
                    token == null
                            || token.access_token == null
                            || token.access_token.isBlank()
                            || token.user_id == null
            ) {

                throw new RuntimeException(
                        "Tiendanube no devolvió los datos de vinculación."
                );
            }

            canalConfiguracionService.guardar(
                    "TIENDANUBE",
                    String.valueOf(
                            token.user_id
                    ),
                    token.access_token
            );

            return Response.ok(
                    paginaExito()
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response
                    .serverError()
                    .entity(
                            paginaError(
                                    "No se pudo vincular Tiendanube: "
                                            + ex.getMessage()
                            )
                    )
                    .build();
        }
    }

    private String paginaExito() {

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Util - Tiendanube</title>
            </head>
            <body style="font-family: Arial; text-align: center; padding-top: 80px;">
                <h2>Tiendanube vinculada correctamente</h2>
                <p>La tienda ya está conectada con Util.</p>
                <p>Puede cerrar esta ventana y volver a Util.</p>
            </body>
            </html>
            """;
    }

    private String paginaError(
            String mensaje
    ) {

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Util - Tiendanube</title>
            </head>
            <body style="font-family: Arial; text-align: center; padding-top: 80px;">
                <h2>No se pudo vincular Tiendanube</h2>
                <p>%s</p>
                <p>Puede cerrar esta ventana y volver a Util.</p>
            </body>
            </html>
            """.formatted(
                mensaje
        );
    }

    @GET
    @Path("/estado")
    @Produces(MediaType.APPLICATION_JSON)
    public Response estado() {

        try {

            CanalConfiguracionDTO config =
                    canalConfiguracionService.obtener(
                            "TIENDANUBE"
                    );

            TiendanubeEstadoDTO estado =
                    new TiendanubeEstadoDTO();

            estado.vinculada =
                    config != null
                            && config.activo
                            && config.storeId != null
                            && !config.storeId.isBlank()
                            && config.accessToken != null
                            && !config.accessToken.isBlank();

            if (config != null) {
                estado.storeId =
                        config.storeId;
            }

            return Response.ok(
                    estado
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error obteniendo estado de Tiendanube"
                    )
                    .build();
        }
    }

    @GET
    @Path("/vincular")
    @Produces(MediaType.APPLICATION_JSON)
    public Response vincular() {

        try {

            if (
                    tenantId == null
                            || tenantId.isBlank()
            ) {

                return Response
                        .status(
                                Response.Status.BAD_REQUEST
                        )
                        .entity(
                                "No se pudo determinar el tenant actual"
                        )
                        .build();
            }

            String state =
                    vinculacionService.generarState(
                            tenantId
                    );

            String url =
                    "https://www.tiendanube.com/apps/"
                            + config.appId()
                            + "/authorize"
                            + "?state="
                            + state;

            TiendanubeVinculacionDTO dto =
                    new TiendanubeVinculacionDTO();

            dto.url =
                    url;

            dto.state =
                    state;

            return Response.ok(
                    dto
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(
                            "Error iniciando vinculación con Tiendanube"
                    )
                    .build();
        }
    }

    @DELETE
    @Path("/vinculacion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response desvincular() {

        try {

            boolean eliminado =
                    canalConfiguracionService.eliminar(
                            "TIENDANUBE"
                    );

            return Response.ok(
                    eliminado
            ).build();

        } catch (Exception ex) {

            ex.printStackTrace();

            return Response.serverError()
                    .entity(false)
                    .build();
        }
    }
}