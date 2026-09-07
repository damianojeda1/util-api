package com.util.api.tiendanube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TiendanubeTokenDTO {

    public String access_token;
    public String token_type;
    public String scope;
    public Long user_id;

    public TiendanubeTokenDTO() {
    }
}