package com.util.api.canalconfiguracion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDateTime;

@ApplicationScoped
public class CanalConfiguracionService {

    @Inject
    CanalConfiguracionRepository repository;

    public CanalConfiguracionDTO obtener(
            String canal
    ) throws SQLException {

        return repository.obtener(
                canal
        );
    }

    public boolean guardar(
            String canal,
            String storeId,
            String accessToken
    ) throws SQLException {

        CanalConfiguracionDTO dto =
                new CanalConfiguracionDTO();

        dto.canal =
                canal;

        dto.storeId =
                storeId;

        dto.accessToken =
                accessToken;

        dto.activo =
                true;

        dto.fechaVinculacion =
                LocalDateTime.now();

        return repository.guardar(
                dto
        );
    }

    public boolean eliminar(
            String canal
    ) throws SQLException {

        return repository.eliminar(
                canal
        );
    }

    public boolean estaVinculado(
            String canal
    ) throws SQLException {

        CanalConfiguracionDTO dto =
                repository.obtener(
                        canal
                );

        return dto != null
                && dto.activo
                && dto.storeId != null
                && !dto.storeId.isBlank()
                && dto.accessToken != null
                && !dto.accessToken.isBlank();
    }
}