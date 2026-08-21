package com.util.api.canalconfiguracion;

import java.time.LocalDateTime;

public class CanalConfiguracionDTO {

    public int codigo;
    public String canal;
    public String storeId;
    public String accessToken;
    public boolean activo;
    public LocalDateTime fechaVinculacion;

    public CanalConfiguracionDTO() {
    }
}