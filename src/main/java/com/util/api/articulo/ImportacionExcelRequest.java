
package com.util.api.articulo;

import java.util.List;

public class ImportacionExcelRequest {

    public List<Item> articulos;

    public boolean actualizaMargen;
    public boolean cargarNuevos;

    public static class Item {
        public ArticuloDTO articulo;
        public boolean actualizaStock;
    }
}
