package com.util.api.tiendanube;

import java.util.List;
import java.util.Map;

public class TiendanubeProductoRequest {

    public Map<String, String> name;

    public Boolean published;

    public List<Variante> variants;

    public static class Variante {

        public String sku;

        public String price;

        public Double stock;
    }
}