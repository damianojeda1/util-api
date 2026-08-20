package com.util.api.articulocanal;

import java.time.LocalDateTime;

public class ArticuloCanalDTO {

    public String codigoArticulo;
    public String codigoProveedor;
    public String canal;

    public String idExterno;
    public String idVarianteExterna;

    public Boolean publicado;
    public LocalDateTime ultimaSincronizacion;

    public ArticuloCanalDTO() {
    }
}