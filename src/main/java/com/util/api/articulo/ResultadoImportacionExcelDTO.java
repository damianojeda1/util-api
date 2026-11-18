package com.util.api.articulo;

import java.util.ArrayList;
import java.util.List;

public class ResultadoImportacionExcelDTO {

    public int insertados;
    public int actualizados;
    public int sinCambios;
    public List<String> errores = new ArrayList<>();
}
