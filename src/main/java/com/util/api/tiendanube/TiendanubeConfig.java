package com.util.api.tiendanube;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "tiendanube")
public interface TiendanubeConfig {

    String appId();

    String clientSecret();

    String apiUrl();
}