package com.util.api.tiendanube;

import com.util.api.canalconfiguracion.CanalConfiguracionDTO;
import com.util.api.canalconfiguracion.CanalConfiguracionService;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
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
    @Produces(MediaType.TEXT_PLAIN)
    public Response callback(
            @QueryParam("code") String code
    ) {

        if (code == null || code.isBlank()) {
            return Response.status(
                            Response.Status.BAD_REQUEST
                    )
                    .entity(
                            "No se recibió el parámetro code"
                    )
                    .build();
        }

        return Response.ok(
                "Código recibido correctamente:\n\n"
                        + code
                        + "\n\nCopialo y usalo para obtener el access token."
        ).build();
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
}