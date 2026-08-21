package com.util.api.tiendanube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TiendanubeImagenDTO {

    public Long id;
    public Long product_id;
    public String src;
    public Integer position;
}