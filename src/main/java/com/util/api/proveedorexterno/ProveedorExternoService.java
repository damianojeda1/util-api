package com.util.api.proveedorexterno;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProveedorExternoService {

    private static final Logger LOG =
            Logger.getLogger(ProveedorExternoService.class);

    @Inject
    ProveedorExternoRepository repository;

    @Inject
    ProveedorExternoSincronizacionRepository
            sincronizacionRepository;

    public List<ProveedorExternoDTO.ProveedorResponse>
    listarProveedoresInstalados() {

        return repository.listarProveedoresInstalados();
    }

    public ProveedorExternoDTO.SincronizarResponse sincronizar(
            int codigoProveedor,
            ProveedorExternoDTO.SincronizarRequest request
    ) {
        try {
            return sincronizacionRepository.sincronizar(
                    codigoProveedor,
                    request.isCargarArticulos()
            );
        } catch (Exception e) {
            LOG.error(
                    "Error sincronizando proveedor externo "
                            + codigoProveedor,
                    e
            );

            ProveedorExternoDTO.SincronizarResponse response =
                    new ProveedorExternoDTO.SincronizarResponse();

            response.setExitoso(false);
            response.setMensaje(
                    "No se pudo sincronizar el proveedor externo: "
                            + obtenerMensaje(e)
            );

            return response;
        }
    }

    private String obtenerMensaje(Exception e) {
        if (e.getMessage() != null
                && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }

        return e.getClass().getSimpleName();
    }
}