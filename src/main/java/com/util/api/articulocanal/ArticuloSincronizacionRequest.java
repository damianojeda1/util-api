package com.util.api.articulocanal;

import java.util.ArrayList;
import java.util.List;

public class ArticuloSincronizacionRequest {

    public String canal;

    public List<ArticuloSincronizacionDTO> articulos =
            new ArrayList<>();

    public ArticuloSincronizacionRequest() {
    }
}