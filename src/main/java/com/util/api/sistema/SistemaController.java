package com.util.api.sistema;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/sistema")
@Produces(MediaType.APPLICATION_JSON)
public class SistemaController {

    @ConfigProperty(name = "util.tenant-id")
    String tenantId;

    @ConfigProperty(name = "util.api-version")
    String apiVersion;

    @ConfigProperty(name = "util.database-name")
    String database;

    @GET
    @Path("/info")
    public SistemaInfoDTO info() {
        return new SistemaInfoDTO(
                tenantId,
                apiVersion,
                database,
                "OK"
        );
    }
}