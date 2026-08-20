package com.util.api.proveedorexterno;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/proveedores-externos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProveedorExternoController {

    @Inject
    ProveedorExternoService service;

    @GET
    public List<ProveedorExternoDTO.ProveedorResponse>
    listarProveedoresInstalados() {

        return service.listarProveedoresInstalados();
    }

    @POST
    @Path("/{codigoProveedor}/sincronizar")
    public ProveedorExternoDTO.SincronizarResponse sincronizar(
            @PathParam("codigoProveedor") int codigoProveedor,
            ProveedorExternoDTO.SincronizarRequest request
    ) {
        if (codigoProveedor <= 0) {
            throw new BadRequestException(
                    "El código del proveedor es inválido"
            );
        }

        if (request == null) {
            request =
                    new ProveedorExternoDTO.SincronizarRequest();
        }

        return service.sincronizar(
                codigoProveedor,
                request
        );
    }
}