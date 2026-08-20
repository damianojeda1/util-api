package com.util.api.tiendanube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TiendanubeProductoDTO {

    public Long id;

    public List<Variante> variants;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Variante {

        public Long id;

        public String sku;

        public String price;

        public Double stock;
    }
}