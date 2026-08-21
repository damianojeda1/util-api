package com.util.api.tiendanube;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TiendanubeVinculacionService {

    private final Map<String, String> states =
            new ConcurrentHashMap<>();

    public String generarState(
            String tenant
    ) {

        String state =
                UUID.randomUUID()
                        .toString();

        states.put(
                state,
                tenant
        );

        return state;
    }

    public String obtenerTenant(
            String state
    ) {

        if (state == null) {
            return null;
        }

        return states.get(
                state
        );
    }

    public String consumirState(
            String state
    ) {

        if (state == null) {
            return null;
        }

        return states.remove(
                state
        );
    }
}