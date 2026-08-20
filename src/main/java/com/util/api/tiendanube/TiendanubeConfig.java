package com.util.api.tiendanube;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "tiendanube")
public interface TiendanubeConfig {

    String storeId();

    String token();

    String apiUrl();
}